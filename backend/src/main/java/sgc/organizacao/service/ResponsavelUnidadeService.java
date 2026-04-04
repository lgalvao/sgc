package sgc.organizacao.service;

import lombok.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.*;
import sgc.comum.config.CacheConfig;
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
public class ResponsavelUnidadeService {
    private final UnidadeRepo unidadeRepo;
    private final UsuarioRepo usuarioRepo;
    private final AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;
    private final ResponsabilidadeRepo responsabilidadeRepo;
    private final ComumRepo repo;

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
                .orElseThrow(() -> new IllegalStateException(
                        "Usuário ausente para atribuição temporária %d".formatted(atribuicao.getCodigo())));

        return AtribuicaoDto.builder()
                .codigo(atribuicao.getCodigo())
                .unidadeCodigo(atribuicao.getUnidade().getCodigo())
                .unidadeSigla(atribuicao.getUnidade().getSigla())
                .usuario(UsuarioResumoDto.fromEntityObrigatorio(usuario))
                .dataInicio(atribuicao.getDataInicio())
                .dataTermino(atribuicao.getDataTermino())
                .justificativa(atribuicao.getJustificativa())
                .build();
    }

    /**
     * Cria uma atribuição temporária de responsável para uma unidade.
     *
     * @throws ErroValidacao se a data de término for anterior à data de início
     */
    @CacheEvict(cacheNames = CacheConfig.CACHE_DIAGNOSTICO_ORGANIZACIONAL, allEntries = true)
    public void criarAtribuicaoTemporaria(Long codUnidade, CriarAtribuicaoRequest request) {
        Unidade unidade = repo.buscar(Unidade.class, codUnidade);

        String titulo = request.tituloEleitoralUsuario();
        Usuario usuario = repo.buscar(Usuario.class, titulo);

        LocalDate inicio = request.dataInicio() != null ? request.dataInicio() : LocalDate.now();

        if (request.dataTermino().isBefore(inicio)) {
            throw new ErroValidacao(Mensagens.DATA_FIM_DEVE_SER_POSTERIOR);
        }

        AtribuicaoTemporaria atribuicao = new AtribuicaoTemporaria()
                .setUnidade(unidade)
                .setUsuarioTitulo(usuario.getTituloEleitoral())
                .setUsuarioMatricula(usuario.getMatricula())
                .setDataInicio(request.dataInicio() != null ? request.dataInicio().atStartOfDay() : LocalDateTime.now())
                .setDataTermino(request.dataTermino().atTime(23, 59, 59))
                .setJustificativa(request.justificativa());

        atribuicaoTemporariaRepo.save(atribuicao);
    }

    /**
     * Busca o responsável atual de uma unidade (com atribuições carregadas).
     *
     * @throws ErroEntidadeNaoEncontrada se a unidade ou responsável não for encontrado
     */
    @Transactional(readOnly = true)
    public Usuario buscarResponsavelAtual(String siglaUnidade) {
        Long unidadeCodigo = buscarCodigoUnidadePorSigla(siglaUnidade);
        ResponsabilidadeUnidadeLeitura responsabilidade = buscarResponsabilidadeDetalhada(unidadeCodigo);
        return repo.buscar(Usuario.class, responsabilidade.usuarioTitulo());
    }

    /**
     * Busca o responsável atual de uma unidade com detalhes da responsabilidade.
     */
    @Transactional(readOnly = true)
    public ResponsavelDto buscarResponsabilidadeDetalhadaAtual(String siglaUnidade) {
        Long unidadeCodigo = buscarCodigoUnidadePorSigla(siglaUnidade);
        return buscarResponsabilidadeDetalhadaAtual(unidadeCodigo);
    }

    /**
     * Busca o responsável atual de uma unidade com detalhes da responsabilidade.
     */
    @Transactional(readOnly = true)
    public ResponsavelDto buscarResponsabilidadeDetalhadaAtual(Long unidadeCodigo) {
        ResponsabilidadeUnidadeLeitura responsabilidade = buscarResponsabilidadeDetalhada(unidadeCodigo);
        Usuario usuario = repo.buscar(Usuario.class, responsabilidade.usuarioTitulo());

        return ResponsavelDto.builder()
                .usuario(UsuarioResumoDto.fromEntityObrigatorio(usuario))
                .tipo(responsabilidade.tipo())
                .dataInicio(responsabilidade.dataInicio())
                .dataFim(responsabilidade.dataFim())
                .build();
    }

    /**
     * Busca o responsável (titular e substituto) de uma unidade.
     *
     * @throws ErroEntidadeNaoEncontrada se não houver responsável
     */
    public UnidadeResponsavelDto buscarResponsavelUnidade(Long unidadeCodigo) {
        ResponsabilidadeUnidadeLeitura responsabilidade = buscarResponsabilidadeDetalhada(unidadeCodigo);
        Usuario responsavel = repo.buscar(Usuario.class, responsabilidade.usuarioTitulo());
        Usuario titularOficial = repo.buscar(Usuario.class, obterTituloTitularObrigatorio(responsabilidade));
        return montarResponsavelDto(unidadeCodigo, responsavel, titularOficial);
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

    private String obterTituloTitularObrigatorio(ResponsabilidadeUnidadeLeitura responsabilidade) {
        String tituloTitular = responsabilidade.tituloTitular();
        if (tituloTitular == null || tituloTitular.isBlank()) {
            throw new IllegalStateException("Titular oficial ausente para unidade %d".formatted(responsabilidade.unidadeCodigo()));
        }
        return tituloTitular;
    }

    private String obterTituloTitularObrigatorio(ResponsabilidadeUnidadeResumoLeitura responsabilidade) {
        String tituloTitular = responsabilidade.titularTitulo();
        if (tituloTitular == null || tituloTitular.isBlank()) {
            throw new IllegalStateException("Titular oficial ausente para unidade %d".formatted(responsabilidade.unidadeCodigo()));
        }
        return tituloTitular;
    }

    private UnidadeResponsavelDto montarResponsavelDto(Long unidadeCodigo, Usuario responsavel, Usuario titularOficial) {
        // Se o responsável é o próprio titular
        if (responsavel.getTituloEleitoral().equals(titularOficial.getTituloEleitoral())) {
            return UnidadeResponsavelDto.builder()
                    .unidadeCodigo(unidadeCodigo)
                    .titularTitulo(responsavel.getTituloEleitoral())
                    .titularNome(responsavel.getNome())
                    .substitutoTitulo(null)
                    .substitutoNome(null)
                    .build();
        }

        // Caso o responsável seja um substituto ou atribuição temporária
        return UnidadeResponsavelDto.builder()
                .unidadeCodigo(unidadeCodigo)
                .titularTitulo(titularOficial.getTituloEleitoral())
                .titularNome(titularOficial.getNome())
                .substitutoTitulo(responsavel.getTituloEleitoral())
                .substitutoNome(responsavel.getNome())
                .build();
    }

    private UnidadeResponsavelDto montarResponsavelDto(ResponsabilidadeUnidadeResumoLeitura responsabilidade) {
        String titularTitulo = obterTituloTitularObrigatorio(responsabilidade);
        if (responsabilidade.responsavelNome() == null || responsabilidade.titularNome() == null) {
            throw new IllegalStateException("Responsável ou titular oficial ausente para unidade %d".formatted(responsabilidade.unidadeCodigo()));
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

        Set<Long> unidadesComResponsavelEfetivo = responsabilidadeRepo.listarLeiturasPorCodigosUnidade(unidadesCodigos).stream()
                .filter(responsabilidade -> !responsabilidade.usuarioTitulo().isBlank())
                .map(ResponsabilidadeLeitura::unidadeCodigo)
                .collect(toSet());

        return unidadesComResponsavelEfetivo.containsAll(unidadesCodigos);
    }

    private Long buscarCodigoUnidadePorSigla(String siglaUnidade) {
        return unidadeRepo.buscarCodigoAtivoPorSigla(siglaUnidade)
                .orElseGet(() -> repo.buscarPorSigla(Unidade.class, siglaUnidade).getCodigo());
    }

    private ResponsabilidadeUnidadeLeitura buscarResponsabilidadeDetalhada(Long unidadeCodigo) {
        return responsabilidadeRepo.buscarLeituraDetalhadaPorCodigoUnidade(unidadeCodigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(Responsabilidade.class.getSimpleName(), unidadeCodigo));
    }
}
