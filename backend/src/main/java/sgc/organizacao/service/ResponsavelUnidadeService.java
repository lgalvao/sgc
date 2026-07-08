package sgc.organizacao.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.*;
import sgc.alerta.model.*;
import sgc.comum.*;
import sgc.comum.config.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;

import java.time.*;
import java.util.*;

import static java.util.stream.Collectors.*;

/**
 * Serviço especializado para gerenciar responsáveis e atribuições de unidades.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Gestão de responsáveis (titular e substituto)</li>
 *   <li>Atribuições temporárias de responsáveis</li>
 *   <li>Carregamento de perfis e atribuições de usuários</li>
 *   <li>Consultas em lote de responsáveis</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResponsavelUnidadeService {
    private final UnidadeRepo unidadeRepo;
    private final UsuarioRepo usuarioRepo;
    private final AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;
    private final ResponsabilidadeRepo responsabilidadeRepo;
    private final CacheViewsOrganizacaoService cacheViewsOrganizacaoService;
    private final CacheOrganizacaoService cacheOrganizacaoService;
    private final AlertaAplicacaoService alertaAplicacaoService;
    private final NotificacaoService notificacaoService;
    private final EmailModelosService emailModelosService;
    private final ConfigAplicacao configAplicacao;
    private final ComumRepo repo;
    private final sgc.organizacao.OrganizacaoDtoMapper organizacaoDtoMapper;


    /**
     * Busca todas as atribuições temporárias cadastradas.
     */
    public List<AtribuicaoDto> buscarTodasAtribuicoes() {
        List<AtribuicaoTemporaria> atribuicoes = atribuicaoTemporariaRepo.listarTodasComUnidade();
        Map<String, Usuario> usuariosPorTitulo = carregarUsuariosPorTitulo(atribuicoes);

        return atribuicoes.stream()
                .map(atribuicao -> toAtribuicaoTemporariaDto(atribuicao, usuariosPorTitulo))
                .toList();
    }

    public List<AtribuicaoDto> buscarAtribuicoesPorUnidade(Long codUnidade) {
        List<AtribuicaoTemporaria> atribuicoes = atribuicaoTemporariaRepo.listarPorUnidadeComUnidade(codUnidade);
        Map<String, Usuario> usuariosPorTitulo = carregarUsuariosPorTitulo(atribuicoes);

        return atribuicoes.stream()
                .map(atribuicao -> toAtribuicaoTemporariaDto(atribuicao, usuariosPorTitulo))
                .toList();
    }

    private Map<String, Usuario> carregarUsuariosPorTitulo(List<AtribuicaoTemporaria> atribuicoes) {
        List<String> titulos = atribuicoes.stream()
                .map(AtribuicaoTemporaria::getUsuarioTitulo)
                .distinct()
                .toList();

        if (titulos.isEmpty()) {
            return Map.of();
        }

        return usuarioRepo.listarPorTitulosComUnidadeLotacao(titulos).stream()
                .collect(toMap(Usuario::getTituloEleitoral, usuario -> usuario));
    }

    private AtribuicaoDto toAtribuicaoTemporariaDto(AtribuicaoTemporaria atribuicao, Map<String, Usuario> usuariosPorTitulo) {
        Usuario usuario = Optional.ofNullable(usuariosPorTitulo.get(atribuicao.getUsuarioTitulo()))
                .orElseThrow(() -> new ErroInconsistenciaInterna(
                        "Usuário ausente para atribuição temporária %d".formatted(atribuicao.getCodigo())));

        return organizacaoDtoMapper.paraAtribuicaoDto(atribuicao, usuario);
    }

    /**
     * Cria uma atribuição temporária de responsável para uma unidade.
     *
     * @throws ErroValidacao se a data de término for anterior à data de início
     */
    @Transactional
    public void criarAtribuicaoTemporaria(Long codUnidade, CriarAtribuicaoRequest request) {
        Unidade unidade = repo.buscar(Unidade.class, codUnidade);
        Usuario usuario = buscarUsuarioObrigatorio(request.tituloEleitoralUsuario());
        AtribuicaoTemporaria atribuicao = montarAtribuicaoTemporaria(new ContextoAtribuicaoTemporaria(new AtribuicaoTemporaria(), unidade, usuario, request));

        AtribuicaoTemporaria atribuicaoSalva = atribuicaoTemporariaRepo.save(atribuicao);
        criarNotificacoesAtribuicaoTemporaria(atribuicaoSalva, usuario);
        cacheOrganizacaoService.invalidarAposCommit();
    }

    @Transactional
    public void atualizarAtribuicaoTemporaria(Long codUnidade, Long codigoAtribuicao, CriarAtribuicaoRequest request) {
        Unidade unidade = repo.buscar(Unidade.class, codUnidade);
        AtribuicaoTemporaria atribuicao = buscarAtribuicaoObrigatoria(codigoAtribuicao);
        validarPertencimentoUnidade(atribuicao, codUnidade);
        Usuario usuario = buscarUsuarioObrigatorio(request.tituloEleitoralUsuario());

        montarAtribuicaoTemporaria(new ContextoAtribuicaoTemporaria(atribuicao, unidade, usuario, request));
        atribuicaoTemporariaRepo.save(atribuicao);
        cacheOrganizacaoService.invalidarAposCommit();
    }

    @Transactional
    public void removerAtribuicaoTemporaria(Long codUnidade, Long codigoAtribuicao) {
        AtribuicaoTemporaria atribuicao = buscarAtribuicaoObrigatoria(codigoAtribuicao);
        validarPertencimentoUnidade(atribuicao, codUnidade);
        atribuicaoTemporariaRepo.delete(atribuicao);
        cacheOrganizacaoService.invalidarAposCommit();
    }

    private Usuario buscarUsuarioObrigatorio(String titulo) {
        return repo.buscar(Usuario.class, titulo);
    }

    private AtribuicaoTemporaria buscarAtribuicaoObrigatoria(Long codigoAtribuicao) {
        return repo.buscar(AtribuicaoTemporaria.class, codigoAtribuicao);
    }

    private void validarPertencimentoUnidade(AtribuicaoTemporaria atribuicao, Long codUnidade) {
        if (!Objects.equals(atribuicao.getUnidade().getCodigo(), codUnidade)) {
            throw new ErroValidacao("A atribuição temporária não pertence à unidade informada.");
        }
    }

    private AtribuicaoTemporaria montarAtribuicaoTemporaria(ContextoAtribuicaoTemporaria contexto) {
        AtribuicaoTemporaria atribuicao = contexto.atribuicao();
        Unidade unidade = contexto.unidade();
        Usuario usuario = contexto.usuario();
        CriarAtribuicaoRequest request = contexto.request();

        LocalDate inicio = request.dataInicio() != null ? request.dataInicio() : LocalDate.now();
        if (request.dataTermino().isBefore(inicio)) {
            throw new ErroValidacao(Mensagens.DATA_FIM_DEVE_SER_POSTERIOR);
        }

        LocalDateTime dataInicio = request.dataInicio() != null ? request.dataInicio().atStartOfDay() : LocalDateTime.now();
        LocalDateTime dataTermino = request.dataTermino().atTime(23, 59, 59);
        validarSobreposicaoPeriodo(new PeriodoAtribuicaoDto(unidade.getCodigo(), dataInicio, dataTermino, atribuicao.getCodigo()));

        return atribuicao
                .setUnidade(unidade)
                .setUsuarioTitulo(usuario.getTituloEleitoral())
                .setUsuarioMatricula(usuario.getMatricula())
                .setDataInicio(dataInicio)
                .setDataTermino(dataTermino)
                .setJustificativa(request.justificativa());
    }

    private void validarSobreposicaoPeriodo(PeriodoAtribuicaoDto periodo) {
        if (atribuicaoTemporariaRepo.existeSobreposicaoPeriodo(
                periodo.codUnidade(), periodo.dataInicio(), periodo.dataTermino(), periodo.codigoIgnorado())) {
            throw new ErroValidacao(Mensagens.ATRIBUICAO_TEMPORARIA_SOBREPOSTA);
        }
    }

    private void criarNotificacoesAtribuicaoTemporaria(AtribuicaoTemporaria atribuicao, Usuario usuario) {
        validarUsuarioComEmail(usuario);
        String siglaUnidade = atribuicao.getUnidade().getSigla();
        String assunto = AssuntosNotificacao.atribuicaoPerfilChefe(siglaUnidade);
        criarAlertaSemInterromperNotificacao(usuario, siglaUnidade);

        String corpoHtml = emailModelosService.criarEmailAtribuicaoTemporaria(
                new EmailModelosService.EmailAtribuicaoTemporariaCommand(
                        assunto,
                        usuario.getNome(),
                        siglaUnidade,
                        atribuicao.getDataInicio(),
                        atribuicao.getDataTermino(),
                        atribuicao.getJustificativa(),
                        urlSistema()
                )
        );

        notificacaoService.enfileirar(EnfileirarNotificacaoCommand.builder()
                .processo(null)
                .tipoNotificacao(TipoNotificacao.ATRIBUICAO_TEMPORARIA)
                .usuarioDestinoTitulo(usuario.getTituloEleitoral())
                .unidadeDestinoSigla(siglaUnidade)
                .unidadeOrigemSigla(siglaUnidade)
                .destinatario(Objects.requireNonNull(usuario.getEmail(), "Usuário sem e-mail após validação"))
                .assunto(assunto)
                .corpoHtml(corpoHtml)
                .chaveIdempotencia(chaveIdempotenciaAtribuicaoTemporaria(atribuicao))
                .build());
    }

    private void validarUsuarioComEmail(Usuario usuario) {
        if (usuario.getEmail() == null) {
            throw new ErroValidacao(Mensagens.USUARIO_SEM_EMAIL);
        }
    }

    private void criarAlertaSemInterromperNotificacao(Usuario usuario, String siglaUnidade) {
        try {
            alertaAplicacaoService.criarAlertaPessoal(
                    usuario.getTituloEleitoral(),
                    "Atribuição temporária para unidade %s".formatted(siglaUnidade)
            );
        } catch (RuntimeException ex) {
            log.warn(
                    "Falha ao criar alerta pessoal de atribuicao temporaria para usuario {}. Fluxo de e-mail sera mantido.",
                    usuario.getTituloEleitoral(),
                    ex
            );
        }
    }

    private String chaveIdempotenciaAtribuicaoTemporaria(AtribuicaoTemporaria atribuicao) {
        if (atribuicao.getCodigo() != null) {
            return "atribuicao-temporaria:%d".formatted(atribuicao.getCodigo());
        }
        return "atribuicao-temporaria:unidade:%d:usuario:%s:inicio:%s:termino:%s".formatted(
                atribuicao.getUnidade().getCodigo(),
                atribuicao.getUsuarioTitulo(),
                atribuicao.getDataInicio(),
                atribuicao.getDataTermino()
        );
    }

    private String urlSistema() {
        String url = configAplicacao.isAmbienteTestes()
                ? configAplicacao.getUrlAcessoHom()
                : configAplicacao.getUrlAcessoProd();
        return url == null || url.isBlank() ? "http://localhost:5173" : url;
    }

    /**
     * Busca o responsável atual de uma unidade (com atribuições carregadas).
     *
     * @throws ErroEntidadeNaoEncontrada se a unidade não for encontrada
     */
    @Transactional(readOnly = true)
    public @Nullable Usuario buscarResponsavelAtual(String siglaUnidade) {
        Long unidadeCodigo = buscarCodigoUnidadePorSigla(siglaUnidade);
        Optional<ResponsabilidadeUnidadeLeitura> responsabilidadeOpt = responsabilidadeRepo.buscarLeituraDetalhadaPorCodigoUnidade(unidadeCodigo);

        if (responsabilidadeOpt.isEmpty()) {
            return null;
        }

        ResponsabilidadeUnidadeLeitura responsabilidade = responsabilidadeOpt.get();
        return repo.buscar(Usuario.class, responsabilidade.usuarioTitulo());
    }

    /**
     * Busca o responsável atual de uma unidade com detalhes da responsabilidade.
     */
    @Transactional(readOnly = true)
    public @Nullable ResponsavelDto buscarResponsabilidadeDetalhadaAtual(String siglaUnidade) {
        Optional<Long> unidadeCodigoOpt = unidadeRepo.buscarCodigoAtivoPorSigla(siglaUnidade);
        return unidadeCodigoOpt.map(this::buscarResponsabilidadeDetalhadaAtual).orElse(null);
    }

    /**
     * Busca o responsável atual de uma unidade com detalhes da responsabilidade.
     */
    @Transactional(readOnly = true)
    public @Nullable ResponsavelDto buscarResponsabilidadeDetalhadaAtual(Long unidadeCodigo) {
        Optional<ResponsabilidadeUnidadeLeitura> responsabilidadeOpt = responsabilidadeRepo.buscarLeituraDetalhadaPorCodigoUnidade(unidadeCodigo);

        if (responsabilidadeOpt.isEmpty()) {
            return null;
        }

        ResponsabilidadeUnidadeLeitura responsabilidade = responsabilidadeOpt.get();
        Usuario usuario = repo.buscar(Usuario.class, responsabilidade.usuarioTitulo());

        return ResponsavelDto.builder()
                .usuario(organizacaoDtoMapper.paraUsuarioResumoObrigatorio(usuario))
                .tipo(responsabilidade.tipo())
                .dataInicio(responsabilidade.dataInicio())
                .dataFim(responsabilidade.dataFim())
                .build();
    }

    /**
     * Busca o responsável (titular e substituto) de uma unidade de forma segura, retornando Optional.
     */
    public Optional<UnidadeResponsavelDto> buscarResponsavelUnidadeOpt(Long unidadeCodigo) {
        List<ResponsabilidadeUnidadeResumoLeitura> lista = responsabilidadeRepo.listarResumosPorCodigosUnidade(List.of(unidadeCodigo));
        if (lista.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(montarResponsavelDto(lista.getFirst()));
    }

    /**
     * Busca o responsável (titular e substituto) de uma unidade.
     *
     * @throws ErroEntidadeNaoEncontrada se não houver responsável
     */
    public UnidadeResponsavelDto buscarResponsavelUnidade(Long unidadeCodigo) {
        return buscarResponsavelUnidadeOpt(unidadeCodigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(Responsabilidade.class.getSimpleName(), unidadeCodigo));
    }

    /**
     * Busca responsáveis de múltiplas unidades em lote.
     */
    @Transactional(readOnly = true)
    public Map<Long, UnidadeResponsavelDto> buscarResponsaveisUnidades(List<Long> unidadesCodigos) {
        if (unidadesCodigos.isEmpty()) return Collections.emptyMap();

        List<ResponsabilidadeUnidadeResumoLeitura> responsabilidades = responsabilidadeRepo.listarResumosPorCodigosUnidade(unidadesCodigos);
        if (responsabilidades.isEmpty()) return Collections.emptyMap();

        return responsabilidades.stream()
                .collect(toMap(
                        ResponsabilidadeUnidadeResumoLeitura::unidadeCodigo,
                        this::montarResponsavelDto
                ));
    }

    private String obterTituloTitularObrigatorio(ResponsabilidadeUnidadeResumoLeitura responsabilidade) {
        String tituloTitular = responsabilidade.titularTitulo();
        if (tituloTitular == null || tituloTitular.isBlank()) {
            throw new ErroInconsistenciaInterna("Titular oficial ausente para unidade %d".formatted(responsabilidade.unidadeCodigo()));
        }
        return tituloTitular;
    }


    private UnidadeResponsavelDto montarResponsavelDto(ResponsabilidadeUnidadeResumoLeitura responsabilidade) {
        String titularTitulo = obterTituloTitularObrigatorio(responsabilidade);
        if (responsabilidade.responsavelNome() == null || responsabilidade.titularNome() == null) {
            throw new ErroInconsistenciaInterna("Responsável ou titular oficial ausente para unidade %d".formatted(responsabilidade.unidadeCodigo()));
        }

        if (responsabilidade.responsavelTitulo().equals(titularTitulo)) {
            return UnidadeResponsavelDto.builder()
                    .unidadeCodigo(responsabilidade.unidadeCodigo())
                    .titularTitulo(titularTitulo)
                    .titularNome(responsabilidade.responsavelNome())
                    .substitutoTitulo(null)
                    .substitutoNome(null)
                    .build();
        }

        return UnidadeResponsavelDto.builder()
                .unidadeCodigo(responsabilidade.unidadeCodigo())
                .titularTitulo(titularTitulo)
                .titularNome(responsabilidade.titularNome())
                .substitutoTitulo(responsabilidade.responsavelTitulo())
                .substitutoNome(responsabilidade.responsavelNome())
                .build();
    }

    /**
     * Busca os códigos das unidades onde o usuário é o responsável atual.
     */
    @Transactional(readOnly = true)
    public List<Long> buscarUnidadesOndeEhResponsavel(String titulo) {
        return responsabilidadeRepo.findByUsuarioTitulo(titulo).stream()
                .map(Responsabilidade::getUnidadeCodigo)
                .toList();
    }

    /**
     * Verifica se todas as unidades informadas possuem responsável efetivo vigente.
     */
    @Transactional(readOnly = true)
    public boolean todasPossuemResponsavelEfetivo(List<Long> unidadesCodigos) {
        if (unidadesCodigos.isEmpty()) {
            return true;
        }

        Set<Long> codigosSolicitados = new HashSet<>(unidadesCodigos);
        Set<Long> unidadesComResponsavelEfetivo = cacheViewsOrganizacaoService.listarTodasResponsabilidades().stream()
                .filter(responsabilidade -> codigosSolicitados.contains(responsabilidade.unidadeCodigo()))
                .filter(responsabilidade -> !responsabilidade.usuarioTitulo().isBlank())
                .map(ResponsabilidadeLeitura::unidadeCodigo)
                .collect(toSet());

        return unidadesComResponsavelEfetivo.containsAll(unidadesCodigos);
    }

    private Long buscarCodigoUnidadePorSigla(String siglaUnidade) {
        return unidadeRepo.buscarCodigoAtivoPorSigla(siglaUnidade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(Unidade.class.getSimpleName(), siglaUnidade));
    }

    private record ContextoAtribuicaoTemporaria(
            AtribuicaoTemporaria atribuicao,
            Unidade unidade,
            Usuario usuario,
            CriarAtribuicaoRequest request
    ) {
    }

    private record PeriodoAtribuicaoDto(
            Long codUnidade,
            LocalDateTime dataInicio,
            LocalDateTime dataTermino,
            Long codigoIgnorado
    ) {
    }

}
