package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import sgc.alerta.AlertaFacade;
import sgc.alerta.EmailService;
import sgc.comum.erros.ErroAcessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.model.ComumRepo;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.dto.ImpactoMapaResponse;
import sgc.mapa.dto.MapaVisualizacaoResponse;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.*;
import sgc.mapa.service.CopiaMapaService;
import sgc.mapa.service.ImpactoMapaService;
import sgc.mapa.service.MapaManutencaoService;
import sgc.mapa.service.MapaSalvamentoService;
import sgc.mapa.service.MapaVisualizacaoService;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapa;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida;
import sgc.subprocesso.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

/**
 * Service unificado para todas as operações do domínio de Subprocesso.
 *
 * <p>Consolida a lógica anteriormente fragmentada em múltiplos services (Workflow, Mapa, Transição, etc.).
 * Implementa o padrão "Radical Simplification".
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SubprocessoService {

    private static final String SIGLA_ADMIN = "ADMIN";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Repositories
    @Setter @Autowired @Lazy
    private SubprocessoRepo subprocessoRepo;
    @Setter @Autowired @Lazy
    private MovimentacaoRepo movimentacaoRepo;
    private final ComumRepo repo;
    private final AnaliseRepo analiseRepo;

    // External Services / Facades
    private final AlertaFacade alertaService;
    private final OrganizacaoFacade organizacaoFacade;
    private final UsuarioFacade usuarioFacade;
    private final ImpactoMapaService impactoMapaService;
    @Setter @Autowired @Lazy
    private CopiaMapaService copiaMapaService; // Renamed from servicoDeCopiaDeMapa
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final MapaSalvamentoService mapaSalvamentoService;
    private final MapaAjusteMapper mapaAjusteMapper;
    private final SgcPermissionEvaluator permissionEvaluator;
    private final MapaVisualizacaoService mapaVisualizacaoService;

    // @Lazy to break circular dependency: SubprocessoService -> MapaManutencaoService -> SubprocessoService (via Facade)
    @Lazy @Autowired @Setter
    private MapaManutencaoService mapaManutencaoService;

    // Mapa Constants
    private static final Map<TipoProcesso, SituacaoSubprocesso> SITUACAO_MAPA_DISPONIBILIZADO = new EnumMap<>(Map.of(
            TipoProcesso.MAPEAMENTO, MAPEAMENTO_MAPA_DISPONIBILIZADO,
            TipoProcesso.REVISAO, REVISAO_MAPA_DISPONIBILIZADO));

    private static final Map<TipoProcesso, SituacaoSubprocesso> SITUACAO_MAPA_COM_SUGESTOES = new EnumMap<>(Map.of(
            TipoProcesso.MAPEAMENTO, MAPEAMENTO_MAPA_COM_SUGESTOES,
            TipoProcesso.REVISAO, REVISAO_MAPA_COM_SUGESTOES));

    private static final Map<TipoProcesso, SituacaoSubprocesso> SITUACAO_MAPA_VALIDADO = new EnumMap<>(Map.of(
            TipoProcesso.MAPEAMENTO, MAPEAMENTO_MAPA_VALIDADO,
            TipoProcesso.REVISAO, REVISAO_MAPA_VALIDADO));

    private static final Map<TipoProcesso, SituacaoSubprocesso> SITUACAO_MAPA_HOMOLOGADO = new EnumMap<>(Map.of(
            TipoProcesso.MAPEAMENTO, MAPEAMENTO_MAPA_HOMOLOGADO,
            TipoProcesso.REVISAO, REVISAO_MAPA_HOMOLOGADO));

    private static final Set<SituacaoSubprocesso> SITUACOES_PERMITIDAS_IMPORTACAO = Set.of(
            NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO);

    // ========================================================================
    // DELEGATES (Replacing MapaFacade / AnaliseFacade usage)
    // ========================================================================

    @Transactional(readOnly = true)
    public MapaVisualizacaoResponse mapaParaVisualizacao(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return mapaVisualizacaoService.obterMapaParaVisualizacao(sp);
    }

    @Transactional(readOnly = true)
    public ImpactoMapaResponse verificarImpactos(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return impactoMapaService.verificarImpactos(sp, usuario);
    }

    @Transactional
    public Mapa salvarMapa(Long codSubprocesso, SalvarMapaRequest request) {
        return salvarMapaSubprocesso(codSubprocesso, request);
    }

    @Transactional(readOnly = true)
    public Mapa mapaCompletoPorSubprocesso(Long codSubprocesso) {
        return mapaManutencaoService.buscarMapaCompletoPorSubprocesso(codSubprocesso);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obterSugestoes(Long codSubprocesso) {
        return Map.of("sugestoes", "");
    }

    // ========================================================================
    // CRUD (from SubprocessoWorkflowService)
    // ========================================================================

    public Subprocesso buscarSubprocesso(Long codigo) {
        return subprocessoRepo.findByIdWithMapaAndAtividades(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codigo));
    }

    public Subprocesso buscarSubprocessoComMapa(Long codigo) {
        return buscarSubprocesso(codigo);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarEntidadesPorProcesso(Long codProcesso) {
        return subprocessoRepo.findByProcessoCodigoWithUnidade(codProcesso);
    }

    @Transactional(readOnly = true)
    public SubprocessoSituacaoDto obterStatus(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        return SubprocessoSituacaoDto.builder()
                .codigo(subprocesso.getCodigo())
                .situacao(subprocesso.getSituacao())
                .build();
    }

    @Transactional(readOnly = true)
    public Subprocesso obterEntidadePorCodigoMapa(Long codMapa) {
        return repo.buscar(Subprocesso.class, "mapa.codigo", codMapa);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarEntidades() {
        return subprocessoRepo.findAllComFetch();
    }

    @Transactional(readOnly = true)
    public Subprocesso obterEntidadePorProcessoEUnidade(Long codProcesso, Long codUnidade) {
        return subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoWithFetch(codProcesso, codUnidade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", "P:%d U:%d".formatted(codProcesso, codUnidade)));
    }

    @Transactional
    public Subprocesso criarEntidade(CriarSubprocessoRequest request) {
        Processo processo = Processo.builder().codigo(request.codProcesso()).build();
        Unidade unidade = Unidade.builder().codigo(request.codUnidade()).build();

        Subprocesso entity = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .dataLimiteEtapa1(request.dataLimiteEtapa1())
                .dataLimiteEtapa2(request.dataLimiteEtapa2())
                .mapa(null)
                .build();

        Subprocesso subprocessoSalvo = subprocessoRepo.save(entity);
        Mapa mapa = Mapa.builder().subprocesso(subprocessoSalvo).build();
        Mapa mapaSalvo = mapaManutencaoService.salvarMapa(mapa);
        subprocessoSalvo.setMapa(mapaSalvo);
        return subprocessoRepo.save(subprocessoSalvo);
    }

    @Transactional
    public Subprocesso atualizarEntidade(Long codigo, AtualizarSubprocessoRequest request) {
        Subprocesso subprocesso = buscarSubprocesso(codigo);
        processarAlteracoes(subprocesso, request);
        return subprocessoRepo.save(subprocesso);
    }

    public void excluir(Long codigo) {
        buscarSubprocesso(codigo);
        subprocessoRepo.deleteById(codigo);
    }

    @Transactional(readOnly = true)
    public boolean verificarAcessoUnidadeAoProcesso(Long codProcesso, List<Long> codigosUnidadesHierarquia) {
        return subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigoIn(codProcesso, codigosUnidadesHierarquia);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarEntidadesPorProcessoEUnidades(Long codProcesso, List<Long> codUnidades) {
        if (codUnidades.isEmpty()) {
            return List.of();
        }
        return subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoInWithUnidade(codProcesso, codUnidades);
    }

    private void processarAlteracoes(Subprocesso subprocesso, AtualizarSubprocessoRequest request) {
        Optional.ofNullable(request.codMapa()).ifPresent(cod -> {
            Mapa m = Mapa.builder().codigo(cod).build();
            Long codAtual = subprocesso.getMapa() != null ? subprocesso.getMapa().getCodigo() : null;
            if (!Objects.equals(codAtual, cod)) {
                subprocesso.setMapa(m);
            }
        });

        if (request.dataLimiteEtapa1() != null && !Objects.equals(subprocesso.getDataLimiteEtapa1(), request.dataLimiteEtapa1())) {
            subprocesso.setDataLimiteEtapa1(request.dataLimiteEtapa1());
        }
        if (request.dataFimEtapa1() != null && !Objects.equals(subprocesso.getDataFimEtapa1(), request.dataFimEtapa1())) {
            subprocesso.setDataFimEtapa1(request.dataFimEtapa1());
        }
        if (request.dataLimiteEtapa2() != null && !Objects.equals(subprocesso.getDataLimiteEtapa2(), request.dataLimiteEtapa2())) {
            subprocesso.setDataLimiteEtapa2(request.dataLimiteEtapa2());
        }
        if (request.dataFimEtapa2() != null && !Objects.equals(subprocesso.getDataFimEtapa2(), request.dataFimEtapa2())) {
            subprocesso.setDataFimEtapa2(request.dataFimEtapa2());
        }
    }

    // ========================================================================
    // VALIDATION (from SubprocessoWorkflowService)
    // ========================================================================

    public List<Atividade> obterAtividadesSemConhecimento(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return obterAtividadesSemConhecimento(sp.getMapa());
    }

    public List<Atividade> obterAtividadesSemConhecimento(@Nullable Mapa mapa) {
        if (mapa == null || mapa.getCodigo() == null) {
            return emptyList();
        }
        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(mapa.getCodigo());
        if (atividades.isEmpty()) {
            return emptyList();
        }
        return atividades.stream()
                .filter(a -> a.getConhecimentos().isEmpty())
                .toList();
    }

    public void validarExistenciaAtividades(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        Mapa mapa = subprocesso.getMapa();

        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(mapa.getCodigo());
        if (atividades.isEmpty()) {
            throw new ErroValidacao("O mapa de competências deve ter ao menos uma atividade cadastrada.");
        }

        List<Atividade> atividadesSemConhecimento = atividades.stream()
                .filter(a -> a.getConhecimentos().isEmpty())
                .toList();

        if (!atividadesSemConhecimento.isEmpty()) {
            throw new ErroValidacao("Todas as atividades devem possuir conhecimentos vinculados. Verifique as atividades pendentes.");
        }
    }

    public void validarAssociacoesMapa(Long mapaId) {
        List<Competencia> competencias = mapaManutencaoService.buscarCompetenciasPorCodMapa(mapaId);
        List<String> competenciasSemAssociacao = competencias.stream()
                .filter(c -> c.getAtividades().isEmpty())
                .map(Competencia::getDescricao)
                .toList();

        if (!competenciasSemAssociacao.isEmpty()) {
            throw new ErroValidacao(
                    "Existem competências que não foram associadas a nenhuma atividade.",
                    Map.of("competenciasNaoAssociadas", competenciasSemAssociacao));
        }

        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigo(mapaId);
        List<String> atividadesSemAssociacao = atividades.stream()
                .filter(a -> a.getCompetencias().isEmpty())
                .map(Atividade::getDescricao)
                .toList();

        if (!atividadesSemAssociacao.isEmpty()) {
            throw new ErroValidacao(
                    "Existem atividades que não foram associadas a nenhuma competência.",
                    Map.of("atividadesNaoAssociadas", atividadesSemAssociacao));
        }
    }

    public ValidacaoCadastroDto validarCadastro(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        List<ErroValidacaoDto> erros = new ArrayList<>();

        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(sp.getMapa().getCodigo());
        if (atividades.isEmpty()) {
            erros.add(ErroValidacaoDto.builder()
                    .tipo("SEM_ATIVIDADES")
                    .mensagem("O mapa não possui atividades cadastradas.")
                    .build());
        } else {
            for (Atividade atividade : atividades) {
                if (atividade.getConhecimentos().isEmpty()) {
                    erros.add(ErroValidacaoDto.builder()
                            .tipo("ATIVIDADE_SEM_CONHECIMENTO")
                            .atividadeCodigo(atividade.getCodigo())
                            .descricaoAtividade(atividade.getDescricao())
                            .mensagem("Esta atividade não possui conhecimentos associados.")
                            .build());
                }
            }
        }

        return ValidacaoCadastroDto.builder()
                .valido(erros.isEmpty())
                .erros(erros)
                .build();
    }

    public void validarSituacaoPermitida(Subprocesso subprocesso, Set<SituacaoSubprocesso> permitidas) {
        if (subprocesso.getSituacao() == null) {
            throw new IllegalArgumentException("Situação do subprocesso não pode ser nula");
        }
        if (permitidas.isEmpty()) {
            throw new IllegalArgumentException("Conjunto de situações permitidas não pode ser vazio");
        }
        if (!permitidas.contains(subprocesso.getSituacao())) {
            String permitidasStr = String.join(", ",
                permitidas.stream().map(SituacaoSubprocesso::name).toList());
            throw new ErroValidacao(
                "Situação do subprocesso não permite esta operação. Situação atual: %s. Situações permitidas: %s"
                    .formatted(subprocesso.getSituacao(), permitidasStr));
        }
    }

    public void validarSituacaoPermitida(Subprocesso subprocesso, SituacaoSubprocesso... permitidas) {
        if (permitidas.length == 0) {
            throw new IllegalArgumentException("Pelo menos uma situação permitida deve ser fornecida");
        }
        validarSituacaoPermitida(subprocesso, Set.of(permitidas));
    }

    public void validarSituacaoPermitida(Subprocesso subprocesso, String mensagem, SituacaoSubprocesso... permitidas) {
        if (subprocesso.getSituacao() == null) {
            throw new IllegalArgumentException("Situação do subprocesso não pode ser nula");
        }
        if (permitidas.length == 0) {
            throw new IllegalArgumentException("Pelo menos uma situação permitida deve ser fornecida");
        }
        if (!Set.of(permitidas).contains(subprocesso.getSituacao())) {
            throw new ErroValidacao(mensagem);
        }
    }

    public void validarSituacaoMinima(Subprocesso subprocesso, SituacaoSubprocesso minima) {
        if (subprocesso.getSituacao() == null) {
            throw new IllegalArgumentException("Situação do subprocesso não pode ser nula");
        }
        if (subprocesso.getSituacao().ordinal() < minima.ordinal()) {
            throw new ErroValidacao(
                "Subprocesso não atingiu a situação mínima necessária. Situação atual: %s. Mínima exigida: %s"
                    .formatted(subprocesso.getSituacao(), minima));
        }
    }

    public void validarSituacaoMinima(Subprocesso subprocesso, SituacaoSubprocesso minima, String mensagem) {
        if (subprocesso.getSituacao() == null) {
            throw new IllegalArgumentException("Situação do subprocesso não pode ser nula");
        }
        if (subprocesso.getSituacao().ordinal() < minima.ordinal()) {
            throw new ErroValidacao(mensagem);
        }
    }

    // ========================================================================
    // CADASTRO WORKFLOW (from SubprocessoWorkflowService)
    // ========================================================================

    @Transactional
    public void disponibilizarCadastro(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp, MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        disponibilizar(sp, MAPEAMENTO_CADASTRO_DISPONIBILIZADO, TipoTransicao.CADASTRO_DISPONIBILIZADO, usuario);
    }

    @Transactional
    public void disponibilizarRevisao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp, REVISAO_CADASTRO_EM_ANDAMENTO);
        disponibilizar(sp, REVISAO_CADASTRO_DISPONIBILIZADA, TipoTransicao.REVISAO_CADASTRO_DISPONIBILIZADA, usuario);
    }

    private void disponibilizar(Subprocesso sp, SituacaoSubprocesso novaSituacao,
            TipoTransicao transicao, Usuario usuario) {

        validarRequisitosNegocioParaDisponibilizacao(sp.getCodigo());

        Unidade origem = sp.getUnidade();
        Unidade destino = origem.getUnidadeSuperior();
        if (destino == null) {
            log.warn("Unidade {} não possui superior. Usando a própria unidade como destino.", origem.getSigla());
            destino = origem;
        }

        sp.setSituacao(novaSituacao);
        sp.setDataFimEtapa1(LocalDateTime.now());
        subprocessoRepo.save(sp);

        removerAnalisesPorSubprocesso(sp.getCodigo());

        final Unidade destinoFinal = destino;
        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(transicao)
                .origem(origem)
                .destino(destinoFinal)
                .usuario(usuario)
                .build());
    }

    private void validarRequisitosNegocioParaDisponibilizacao(Long codSubprocesso) {
        validarExistenciaAtividades(codSubprocesso);

        if (!obterAtividadesSemConhecimento(codSubprocesso).isEmpty()) {
            throw new ErroValidacao("Existem atividades sem conhecimentos associados.");
        }
    }

    @Transactional
    public void devolverCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp, MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

        Unidade unidadeSubprocesso = sp.getUnidade();
        Unidade unidadeAnalise = unidadeSubprocesso.getUnidadeSuperior();
        if (unidadeAnalise == null) {
            unidadeAnalise = unidadeSubprocesso;
        }

        sp.setDataFimEtapa1(null);
        registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO)
                .tipoTransicao(TipoTransicao.CADASTRO_DEVOLVIDO)
                .tipoAnalise(TipoAnalise.CADASTRO)
                .tipoAcaoAnalise(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO)
                .unidadeAnalise(unidadeAnalise)
                .unidadeOrigemTransicao(unidadeAnalise)
                .unidadeDestinoTransicao(sp.getUnidade())
                .usuario(usuario)
                .motivoAnalise(observacoes)
                .observacoes(observacoes)
                .build());
    }

    @Transactional
    public void aceitarCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarAceiteCadastro(codSubprocesso, usuario, observacoes);
    }

    private void executarAceiteCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp, MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

        Unidade unidadeAtual = obterUnidadeLocalizacao(sp);
        Unidade unidadeDestino = unidadeAtual.getUnidadeSuperior();
        if (unidadeDestino == null) {
            unidadeDestino = unidadeAtual;
        }

        registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                .tipoTransicao(TipoTransicao.CADASTRO_ACEITO)
                .tipoAnalise(TipoAnalise.CADASTRO)
                .tipoAcaoAnalise(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                .unidadeAnalise(unidadeAtual)
                .unidadeOrigemTransicao(unidadeAtual)
                .unidadeDestinoTransicao(unidadeDestino)
                .usuario(usuario)
                .motivoAnalise(observacoes)
                .observacoes(observacoes)
                .build());
    }

    private Unidade obterUnidadeLocalizacao(Subprocesso sp) {
        if (sp.getLocalizacaoAtualCache() != null) return sp.getLocalizacaoAtualCache();
        if (sp.getCodigo() == null) {
            return sp.getUnidade();
        }
        List<Movimentacao> movs = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());
        if (movs.isEmpty()) {
            return sp.getUnidade();
        }
        Unidade destino = movs.getFirst().getUnidadeDestino();
        return (destino != null) ? destino : sp.getUnidade();
    }

    @Transactional
    public void homologarCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        executarHomologacaoCadastro(codSubprocesso, usuario, observacoes);
    }

    private void executarHomologacaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp, MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

        Unidade admin = organizacaoFacade.buscarEntidadePorSigla(SIGLA_ADMIN);
        sp.setSituacao(MAPEAMENTO_CADASTRO_HOMOLOGADO);
        subprocessoRepo.save(sp);

        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.CADASTRO_HOMOLOGADO)
                .origem(admin)
                .destino(admin)
                .usuario(usuario)
                .observacoes(observacoes)
                .build());
    }

    @Transactional
    public void devolverRevisaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp, REVISAO_CADASTRO_DISPONIBILIZADA);

        Unidade unidadeSubprocesso = sp.getUnidade();
        Unidade unidadeAnalise = unidadeSubprocesso.getUnidadeSuperior();
        if (unidadeAnalise == null) {
            unidadeAnalise = unidadeSubprocesso;
        }

        sp.setDataFimEtapa1(null);
        registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(REVISAO_CADASTRO_EM_ANDAMENTO)
                .tipoTransicao(TipoTransicao.REVISAO_CADASTRO_DEVOLVIDA)
                .tipoAnalise(TipoAnalise.CADASTRO)
                .tipoAcaoAnalise(TipoAcaoAnalise.DEVOLUCAO_REVISAO)
                .unidadeAnalise(unidadeAnalise)
                .unidadeOrigemTransicao(unidadeAnalise)
                .unidadeDestinoTransicao(sp.getUnidade())
                .usuario(usuario)
                .motivoAnalise(observacoes)
                .observacoes(observacoes)
                .build());
    }

    @Transactional
    public void aceitarRevisaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp, REVISAO_CADASTRO_DISPONIBILIZADA);

        Unidade unidadeAtual = obterUnidadeLocalizacao(sp);
        Unidade unidadeDestino = unidadeAtual.getUnidadeSuperior();
        if (unidadeDestino == null) {
            unidadeDestino = unidadeAtual;
        }

        registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(REVISAO_CADASTRO_DISPONIBILIZADA)
                .tipoTransicao(TipoTransicao.REVISAO_CADASTRO_ACEITA)
                .tipoAnalise(TipoAnalise.CADASTRO)
                .tipoAcaoAnalise(TipoAcaoAnalise.ACEITE_REVISAO)
                .unidadeAnalise(unidadeAtual)
                .unidadeOrigemTransicao(unidadeAtual)
                .unidadeDestinoTransicao(unidadeDestino)
                .usuario(usuario)
                .motivoAnalise(observacoes)
                .observacoes(observacoes)
                .build());
    }

    @Transactional
    public void homologarRevisaoCadastro(Long codSubprocesso, Usuario usuario, @Nullable String observacoes) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp, REVISAO_CADASTRO_DISPONIBILIZADA);

        var impactos = impactoMapaService.verificarImpactos(sp, usuario);
        if (impactos.temImpactos()) {
            Unidade admin = organizacaoFacade.buscarEntidadePorSigla(SIGLA_ADMIN);
            sp.setSituacao(REVISAO_CADASTRO_HOMOLOGADA);
            subprocessoRepo.save(sp);
            registrarTransicao(RegistrarTransicaoCommand.builder()
                    .sp(sp)
                    .tipo(TipoTransicao.REVISAO_CADASTRO_HOMOLOGADA)
                    .origem(admin)
                    .destino(admin)
                    .usuario(usuario)
                    .observacoes(observacoes)
                    .build());
        } else {
            sp.setSituacao(REVISAO_MAPA_HOMOLOGADO);
            subprocessoRepo.save(sp);
        }
    }

    @Transactional
    public void aceitarCadastroEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> executarAceiteCadastro(codSubprocesso, usuario,
                "De acordo com o cadastro de atividades da unidade (Em Bloco)"));
    }

    @Transactional
    public void homologarCadastroEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(
                codSubprocesso -> executarHomologacaoCadastro(codSubprocesso, usuario, "Homologação em bloco"));
    }

    // ========================================================================
    // ADMIN WORKFLOW (from SubprocessoWorkflowService)
    // ========================================================================

    @Transactional
    public void reabrirCadastro(Long codigo, String justificativa) {
        executarReabertura(codigo, justificativa, MAPEAMENTO_CADASTRO_HOMOLOGADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                TipoTransicao.CADASTRO_REABERTO, false);
    }

    @Transactional
    public void reabrirRevisaoCadastro(Long codigo, String justificativa) {
        executarReabertura(codigo, justificativa, REVISAO_CADASTRO_HOMOLOGADA, REVISAO_CADASTRO_EM_ANDAMENTO,
                TipoTransicao.REVISAO_CADASTRO_REABERTA, true);
    }

    private void executarReabertura(Long codigo, String justificativa, SituacaoSubprocesso situacaoMinima,
                                    SituacaoSubprocesso novaSituacao, TipoTransicao tipoTransicao, boolean isRevisao) {
        Subprocesso sp = buscarSubprocesso(codigo);

        validarSituacaoMinima(sp, situacaoMinima,
            "Subprocesso ainda está em fase de " + (isRevisao ? "revisão" : "cadastro") + ".");

        Unidade admin = organizacaoFacade.buscarEntidadePorSigla(SIGLA_ADMIN);
        Usuario usuario = usuarioFacade.usuarioAutenticado();

        sp.setSituacao(novaSituacao);
        sp.setDataFimEtapa1(null);
        subprocessoRepo.save(sp);

        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(tipoTransicao)
                .origem(admin)
                .destino(sp.getUnidade())
                .usuario(usuario)
                .observacoes(justificativa)
                .build());

        enviarAlertasReabertura(sp, justificativa, isRevisao);
    }

    private void enviarAlertasReabertura(Subprocesso sp, String justificativa, boolean isRevisao) {
        if (isRevisao) {
            alertaService.criarAlertaReaberturaRevisao(sp.getProcesso(), sp.getUnidade(), justificativa);
        } else {
            alertaService.criarAlertaReaberturaCadastro(sp.getProcesso(), sp.getUnidade(), justificativa);
        }
        Unidade superior = sp.getUnidade().getUnidadeSuperior();
        while (superior != null) {
            if (isRevisao) {
                alertaService.criarAlertaReaberturaRevisaoSuperior(sp.getProcesso(), superior, sp.getUnidade());
            } else {
                alertaService.criarAlertaReaberturaCadastroSuperior(sp.getProcesso(), superior, sp.getUnidade());
            }
            superior = superior.getUnidadeSuperior();
        }
    }

    @Transactional
    public void alterarDataLimite(Long codSubprocesso, LocalDate novaDataLimite) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        SituacaoSubprocesso s = sp.getSituacao();

        if (s.name().contains("CADASTRO")) {
            sp.setDataLimiteEtapa1(novaDataLimite.atStartOfDay());
        } else if (s.name().contains("MAPA")) {
            sp.setDataLimiteEtapa2(novaDataLimite.atStartOfDay());
        } else {
            sp.setDataLimiteEtapa1(novaDataLimite.atStartOfDay());
        }

        subprocessoRepo.save(sp);

        try {
            String novaDataStr = novaDataLimite.format(DATE_FORMATTER);
            String assunto = "SGC: Data limite alterada";
            String corpo = ("Prezado(a) responsável pela %s," + "%n%n" +
                    "A data limite da etapa atual no processo %s foi alterada para %s.%n")
                    .formatted(sp.getUnidade().getSigla(), sp.getProcesso().getDescricao(), novaDataStr);

            emailService.enviarEmail(sp.getUnidade().getSigla(), assunto, corpo);

            int etapa = s.name().contains("MAPA") ? 2 : 1;
            alertaService.criarAlertaAlteracaoDataLimite(sp.getProcesso(), sp.getUnidade(), novaDataStr, etapa);
        } catch (Exception e) {
            log.error("Erro ao enviar notificações de alteração de prazo: {}", e.getMessage());
        }
    }

    @Transactional
    public void atualizarParaEmAndamento(Long mapaCodigo) {
        var subprocesso = repo.buscar(Subprocesso.class, "mapa.codigo", mapaCodigo);
        if (subprocesso.getSituacao() == NAO_INICIADO) {
            var tipoProcesso = subprocesso.getProcesso().getTipo();
            if (tipoProcesso == TipoProcesso.MAPEAMENTO) {
                log.debug("Atualizando subprocesso {} p/ MAPEAMENTO_CADASTRO_EM_ANDAMENTO", subprocesso.getCodigo());
                subprocesso.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
                subprocessoRepo.save(subprocesso);
            } else if (tipoProcesso == TipoProcesso.REVISAO) {
                log.debug("Atualizando subprocesso {} p/ REVISAO_CADASTRO_EM_ANDAMENTO", subprocesso.getCodigo());
                subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
                subprocessoRepo.save(subprocesso);
            }
        }
    }

    @Transactional
    public void registrarMovimentacaoLembrete(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        Usuario usuario = usuarioFacade.usuarioAutenticado();
        var unidadeAdmin = organizacaoFacade.buscarEntidadePorSigla(SIGLA_ADMIN);

        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(unidadeAdmin)
                .unidadeDestino(subprocesso.getUnidade())
                .descricao("Lembrete de prazo enviado")
                .usuario(usuario)
                .build());
        subprocesso.setLocalizacaoAtualCache(subprocesso.getUnidade());
    }

    public List<Subprocesso> listarSubprocessosHomologados() {
        return subprocessoRepo.findBySituacao(REVISAO_CADASTRO_HOMOLOGADA);
    }

    // ========================================================================
    // FACTORY (from SubprocessoWorkflowService)
    // ========================================================================

    public void criarParaMapeamento(Processo processo, Collection<Unidade> unidades, Unidade unidadeOrigem, Usuario usuario) {
        List<Unidade> unidadesElegiveis = unidades.stream()
                .filter(u -> u.getTipo() == TipoUnidade.OPERACIONAL
                              || u.getTipo() == TipoUnidade.INTEROPERACIONAL
                              || u.getTipo() == TipoUnidade.RAIZ)
                .toList();

        if (unidadesElegiveis.isEmpty()) return;

        List<Subprocesso> subprocessos = unidadesElegiveis.stream()
                .map(unidade -> Subprocesso.builder()
                        .processo(processo)
                        .unidade(unidade)
                        .mapa(null)
                        .situacao(NAO_INICIADO)
                        .dataLimiteEtapa1(processo.getDataLimite())
                        .build())
                .map(Subprocesso.class::cast)
                .toList();

        List<Subprocesso> subprocessosSalvos = subprocessoRepo.saveAll(subprocessos);
        List<Mapa> mapas = subprocessosSalvos.stream()
                .<Mapa>map(sp -> Mapa.builder()
                        .subprocesso(sp)
                        .build())
                .toList();

        List<Mapa> mapasSalvos = mapaManutencaoService.salvarMapas(mapas);
        for (int i = 0; i < subprocessosSalvos.size(); i++) {
            subprocessosSalvos.get(i).setMapa(mapasSalvos.get(i));
        }

        List<Movimentacao> movimentacoes = new ArrayList<>();
        for (Subprocesso sp : subprocessosSalvos) {
            movimentacoes.add(Movimentacao.builder()
                    .subprocesso(sp)
                    .unidadeOrigem(unidadeOrigem)
                    .unidadeDestino(sp.getUnidade())
                    .usuario(usuario)
                    .descricao("Processo iniciado")
                    .build());
            sp.setLocalizacaoAtualCache(sp.getUnidade());
        }
        movimentacaoRepo.saveAll(movimentacoes);
    }

    public void criarParaRevisao(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa, Unidade unidadeOrigem, Usuario usuario) {
        Long codMapaVigente = unidadeMapa.getMapaVigente().getCodigo();

        Subprocesso subprocesso = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(NAO_INICIADO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build();
        Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);

        Mapa mapaCopiado = copiaMapaService.copiarMapaParaUnidade(codMapaVigente);
        mapaCopiado.setSubprocesso(subprocessoSalvo);
        Mapa mapaSalvo = mapaManutencaoService.salvarMapa(mapaCopiado);
        subprocessoSalvo.setMapa(mapaSalvo);

        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocessoSalvo)
                .unidadeOrigem(unidadeOrigem)
                .unidadeDestino(unidade)
                .usuario(usuario)
                .descricao("Processo de revisão iniciado")
                .build());

        subprocessoSalvo.setLocalizacaoAtualCache(unidade);
        log.info("Subprocesso criado para unidade {}", unidade.getSigla());
    }

    public void criarParaDiagnostico(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa, Unidade unidadeOrigem, Usuario usuario) {
        Long codMapaVigente = unidadeMapa.getMapaVigente().getCodigo();
        Subprocesso subprocesso = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .mapa(null)
                .situacao(DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build();

        Mapa mapaCopiado = copiaMapaService.copiarMapaParaUnidade(codMapaVigente);
        Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);
        mapaCopiado.setSubprocesso(subprocessoSalvo);

        Mapa mapaSalvo = mapaManutencaoService.salvarMapa(mapaCopiado);
        subprocessoSalvo.setMapa(mapaSalvo);

        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocessoSalvo)
                .unidadeOrigem(unidadeOrigem)
                .unidadeDestino(unidade)
                .usuario(usuario)
                .descricao("Processo de diagnóstico iniciado")
                .build());
        subprocessoSalvo.setLocalizacaoAtualCache(unidade);
        log.info("Subprocesso {} para diagnóstico criado para unidade {}", subprocessoSalvo.getCodigo(), unidade.getSigla());
    }

    // ========================================================================
    // MAPA WORKFLOW (from SubprocessoMapaWorkflowService)
    // ========================================================================

    public Mapa salvarMapaSubprocesso(Long codSubprocesso, SalvarMapaRequest request) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        Long codMapa = subprocesso.getMapa().getCodigo();
        boolean eraVazio = mapaManutencaoService.buscarCompetenciasPorCodMapa(codMapa).isEmpty();
        boolean temNovasCompetencias = !request.competencias().isEmpty();

        Mapa mapa = mapaSalvamentoService.salvarMapaCompleto(codMapa, request);
        if (eraVazio && temNovasCompetencias) {
            if (subprocesso.getSituacao() == MAPEAMENTO_CADASTRO_HOMOLOGADO) {
                subprocesso.setSituacao(MAPEAMENTO_MAPA_CRIADO);
                subprocessoRepo.save(subprocesso);
            } else if (subprocesso.getSituacao() == REVISAO_CADASTRO_HOMOLOGADA) {
                subprocesso.setSituacao(REVISAO_MAPA_AJUSTADO);
                subprocessoRepo.save(subprocesso);
            }
        }

        return mapa;
    }

    public Mapa adicionarCompetencia(Long codSubprocesso, CompetenciaRequest request) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        Mapa mapa = subprocesso.getMapa();

        Long codMapa = mapa.getCodigo();
        boolean eraVazio = mapaManutencaoService.buscarCompetenciasPorCodMapa(codMapa).isEmpty();

        mapaManutencaoService.criarCompetenciaComAtividades(
                mapa, request.descricao(), request.atividadesIds());

        // Alterar situação para MAPA_CRIADO/AJUSTADO se era vazio e passou a ter competências
        if (eraVazio) {
            if (subprocesso.getSituacao() == MAPEAMENTO_CADASTRO_HOMOLOGADO) {
                subprocesso.setSituacao(MAPEAMENTO_MAPA_CRIADO);
                subprocessoRepo.save(subprocesso);
            } else if (subprocesso.getSituacao() == REVISAO_CADASTRO_HOMOLOGADA) {
                subprocesso.setSituacao(REVISAO_MAPA_AJUSTADO);
                subprocessoRepo.save(subprocesso);
            }
        }

        return mapaManutencaoService.buscarMapaPorCodigo(mapa.getCodigo());
    }

    public Mapa atualizarCompetencia(
            Long codSubprocesso,
            Long codCompetencia,
            CompetenciaRequest request) {

        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        mapaManutencaoService.atualizarCompetencia(codCompetencia, request.descricao(), request.atividadesIds());

        Mapa mapa = subprocesso.getMapa();
        return mapaManutencaoService.buscarMapaPorCodigo(mapa.getCodigo());
    }

    public Mapa removerCompetencia(Long codSubprocesso, Long codCompetencia) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);

        Long codMapa = subprocesso.getMapa().getCodigo();
        mapaManutencaoService.removerCompetencia(codCompetencia);

        // Se o mapa ficou vazio, voltar para situação anterior
        boolean ficouVazio = mapaManutencaoService.buscarCompetenciasPorCodMapa(codMapa).isEmpty();
        if (ficouVazio) {
            if (subprocesso.getSituacao() == MAPEAMENTO_MAPA_CRIADO) {
                subprocesso.setSituacao(MAPEAMENTO_CADASTRO_HOMOLOGADO);
                subprocessoRepo.save(subprocesso);
                log.info("Situação do subprocesso {} alterada para CADASTRO_HOMOLOGADO (mapa ficou vazio)", codSubprocesso);
            } else if (subprocesso.getSituacao() == REVISAO_MAPA_AJUSTADO) {
                subprocesso.setSituacao(REVISAO_CADASTRO_HOMOLOGADA);
                subprocessoRepo.save(subprocesso);
                log.info("Situação do subprocesso {} alterada para REVISAO_CADASTRO_HOMOLOGADA (mapa ficou vazio)", codSubprocesso);
            }
        }

        return mapaManutencaoService.buscarMapaPorCodigo(subprocesso.getMapa().getCodigo());
    }

    private Subprocesso getSubprocessoParaEdicao(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);

        validarSituacaoPermitida(subprocesso,
            "Mapa só pode ser editado com cadastro homologado ou mapa criado. Situação atual: %s".formatted(subprocesso.getSituacao()),
            MAPEAMENTO_CADASTRO_HOMOLOGADO,
            MAPEAMENTO_MAPA_CRIADO,
            REVISAO_CADASTRO_HOMOLOGADA,
            REVISAO_MAPA_AJUSTADO);

        return subprocesso;
    }

    @Transactional
    public void disponibilizarMapa(Long codSubprocesso, DisponibilizarMapaRequest request, Usuario usuario) {
        executarDisponibilizacaoMapa(codSubprocesso, request, usuario);
    }

    private void executarDisponibilizacaoMapa(Long codSubprocesso, DisponibilizarMapaRequest request, Usuario usuario) {
        Subprocesso sp = getSubprocessoParaEdicao(codSubprocesso);
        validarSituacaoPermitida(sp,
                MAPEAMENTO_CADASTRO_HOMOLOGADO, MAPEAMENTO_MAPA_CRIADO, MAPEAMENTO_MAPA_COM_SUGESTOES,
                REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO, REVISAO_MAPA_COM_SUGESTOES);

        validarMapaParaDisponibilizacao(sp);
        validarAssociacoesMapa(sp.getMapa().getCodigo());

        sp.getMapa().setSugestoes(null);
        removerAnalisesPorSubprocesso(codSubprocesso);

        if (StringUtils.hasText(request.observacoes())) {
            sp.getMapa().setSugestoes(request.observacoes());
        }

        sp.setSituacao(SITUACAO_MAPA_DISPONIBILIZADO.get(sp.getProcesso().getTipo()));

        sp.setDataLimiteEtapa2(request.dataLimite().atStartOfDay());
        sp.setDataFimEtapa1(LocalDateTime.now());
        subprocessoRepo.save(sp);

        Unidade admin = organizacaoFacade.buscarEntidadePorSigla(SIGLA_ADMIN);

        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.MAPA_DISPONIBILIZADO)
                .origem(admin)
                .destino(sp.getUnidade())
                .usuario(usuario)
                .observacoes(request.observacoes())
                .build());
    }

    private void validarMapaParaDisponibilizacao(Subprocesso subprocesso) {
        Long codMapa = subprocesso.getMapa().getCodigo();
        var competencias = mapaManutencaoService.buscarCompetenciasPorCodMapa(codMapa);

        if (competencias.stream().anyMatch(c -> c.getAtividades().isEmpty())) {
            throw new ErroValidacao("Todas as competências devem estar associadas a pelo menos uma atividade.");
        }

        var atividadesDoSubprocesso = mapaManutencaoService.buscarAtividadesPorMapaCodigo(codMapa);
        var atividadesAssociadas = competencias.stream()
                .flatMap(c -> c.getAtividades().stream())
                .map(Atividade::getCodigo)
                .collect(Collectors.toSet());

        var atividadesNaoAssociadas = atividadesDoSubprocesso.stream()
                .filter(a -> !atividadesAssociadas.contains(a.getCodigo()))
                .toList();

        if (!atividadesNaoAssociadas.isEmpty()) {
            String nomesAtividades = atividadesNaoAssociadas.stream()
                    .map(Atividade::getDescricao)
                    .collect(Collectors.joining(", "));

            throw new ErroValidacao(
                    "Todas as atividades devem estar associadas a pelo menos uma competência.%nAtividades pendentes: %s"
                            .formatted(nomesAtividades));
        }
    }

    @Transactional
    public void apresentarSugestoes(Long codSubprocesso, @Nullable String sugestoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp, MAPEAMENTO_MAPA_DISPONIBILIZADO, REVISAO_MAPA_DISPONIBILIZADO);

        sp.getMapa().setSugestoes(sugestoes);
        sp.setSituacao(SITUACAO_MAPA_COM_SUGESTOES.get(sp.getProcesso().getTipo()));

        sp.setDataFimEtapa2(LocalDateTime.now());
        subprocessoRepo.save(sp);

        removerAnalisesPorSubprocesso(sp.getCodigo());

        Unidade destino = sp.getUnidade().getUnidadeSuperior();
        if (destino == null) {
            destino = sp.getUnidade();
        }

        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.MAPA_SUGESTOES_APRESENTADAS)
                .origem(sp.getUnidade())
                .destino(destino)
                .usuario(usuario)
                .observacoes(sugestoes)
                .build());
    }

    @Transactional
    public void validarMapa(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp, MAPEAMENTO_MAPA_DISPONIBILIZADO, REVISAO_MAPA_DISPONIBILIZADO);

        sp.setSituacao(SITUACAO_MAPA_VALIDADO.get(sp.getProcesso().getTipo()));

        sp.setDataFimEtapa2(LocalDateTime.now());
        subprocessoRepo.save(sp);

        Unidade destino = sp.getUnidade().getUnidadeSuperior();
        if (destino == null) {
            destino = sp.getUnidade();
        }

        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.MAPA_VALIDADO)
                .origem(sp.getUnidade())
                .destino(destino)
                .usuario(usuario)
                .build());
    }

    @Transactional
    public void devolverValidacao(Long codSubprocesso, @Nullable String justificativa, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp,
                MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_VALIDADO,
                REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_VALIDADO);

        SituacaoSubprocesso novaSituacao = SITUACAO_MAPA_DISPONIBILIZADO.get(sp.getProcesso().getTipo());
        sp.setDataFimEtapa2(null);

        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior();
        if (unidadeSuperior == null) {
            unidadeSuperior = sp.getUnidade();
        }

        registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
                .sp(sp)
                .novaSituacao(novaSituacao)
                .tipoTransicao(TipoTransicao.MAPA_VALIDACAO_DEVOLVIDA)
                .tipoAnalise(TipoAnalise.VALIDACAO)
                .tipoAcaoAnalise(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO)
                .unidadeAnalise(unidadeSuperior)
                .unidadeOrigemTransicao(unidadeSuperior)
                .unidadeDestinoTransicao(sp.getUnidade())
                .usuario(usuario)
                .motivoAnalise(justificativa)
                .observacoes(justificativa)
                .build());
    }

    @Transactional
    public void aceitarValidacao(Long codSubprocesso, Usuario usuario) {
        executarAceiteValidacao(codSubprocesso, usuario);
    }

    private void executarAceiteValidacao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp,
                MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_VALIDADO,
                REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_VALIDADO);

        Unidade unidadeAtual = obterUnidadeLocalizacao(sp);
        Unidade proximaUnidade = unidadeAtual.getUnidadeSuperior();

        // Se não tem próxima unidade (é o topo ou estrutura rasa), homologar direto
        if (proximaUnidade == null) {
            // Caso especial: Fim da cadeia de validação (Homologação Implícita?)
            Unidade sup = unidadeAtual.getUnidadeSuperior();
            String siglaUnidade = sup != null ? sup.getSigla() : unidadeAtual.getSigla();

            criarAnalise(sp, CriarAnaliseCommand.builder()
                    .codSubprocesso(codSubprocesso)
                    .observacoes("Aceite da validação")
                    .tipo(TipoAnalise.VALIDACAO)
                    .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                    .siglaUnidade(siglaUnidade)
                    .tituloUsuario(usuario.getTituloEleitoral())
                    .motivo(null)
                    .build());

            sp.setSituacao(SITUACAO_MAPA_HOMOLOGADO.get(sp.getProcesso().getTipo()));
            subprocessoRepo.save(sp);
        } else {
            SituacaoSubprocesso novaSituacao = SITUACAO_MAPA_VALIDADO.get(sp.getProcesso().getTipo());
            registrarAnaliseETransicao(RegistrarWorkflowCommand.builder()
                    .sp(sp)
                    .novaSituacao(novaSituacao)
                    .tipoTransicao(TipoTransicao.MAPA_VALIDACAO_ACEITA)
                    .tipoAnalise(TipoAnalise.VALIDACAO)
                    .tipoAcaoAnalise(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                    .unidadeAnalise(unidadeAtual)
                    .unidadeOrigemTransicao(unidadeAtual)
                    .unidadeDestinoTransicao(proximaUnidade)
                    .usuario(usuario)
                    .motivoAnalise("Aceite da validação")
                    .build());
        }
    }

    @Transactional
    public void homologarValidacao(Long codSubprocesso, Usuario usuario) {
        executarHomologacaoValidacao(codSubprocesso, usuario);
    }

    private void executarHomologacaoValidacao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp,
                MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_VALIDADO,
                REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_VALIDADO);

        sp.setSituacao(SITUACAO_MAPA_HOMOLOGADO.get(sp.getProcesso().getTipo()));
        subprocessoRepo.save(sp);

        Unidade admin = organizacaoFacade.buscarEntidadePorSigla(SIGLA_ADMIN);
        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.MAPA_HOMOLOGADO)
                .origem(admin)
                .destino(admin)
                .usuario(usuario)
                .build());
    }

    @Transactional
    public void submeterMapaAjustado(Long codSubprocesso, SubmeterMapaAjustadoRequest request, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        validarSituacaoPermitida(sp, REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO);
        validarAssociacoesMapa(sp.getMapa().getCodigo());

        sp.setSituacao(SITUACAO_MAPA_DISPONIBILIZADO.get(sp.getProcesso().getTipo()));
        sp.setDataFimEtapa1(LocalDateTime.now());

        if (request.dataLimiteEtapa2() != null) {
            sp.setDataLimiteEtapa2(request.dataLimiteEtapa2());
        }

        subprocessoRepo.save(sp);

        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(TipoTransicao.MAPA_DISPONIBILIZADO)
                .origem(sp.getUnidade())
                .destino(sp.getUnidade())
                .usuario(usuario)
                .observacoes(request.justificativa())
                .build());
    }

    @Transactional
    public void disponibilizarMapaEmBloco(List<Long> subprocessoCodigos,
            DisponibilizarMapaRequest request, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> executarDisponibilizacaoMapa(codSubprocesso, request, usuario));
    }

    @Transactional
    public void aceitarValidacaoEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> executarAceiteValidacao(codSubprocesso, usuario));
    }

    @Transactional
    public void homologarValidacaoEmBloco(List<Long> subprocessoCodigos, Usuario usuario) {
        subprocessoCodigos.forEach(codSubprocesso -> executarHomologacaoValidacao(codSubprocesso, usuario));
    }

    // ========================================================================
    // AJUSTE MAPA (from SubprocessoAjusteMapaService)
    // ========================================================================

    @Transactional
    public void salvarAjustesMapa(Long codSubprocesso, List<CompetenciaAjusteDto> competencias) {
        log.info("Salvando ajustes no mapa do subprocesso {} ({} competências)", codSubprocesso, competencias.size());
        Subprocesso sp = repo.buscar(Subprocesso.class, codSubprocesso);

        validarSituacaoParaAjuste(sp);
        atualizarDescricoesAtividades(competencias);
        atualizarCompetenciasEAssociacoes(competencias);

        sp.setSituacao(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        subprocessoRepo.save(sp);
    }

    @Transactional(readOnly = true)
    public MapaAjusteDto obterMapaParaAjuste(Long codSubprocesso) {
        log.info("Recuperando mapa para ajustes do subprocesso {}", codSubprocesso);
        Subprocesso sp = subprocessoRepo.findByIdWithMapaAndAtividades(codSubprocesso).orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));
        Long codMapa = sp.getMapa().getCodigo();

        Analise analise = listarAnalisesPorSubprocesso(codSubprocesso, TipoAnalise.VALIDACAO)
                .stream()
                .findFirst()
                .orElse(null);

        List<Competencia> competencias = mapaManutencaoService.buscarCompetenciasPorCodMapaSemRelacionamentos(codMapa);
        List<Atividade> atividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoSemRelacionamentos(codMapa);
        List<Conhecimento> conhecimentos = mapaManutencaoService.listarConhecimentosPorMapa(codMapa);
        Map<Long, Set<Long>> associacoes = mapaManutencaoService.buscarIdsAssociacoesCompetenciaAtividade(codMapa);

        return mapaAjusteMapper.toDto(sp, analise, competencias, atividades, conhecimentos, associacoes);
    }

    private void validarSituacaoParaAjuste(Subprocesso sp) {
        if (sp.getSituacao() != SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA
                && sp.getSituacao() != SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO) {
            throw new ErroMapaEmSituacaoInvalida(
                    "Ajustes no mapa só podem ser feitos em estados específicos. "
                            + "Situação atual: %s".formatted(sp.getSituacao()));
        }
    }

    private void atualizarDescricoesAtividades(List<CompetenciaAjusteDto> competencias) {
        Map<Long, String> atividadeDescricoes = new HashMap<>();
        for (CompetenciaAjusteDto compDto : competencias) {
            for (AtividadeAjusteDto ativDto : compDto.getAtividades()) {
                atividadeDescricoes.put(ativDto.codAtividade(), ativDto.nome());
            }
        }
        if (!atividadeDescricoes.isEmpty()) {
            mapaManutencaoService.atualizarDescricoesAtividadeEmLote(atividadeDescricoes);
        }
    }

    private void atualizarCompetenciasEAssociacoes(List<CompetenciaAjusteDto> competencias) {
        List<Long> competenciaIds = competencias.stream()
                .map(CompetenciaAjusteDto::getCodCompetencia)
                .toList();

        Map<Long, Competencia> mapaCompetencias = mapaManutencaoService.buscarCompetenciasPorCodigos(competenciaIds)
                .stream()
                .collect(Collectors.toMap(Competencia::getCodigo, Function.identity()));

        List<Long> todasAtividadesIds = competencias.stream()
                .flatMap(c -> c.getAtividades().stream())
                .map(AtividadeAjusteDto::codAtividade)
                .distinct()
                .toList();

        Map<Long, Atividade> mapaAtividades = mapaManutencaoService.buscarAtividadesPorCodigos(todasAtividadesIds)
                .stream()
                .collect(Collectors.toMap(Atividade::getCodigo, Function.identity()));

        List<Competencia> competenciasParaSalvar = new ArrayList<>();
        for (CompetenciaAjusteDto compDto : competencias) {
            Competencia competencia = mapaCompetencias.get(compDto.getCodCompetencia());
            if (competencia != null) {
                competencia.setDescricao(compDto.getNome());

                Set<Atividade> atividadesSet = new HashSet<>();
                for (AtividadeAjusteDto ativDto : compDto.getAtividades()) {
                    Atividade ativ = mapaAtividades.get(ativDto.codAtividade());
                    atividadesSet.add(ativ);
                }
                competencia.setAtividades(atividadesSet);
                competenciasParaSalvar.add(competencia);
            }
        }
        mapaManutencaoService.salvarTodasCompetencias(competenciasParaSalvar);
    }

    // ========================================================================
    // CONTEXTO (from SubprocessoContextoService)
    // ========================================================================

    public SubprocessoDetalheResponse obterDetalhes(Long codigo, Usuario usuarioAutenticado) {
        Subprocesso sp = subprocessoRepo.findByIdWithMapaAndAtividades(codigo).orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codigo));
        return obterDetalhes(sp, usuarioAutenticado);
    }

    public SubprocessoDetalheResponse obterDetalhes(Subprocesso sp, Usuario usuarioAutenticado) {
        String siglaUnidade = sp.getUnidade().getSigla();
        String localizacaoAtual = siglaUnidade;

        Usuario responsavel = usuarioFacade.buscarResponsavelAtual(siglaUnidade);
        Usuario titular = null;
        try {
            titular = usuarioFacade.buscarPorLogin(sp.getUnidade().getTituloTitular());
        } catch (Exception e) {
            log.warn("Erro ao buscar titular da unidade {}: {}", siglaUnidade, e.getMessage());
        }

        List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());
        if (!movimentacoes.isEmpty()) {
            Unidade destino = movimentacoes.getFirst().getUnidadeDestino();
            if (destino != null) {
                localizacaoAtual = destino.getSigla();
            }
        }

        PermissoesSubprocessoDto permissoes = obterPermissoesUI(sp, usuarioAutenticado);

        return SubprocessoDetalheResponse.builder()
                .subprocesso(sp)
                .responsavel(responsavel)
                .titular(titular)
                .movimentacoes(movimentacoes)
                .localizacaoAtual(localizacaoAtual)
                .permissoes(permissoes)
                .build();
    }

    public ContextoEdicaoResponse obterContextoEdicao(Long codSubprocesso) {
        Usuario usuario = usuarioFacade.usuarioAutenticado();
        Subprocesso subprocesso = subprocessoRepo.findByIdWithMapaAndAtividades(codSubprocesso).orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));
        SubprocessoDetalheResponse detalhes = obterDetalhes(subprocesso, usuario);

        Unidade unidade = subprocesso.getUnidade();
        List<AtividadeDto> atividades = listarAtividadesSubprocesso(codSubprocesso);

        return new ContextoEdicaoResponse(
                unidade,
                subprocesso,
                detalhes,
                mapaManutencaoService.buscarMapaPorCodigo(subprocesso.getMapa().getCodigo()),
                atividades
        );
    }

    public PermissoesSubprocessoDto obterPermissoesUI(Subprocesso sp, Usuario usuario) {
        if (sp.getProcesso() != null && sp.getProcesso().getSituacao() == SituacaoProcesso.FINALIZADO) {
            return PermissoesSubprocessoDto.builder().build(); // Tudo false
        }

        Unidade localizacao = obterUnidadeLocalizacao(sp);
        boolean isNaUnidade = Objects.equals(usuario.getUnidadeAtivaCodigo(), localizacao.getCodigo());
        Perfil perfil = usuario.getPerfilAtivo();
        SituacaoSubprocesso situacao = sp.getSituacao();

        boolean isChefe = perfil == Perfil.CHEFE;
        boolean isGestor = perfil == Perfil.GESTOR;
        boolean isAdmin = perfil == Perfil.ADMIN;

        return PermissoesSubprocessoDto.builder()
            .podeEditarCadastro(isNaUnidade && isChefe && Set.of(NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO).contains(situacao))
            .podeDisponibilizarCadastro(isNaUnidade && isChefe && Set.of(MAPEAMENTO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO).contains(situacao))
            .podeDevolverCadastro(isNaUnidade && (isGestor || isAdmin) && Set.of(MAPEAMENTO_CADASTRO_DISPONIBILIZADO, REVISAO_CADASTRO_DISPONIBILIZADA).contains(situacao))
            .podeAceitarCadastro(isNaUnidade && isGestor && Set.of(MAPEAMENTO_CADASTRO_DISPONIBILIZADO, REVISAO_CADASTRO_DISPONIBILIZADA).contains(situacao))
            .podeHomologarCadastro(isNaUnidade && isAdmin && Set.of(MAPEAMENTO_CADASTRO_DISPONIBILIZADO, REVISAO_CADASTRO_DISPONIBILIZADA).contains(situacao))
            .podeEditarMapa(isNaUnidade && isAdmin && Set.of(
                    NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, MAPEAMENTO_CADASTRO_HOMOLOGADO,
                    MAPEAMENTO_MAPA_CRIADO, MAPEAMENTO_MAPA_COM_SUGESTOES,
                    REVISAO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_HOMOLOGADA,
                    REVISAO_MAPA_AJUSTADO, REVISAO_MAPA_COM_SUGESTOES,
                    DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO).contains(situacao))
            .podeDisponibilizarMapa(isNaUnidade && isAdmin && Set.of(
                    MAPEAMENTO_CADASTRO_HOMOLOGADO, MAPEAMENTO_MAPA_CRIADO, MAPEAMENTO_MAPA_COM_SUGESTOES,
                    REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO, REVISAO_MAPA_COM_SUGESTOES).contains(situacao))
            .podeValidarMapa(isNaUnidade && isChefe && Set.of(MAPEAMENTO_MAPA_DISPONIBILIZADO, REVISAO_MAPA_DISPONIBILIZADO).contains(situacao))
            .podeApresentarSugestoes(isNaUnidade && isChefe && Set.of(MAPEAMENTO_MAPA_DISPONIBILIZADO, REVISAO_MAPA_DISPONIBILIZADO).contains(situacao))
            .podeDevolverMapa(isNaUnidade && (isGestor || isAdmin) && Set.of(
                    MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_VALIDADO,
                    REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_VALIDADO).contains(situacao))
            .podeAceitarMapa(isNaUnidade && isGestor && Set.of(
                    MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_VALIDADO,
                    REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_VALIDADO).contains(situacao))
            .podeHomologarMapa(isNaUnidade && isAdmin && Set.of(
                    MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_VALIDADO,
                    REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_VALIDADO).contains(situacao))
            .podeVisualizarImpacto(isAdmin || (isNaUnidade && (isChefe || isGestor) && Set.of(
                    NAO_INICIADO, REVISAO_CADASTRO_EM_ANDAMENTO,
                    REVISAO_CADASTRO_DISPONIBILIZADA, REVISAO_CADASTRO_HOMOLOGADA,
                    REVISAO_MAPA_AJUSTADO).contains(situacao)))
            .podeAlterarDataLimite(isAdmin)
            .podeReabrirCadastro(isAdmin)
            .podeReabrirRevisao(isAdmin)
            .podeEnviarLembrete(isAdmin || isGestor)
            .build();
    }

    // ========================================================================
    // ATIVIDADE (from SubprocessoAtividadeService)
    // ========================================================================

    @Transactional
    public void importarAtividades(Long codSubprocessoDestino, Long codSubprocessoOrigem) {
        final Subprocesso spDestino = repo.buscar(Subprocesso.class, codSubprocessoDestino);
        Usuario usuario = usuarioFacade.usuarioAutenticado();

        if (!permissionEvaluator.checkPermission(usuario, spDestino, "EDITAR_CADASTRO")) {
             throw new ErroAcessoNegado("Usuário não tem permissão para importar atividades.");
        }
        validarSituacaoParaImportacao(spDestino);

        Subprocesso spOrigem = repo.buscar(Subprocesso.class, codSubprocessoOrigem);
        if (!permissionEvaluator.checkPermission(usuario, spOrigem, "CONSULTAR_PARA_IMPORTACAO")) {
            throw new ErroAcessoNegado("Usuário não tem permissão para consultar o subprocesso de origem.");
        }

        copiaMapaService.importarAtividadesDeOutroMapa(
                spOrigem.getMapa().getCodigo(),
                spDestino.getMapa().getCodigo());

        if (spDestino.getSituacao() == SituacaoSubprocesso.NAO_INICIADO) {
            var tipoProcesso = spDestino.getProcesso().getTipo();

            switch (tipoProcesso) {
                case MAPEAMENTO -> spDestino.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
                case REVISAO -> spDestino.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
                default -> log.debug("Tipo de processo {} não requer atualização automática de situação.", tipoProcesso);
            }
            subprocessoRepo.save(spDestino);
        }

        final Unidade unidadeOrigem = spOrigem.getUnidade();
        String descMovimentacao = String.format("Importação de atividades do subprocesso #%d (Unidade: %s)",
                spOrigem.getCodigo(),
                unidadeOrigem.getSigla());

        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(spDestino)
                .unidadeOrigem(unidadeOrigem)
                .unidadeDestino(spDestino.getUnidade())
                .descricao(descMovimentacao)
                .usuario(usuario)
                .build());
        spDestino.setLocalizacaoAtualCache(spDestino.getUnidade());
    }

    @Transactional(readOnly = true)
    public List<AtividadeDto> listarAtividadesSubprocesso(Long codSubprocesso) {
        Subprocesso subprocesso = subprocessoRepo.findByIdWithMapaAndAtividades(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));
        List<Atividade> todasAtividades = mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(
                subprocesso.getMapa().getCodigo());

        return todasAtividades.stream().map(this::mapAtividadeToDto).toList();
    }

    private void validarSituacaoParaImportacao(Subprocesso sp) {
        if (!SITUACOES_PERMITIDAS_IMPORTACAO.contains(sp.getSituacao())) {
            throw new ErroValidacao(
                "Situação do subprocesso não permite importação. Situação atual: %s"
                    .formatted(sp.getSituacao()));
        }
    }

    private AtividadeDto mapAtividadeToDto(Atividade atividade) {
        return AtividadeDto.builder()
                .codigo(atividade.getCodigo())
                .descricao(atividade.getDescricao())
                .conhecimentos(new ArrayList<>(atividade.getConhecimentos()))
                .build();
    }

    // ========================================================================
    // ANALISE (from AnaliseService)
    // ========================================================================

    @Transactional(readOnly = true)
    public List<Analise> listarAnalisesPorSubprocesso(Long codSubprocesso) {
        return analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codSubprocesso);
    }

    @Transactional(readOnly = true)
    public List<Analise> listarAnalisesPorSubprocesso(Long codSubprocesso, TipoAnalise tipo) {
         return analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codSubprocesso).stream()
                 .filter(a -> a.getTipo() == tipo)
                 .toList();
    }

    @Transactional
    public Analise salvarAnalise(Analise analise) {
        return analiseRepo.save(analise);
    }

    @Transactional
    public Analise criarAnalise(Subprocesso sp, CriarAnaliseCommand cmd) {
        var unidadeDto = organizacaoFacade.buscarPorSigla(cmd.siglaUnidade());
        Analise analise = Analise.builder()
                .subprocesso(sp)
                .dataHora(LocalDateTime.now())
                .observacoes(cmd.observacoes())
                .tipo(cmd.tipo())
                .acao(cmd.acao())
                .unidadeCodigo(unidadeDto.getCodigo())
                .usuarioTitulo(cmd.tituloUsuario())
                .motivo(cmd.motivo())
                .build();
        return salvarAnalise(analise);
    }

    public List<AnaliseHistoricoDto> listarHistoricoCadastro(Long codSubprocesso) {
        return listarAnalisesPorSubprocesso(codSubprocesso).stream()
                .filter(a -> a.getTipo() == TipoAnalise.CADASTRO)
                .map(this::paraHistoricoDto)
                .toList();
    }

    public List<AnaliseHistoricoDto> listarHistoricoValidacao(Long codSubprocesso) {
        return listarAnalisesPorSubprocesso(codSubprocesso).stream()
                .filter(a -> a.getTipo() == TipoAnalise.VALIDACAO)
                .map(this::paraHistoricoDto)
                .toList();
    }

    public AnaliseHistoricoDto paraHistoricoDto(Analise analise) {
        UnidadeDto unidade = organizacaoFacade.dtoPorCodigo(analise.getUnidadeCodigo());
        return AnaliseHistoricoDto.builder()
                .dataHora(analise.getDataHora())
                .observacoes(analise.getObservacoes())
                .acao(analise.getAcao())
                .unidadeSigla(unidade.getSigla())
                .unidadeNome(unidade.getNome())
                .analistaUsuarioTitulo(analise.getUsuarioTitulo())
                .motivo(analise.getMotivo())
                .tipo(analise.getTipo())
                .build();
    }

    @Transactional
    public void removerAnalisesPorSubprocesso(Long codSubprocesso) {
        List<Analise> analises = analiseRepo.findBySubprocessoCodigo(codSubprocesso);
        if (!analises.isEmpty()) {
            analiseRepo.deleteAll(analises);
        }
    }

    // ========================================================================
    // TRANSICAO (from SubprocessoTransicaoService)
    // ========================================================================

    @Transactional
    public void registrarTransicao(RegistrarTransicaoCommand cmd) {
        Usuario usuario = cmd.usuario() != null ? cmd.usuario() : usuarioFacade.usuarioAutenticado();

        // 1. Salvar movimentação
        Movimentacao movimentacao = Movimentacao.builder()
                .subprocesso(cmd.sp())
                .unidadeOrigem(cmd.origem())
                .unidadeDestino(cmd.destino())
                .descricao(cmd.tipo().getDescricaoMovimentacao())
                .usuario(usuario)
                .build();
        movimentacaoRepo.save(movimentacao);

        cmd.sp().setLocalizacaoAtualCache(cmd.destino() != null ? cmd.destino() : cmd.sp().getUnidade());

        // 2. Notificar
        notificarTransicao(cmd.sp(), cmd.tipo(), cmd.origem(), cmd.destino(), cmd.observacoes());
    }

    @Transactional
    public void registrarAnaliseETransicao(RegistrarWorkflowCommand cmd) {
        Subprocesso sp = cmd.sp();

        criarAnalise(
                sp,
                CriarAnaliseCommand.builder()
                        .codSubprocesso(sp.getCodigo())
                        .observacoes(cmd.observacoes())
                        .tipo(cmd.tipoAnalise())
                        .acao(cmd.tipoAcaoAnalise())
                        .siglaUnidade(cmd.unidadeAnalise().getSigla())
                        .tituloUsuario(cmd.usuario().getTituloEleitoral())
                        .motivo(cmd.motivoAnalise())
                        .build());

        sp.setSituacao(cmd.novaSituacao());

        registrarTransicao(RegistrarTransicaoCommand.builder()
                .sp(sp)
                .tipo(cmd.tipoTransicao())
                .origem(cmd.unidadeOrigemTransicao())
                .destino(cmd.unidadeDestinoTransicao())
                .usuario(cmd.usuario())
                .observacoes(cmd.observacoes())
                .build());

        log.debug("{} -> {}", cmd.novaSituacao(), cmd.tipoTransicao());
    }

    // ========================================================================
    // NOTIFICATION (from SubprocessoEmailService)
    // ========================================================================

    private void notificarTransicao(Subprocesso sp, TipoTransicao tipo,
                                     Unidade origem,
                                     Unidade destino,
                                     String observacoes) {
        try {
            if (tipo.geraAlerta()) {
                String sigla = sp.getUnidade().getSigla();
                String descricao = tipo.formatarAlerta(sigla);
                alertaService.criarAlertaTransicao(sp.getProcesso(), descricao, origem, destino);
            }

            if (tipo.enviaEmail()) {
                notificarMovimentacaoEmail(sp, tipo, origem, destino, observacoes);
            }
        } catch (Exception e) {
            log.error("Falha ao enviar notificação de transição {}: {}", tipo, e.getMessage(), e);
        }
    }

    private void notificarMovimentacaoEmail(Subprocesso sp, TipoTransicao tipo,
                                            Unidade unidadeOrigem, Unidade unidadeDestino,
                                            String observacoes) {
        if (!tipo.enviaEmail())
            return;

        try {
            Map<String, Object> variaveis = criarVariaveisTemplateDireto(sp, unidadeOrigem, unidadeDestino, observacoes);

            // 1. Notificação Operacional
            enviarNotificacaoOperacional(sp, tipo, unidadeDestino, variaveis);

            // 2. Notificação de Acompanhamento
            if (tipo.enviaEmailSuperior()) {
                enviarNotificacaoAcompanhamentoSuperior(unidadeOrigem, sp, tipo, variaveis, unidadeDestino);
            }

        } catch (Exception e) {
            log.error("Erro ao processar comunicações da movimentação {}: {}", tipo, e.getMessage(), e);
        }
    }

    private void enviarNotificacaoOperacional(Subprocesso sp, TipoTransicao tipo, Unidade unidadeDestino, Map<String, Object> variaveis) {
        String assunto = criarAssunto(tipo, sp, false);
        String corpo = processarTemplate(tipo.getTemplateEmail(), variaveis);

        String emailUnidade = String.format("%s@tre-pe.jus.br", unidadeDestino.getSigla().toLowerCase());
        emailService.enviarEmailHtml(emailUnidade, assunto, corpo);
        log.info("Notificação operacional {} enviada para {}", tipo, unidadeDestino.getSigla());

        notificarResponsavelPessoal(unidadeDestino, assunto, corpo, tipo);
    }

    private void notificarResponsavelPessoal(Unidade unidade, String assunto, String corpo, TipoTransicao tipo) {
        UnidadeResponsavelDto responsavel = organizacaoFacade.buscarResponsavelUnidade(unidade.getCodigo());
        if (responsavel.substitutoTitulo() != null) {
            usuarioFacade.buscarUsuarioPorTitulo(responsavel.substitutoTitulo())
                .ifPresent(u -> {
                    if (!u.getEmail().isBlank()) {
                        emailService.enviarEmailHtml(u.getEmail(), assunto, corpo);
                        log.info("Notificação operacional '{}' enviada a e-mail pessoal de {}", tipo, u.getNome());
                    }
                });
        }
    }

    private void enviarNotificacaoAcompanhamentoSuperior(Unidade unidadeOrigem, Subprocesso sp, TipoTransicao tipo, Map<String, Object> variaveisBase, Unidade unidadeJaNotificada) {
        Unidade superior = unidadeOrigem.getUnidadeSuperior();
        String assunto = criarAssunto(tipo, sp, true);

        while (superior != null) {
            if (superior.getCodigo().equals(unidadeJaNotificada.getCodigo())) {
                superior = superior.getUnidadeSuperior();
                continue;
            }
            try {
                Map<String, Object> variaveis = new HashMap<>(variaveisBase);
                variaveis.put("siglaUnidadeSuperior", superior.getSigla());

                String corpo = processarTemplate(tipo.getTemplateEmailSuperior(), variaveis);
                String emailSuperior = String.format("%s@tre-pe.jus.br", superior.getSigla().toLowerCase());

                emailService.enviarEmailHtml(emailSuperior, assunto, corpo);
                log.info("Notificação de acompanhamento {} enviada para unidade {}", tipo, superior.getSigla());
            } catch (Exception e) {
                log.warn("Falha ao notificar unidade superior {}: {}", superior.getSigla(), e.getMessage());
            }
            superior = superior.getUnidadeSuperior();
        }
    }

    private Map<String, Object> criarVariaveisTemplateDireto(Subprocesso sp,
                                                            Unidade unidadeOrigem, Unidade unidadeDestino,
                                                            @Nullable String observacoes) {
        Map<String, Object> variaveis = new HashMap<>();

        variaveis.put("siglaUnidade", sp.getUnidade().getSigla());
        variaveis.put("nomeUnidade", sp.getUnidade().getNome());

        variaveis.put("siglaUnidadeOrigem", unidadeOrigem.getSigla());
        variaveis.put("nomeUnidadeOrigem", unidadeOrigem.getNome());
        variaveis.put("siglaUnidadeDestino", unidadeDestino.getSigla());
        variaveis.put("nomeUnidadeDestino", unidadeDestino.getNome());

        variaveis.put("nomeProcesso", sp.getProcesso().getDescricao());
        variaveis.put("tipoProcesso", sp.getProcesso().getTipo().name());

        if (sp.getDataLimiteEtapa1() != null) {
            variaveis.put("dataLimiteEtapa1", sp.getDataLimiteEtapa1().format(DATE_FORMATTER));
        }

        if (sp.getDataLimiteEtapa2() != null) {
            variaveis.put("dataLimiteEtapa2", sp.getDataLimiteEtapa2().format(DATE_FORMATTER));
            variaveis.put("dataLimiteValidacao", sp.getDataLimiteEtapa2().format(DATE_FORMATTER));
        }

        if (observacoes != null) {
            variaveis.put("observacoes", observacoes);
        }

        return variaveis;
    }

    private String criarAssunto(TipoTransicao tipo, Subprocesso sp, boolean paraSuperior) {
        String base = tipo.getDescricaoMovimentacao();
        if (paraSuperior) {
            return "SGC: %s - %s".formatted(base, sp.getUnidade().getSigla());
        }
        return "SGC: %s".formatted(base);
    }

    private String processarTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }
}
