package sgc.subprocesso.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.util.stream.Collectors;
import java.util.*;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubprocessoConsultaService {

    private static final String NOME_ENTIDADE = "Subprocesso";
    private static final Set<SituacaoSubprocesso> SITUACOES_EDICAO_CADASTRO = Set.of(
            NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO);
    private static final Set<SituacaoSubprocesso> SITUACOES_DISPONIBILIZACAO_CADASTRO = Set.of(
            MAPEAMENTO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO);
    private static final Set<SituacaoSubprocesso> SITUACOES_ANALISE_CADASTRO = Set.of(
            MAPEAMENTO_CADASTRO_DISPONIBILIZADO, REVISAO_CADASTRO_DISPONIBILIZADA);
    private static final Set<SituacaoSubprocesso> SITUACOES_DISPONIBILIZACAO_MAPA = Set.of(
            MAPEAMENTO_CADASTRO_HOMOLOGADO,
            MAPEAMENTO_MAPA_CRIADO,
            MAPEAMENTO_MAPA_COM_SUGESTOES,
            REVISAO_CADASTRO_HOMOLOGADA,
            REVISAO_MAPA_AJUSTADO,
            REVISAO_MAPA_COM_SUGESTOES);
    private static final Set<SituacaoSubprocesso> SITUACOES_ANALISE_MAPA = Set.of(
            MAPEAMENTO_MAPA_DISPONIBILIZADO, REVISAO_MAPA_DISPONIBILIZADO);
    private static final Set<SituacaoSubprocesso> SITUACOES_COM_SUGESTOES_MAPA = Set.of(
            MAPEAMENTO_MAPA_COM_SUGESTOES, REVISAO_MAPA_COM_SUGESTOES);
    private static final Set<SituacaoSubprocesso> SITUACOES_GESTAO_MAPA = Set.of(
            MAPEAMENTO_MAPA_COM_SUGESTOES,
            MAPEAMENTO_MAPA_VALIDADO,
            REVISAO_MAPA_COM_SUGESTOES,
            REVISAO_MAPA_VALIDADO);
    private static final Set<SituacaoSubprocesso> SITUACOES_EDICAO_MAPA = Set.of(
            MAPEAMENTO_CADASTRO_HOMOLOGADO,
            MAPEAMENTO_MAPA_CRIADO,
            REVISAO_CADASTRO_HOMOLOGADA,
            REVISAO_MAPA_AJUSTADO,
            DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO);
    private final SubprocessoRepo subprocessoRepo;
    private final AnaliseRepo analiseRepo;
    private final UnidadeService unidadeService;
    private final UsuarioFacade usuarioFacade;
    private final ImpactoMapaService impactoMapaService;
    private final MapaVisualizacaoService mapaVisualizacaoService;
    private final MapaManutencaoService mapaManutencaoService;
    private final MovimentacaoRepo movimentacaoRepo;
    private final HierarquiaService hierarquiaService;
    private final SubprocessoValidacaoService validacaoService;
    private final LocalizacaoSubprocessoService localizacaoSubprocessoService;
    private final AnaliseHistoricoService analiseHistoricoService;

    public MapaVisualizacaoResponse mapaParaVisualizacao(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return mapaVisualizacaoService.obterMapaParaVisualizacao(sp);
    }

    public ImpactoMapaResponse verificarImpactos(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return impactoMapaService.verificarImpactos(sp);
    }

    public MapaCompletoDto mapaCompletoDtoPorSubprocesso(Long codSubprocesso) {
        return MapaCompletoDto.fromEntity(mapaManutencaoService.mapaCompletoSubprocesso(codSubprocesso));
    }

    public Map<String, Object> obterSugestoes(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        Mapa mapa = subprocesso.getMapa();
        if (mapa == null) {
            if (subprocesso.getSituacao() != null && subprocesso.getSituacao().name().contains("MAPA")) {
                throw new ErroInconsistenciaInterna(
                        "Subprocesso %s em etapa de mapa sem mapa vinculado para leitura de sugestoes"
                                .formatted(codSubprocesso)
                );
            }
            return Map.of("sugestoes", "");
        }
        String sugestoes = Optional.ofNullable(mapa.getSugestoes()).orElse("");
        return Map.of("sugestoes", sugestoes);
    }

    public Subprocesso buscarSubprocesso(Long codigo) {
        return subprocessoRepo.buscarPorCodigoComMapaEAtividades(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(NOME_ENTIDADE, codigo));
    }

    private Subprocesso buscarSubprocessoComMapa(Long codigo) {
        return subprocessoRepo.buscarPorCodigoComMapa(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(NOME_ENTIDADE, codigo));
    }

    public List<Subprocesso> listarEntidadesPorProcesso(Long codProcesso) {
        return subprocessoRepo.listarPorProcessoComUnidade(codProcesso);
    }

    public SubprocessoSituacaoDto obterStatus(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        return obterStatus(subprocesso);
    }

    public SubprocessoSituacaoDto obterStatus(Subprocesso subprocesso) {
        return SubprocessoSituacaoDto.builder()
                .codigo(subprocesso.getCodigo())
                .situacao(subprocesso.getSituacao())
                .build();
    }

    public Subprocesso obterEntidadePorCodigoMapa(Long codMapa) {
        return subprocessoRepo.findByMapa_Codigo(codMapa)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(NOME_ENTIDADE, "Mapa ID: " + codMapa));
    }

    public List<Subprocesso> listarTodos() {
        return subprocessoRepo.listarTodosComFetch();
    }

    public SubprocessoCadastroDto obterCadastro(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        return obterCadastro(subprocesso);
    }

    public SubprocessoCadastroDto obterCadastro(Subprocesso subprocesso) {
        return SubprocessoCadastroDto.fromEntity(subprocesso, listarAtividadesSubprocesso(subprocesso));
    }

    public Subprocesso obterEntidadePorProcessoEUnidade(Long codProcesso, Long codUnidade) {
        return subprocessoRepo.buscarPorProcessoEUnidadeComFetch(codProcesso, codUnidade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(NOME_ENTIDADE, "P:%d U:%d".formatted(codProcesso, codUnidade)));
    }

    public List<Subprocesso> listarEntidadesPorProcessoEUnidades(Long codProcesso, List<Long> codUnidades) {
        if (codUnidades.isEmpty()) return List.of();
        return subprocessoRepo.listarPorProcessoEUnidadesComUnidade(codProcesso, codUnidades);
    }

    public List<Subprocesso> listarPorProcessoESituacoes(Long codProcesso, List<SituacaoSubprocesso> situacoes) {
        return subprocessoRepo.listarPorProcessoESituacoesComUnidade(codProcesso, situacoes);
    }

    public List<Subprocesso> listarPorProcessoEUnidadeCodigosESituacoes(Long codProcesso, List<Long> codigosUnidades, List<SituacaoSubprocesso> situacoes) {
        if (codigosUnidades.isEmpty() || situacoes.isEmpty()) {
            return List.of();
        }
        return subprocessoRepo.listarPorProcessoEUnidadesComUnidade(codProcesso, codigosUnidades).stream()
                .filter(sp -> situacoes.contains(sp.getSituacao()))
                .toList();
    }

    public List<Subprocesso> listarPorProcessoUnidadeESituacoes(Long codProcesso, Long codUnidade, List<SituacaoSubprocesso> situacoes) {
        return subprocessoRepo.listarPorProcessoUnidadeESituacoesComUnidade(codProcesso, codUnidade, situacoes);
    }

    public ValidacaoCadastroDto validarCadastro(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return validacaoService.validarCadastro(sp);
    }

    public SubprocessoDetalheResponse obterDetalhes(Long codigo) {
        Subprocesso sp = buscarSubprocesso(codigo);
        return obterDetalhes(sp);
    }

    public SubprocessoDetalheResponse obterDetalhes(Subprocesso sp) {
        List<Movimentacao> movimentacoes = listarMovimentacoes(sp);
        return construirDetalhe(montarContextoConsulta(sp, movimentacoes), movimentacoes);
    }

    public ContextoEdicaoResponse obterContextoEdicao(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocessoComMapa(codSubprocesso);
        Long codMapa = obterCodigoMapaObrigatorio(subprocesso);
        List<Atividade> atividadesComConhecimentos = mapaManutencaoService.atividadesMapaCodigoComConhecimentos(codMapa);
        Mapa mapaCompleto = mapaManutencaoService.mapaComCompetenciasEAtividadesSubprocesso(subprocesso.getCodigo());

        return new ContextoEdicaoResponse(
                subprocesso.getUnidade(),
                SubprocessoResumoDto.fromEntity(subprocesso),
                obterDetalhes(subprocesso),
                MapaCompletoDto.fromEntity(mapaCompleto),
                atividadesComConhecimentos.stream()
                        .map(AtividadeDto::fromEntity)
                        .toList()
        );
    }

    public ContextoCadastroAtividadesResponse obterContextoCadastroAtividades(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocessoComMapa(codSubprocesso);
        ContextoConsultaSubprocesso contexto = montarContextoConsultaLeve(subprocesso);
        List<AtividadeDto> atividadesDisponiveis = listarAtividadesSubprocesso(subprocesso);

        return new ContextoCadastroAtividadesResponse(
                subprocesso.getUnidade(),
                construirDetalheCadastro(contexto),
                MapaResumoDto.fromEntity(obterMapaObrigatorio(subprocesso)),
                atividadesDisponiveis,
                obterAssinaturaCadastroReferencia(subprocesso, atividadesDisponiveis)
        );
    }

    private String obterAssinaturaCadastroReferencia(Subprocesso subprocesso, List<AtividadeDto> atividadesDisponiveis) {
        List<AtividadeDto> atividadesReferencia = atividadesDisponiveis;

        if (subprocesso.getProcesso().getTipo() == TipoProcesso.REVISAO) {
            atividadesReferencia = mapaManutencaoService.mapaVigenteUnidade(subprocesso.getUnidade().getCodigo())
                    .map(Mapa::getCodigo)
                    .map(mapaManutencaoService::atividadesMapaCodigoComConhecimentos)
                    .orElseGet(List::of)
                    .stream()
                    .map(AtividadeDto::fromEntity)
                    .toList();
        }

        return calcularAssinaturaCadastro(atividadesReferencia);
    }

    private String calcularAssinaturaCadastro(List<AtividadeDto> atividades) {
        return atividades.stream()
                .map(atividade -> {
                    String descricao = atividade.descricao().trim();
                    String conhecimentos = atividade.conhecimentos().stream()
                            .map(conhecimento -> conhecimento.descricao().trim())
                            .sorted()
                            .collect(Collectors.joining("\u0001"));
                    return descricao + "\u0002" + conhecimentos;
                })
                .sorted()
                .collect(Collectors.joining("\u0003"));
    }

    public PermissoesSubprocessoDto obterPermissoesUI(Subprocesso sp) {
        return resolverPermissoes(montarContextoConsultaLeve(sp));
    }

    private PermissoesSubprocessoDto construirPermissoes(ContextoConsultaSubprocesso contexto) {
        PermissoesFluxo permissoesFluxo = calcularPermissoesFluxo(contexto);
        boolean mesmaUnidade = contexto.mesmaUnidade();
        boolean habilitarAcessoCadastro = verificarAcessoCadastroHabilitado(contexto);
        boolean habilitarAcessoMapa = verificarAcessoMapaHabilitado(contexto);

        return PermissoesSubprocessoDto.builder()
                .podeEditarCadastro(permissoesFluxo.podeEditarCadastro())
                .podeDisponibilizarCadastro(permissoesFluxo.podeDisponibilizarCadastro())
                .podeDevolverCadastro(permissoesFluxo.podeDevolverCadastro())
                .podeAceitarCadastro(permissoesFluxo.podeAceitarCadastro())
                .podeHomologarCadastro(permissoesFluxo.podeHomologarCadastro())
                .podeEditarMapa(permissoesFluxo.podeEditarMapa())
                .podeDisponibilizarMapa(permissoesFluxo.podeDisponibilizarMapa())
                .podeValidarMapa(permissoesFluxo.podeValidarMapa())
                .podeApresentarSugestoes(permissoesFluxo.podeApresentarSugestoes())
                .podeVerSugestoes(permissoesFluxo.podeVerSugestoes())
                .podeDevolverMapa(permissoesFluxo.podeDevolverMapa())
                .podeAceitarMapa(permissoesFluxo.podeAceitarMapa())
                .podeHomologarMapa(permissoesFluxo.podeHomologarMapa())
                .podeVisualizarImpacto(permissoesFluxo.podeVisualizarImpacto())
                .podeAlterarDataLimite(contexto.isAdmin())
                .podeReabrirCadastro(contexto.isAdmin() && isSituacaoMapeamentoAPartirDe(contexto.situacao(), MAPEAMENTO_MAPA_HOMOLOGADO))
                .podeReabrirRevisao(contexto.isAdmin() && isSituacaoRevisaoAPartirDe(contexto.situacao(), REVISAO_MAPA_HOMOLOGADO))
                .podeEnviarLembrete(contexto.isAdmin())
                .mesmaUnidade(mesmaUnidade)
                .habilitarAcessoCadastro(habilitarAcessoCadastro)
                .habilitarAcessoMapa(habilitarAcessoMapa)
                .habilitarEditarCadastro(permissoesFluxo.podeEditarCadastro() && mesmaUnidade)
                .habilitarDisponibilizarCadastro(permissoesFluxo.podeDisponibilizarCadastro() && mesmaUnidade)
                .habilitarDevolverCadastro(permissoesFluxo.podeDevolverCadastro() && mesmaUnidade)
                .habilitarAceitarCadastro(permissoesFluxo.podeAceitarCadastro() && mesmaUnidade)
                .habilitarHomologarCadastro(permissoesFluxo.podeHomologarCadastro() && mesmaUnidade)
                .habilitarEditarMapa(permissoesFluxo.podeEditarMapa() && mesmaUnidade)
                .habilitarDisponibilizarMapa(permissoesFluxo.podeDisponibilizarMapa() && mesmaUnidade)
                .habilitarValidarMapa(permissoesFluxo.podeValidarMapa() && mesmaUnidade)
                .habilitarApresentarSugestoes(permissoesFluxo.podeApresentarSugestoes() && mesmaUnidade)
                .habilitarDevolverMapa(permissoesFluxo.podeDevolverMapa() && mesmaUnidade)
                .habilitarAceitarMapa(permissoesFluxo.podeAceitarMapa() && mesmaUnidade)
                .habilitarHomologarMapa(permissoesFluxo.podeHomologarMapa() && mesmaUnidade)
                .build();
    }

    private PermissoesFluxo calcularPermissoesFluxo(ContextoConsultaSubprocesso contexto) {
        SituacaoSubprocesso situacao = contexto.situacao();
        return PermissoesFluxo.builder()
                .podeEditarCadastro(contexto.isChefe() && SITUACOES_EDICAO_CADASTRO.contains(situacao))
                .podeDisponibilizarCadastro(contexto.isChefe() && SITUACOES_DISPONIBILIZACAO_CADASTRO.contains(situacao))
                .podeDevolverCadastro(contexto.isGestorOuAdmin() && SITUACOES_ANALISE_CADASTRO.contains(situacao))
                .podeAceitarCadastro(contexto.isGestor() && SITUACOES_ANALISE_CADASTRO.contains(situacao))
                .podeHomologarCadastro(contexto.isAdmin() && SITUACOES_ANALISE_CADASTRO.contains(situacao))
                .podeEditarMapa(verificarEditarMapa(contexto))
                .podeDisponibilizarMapa(contexto.isAdmin() && SITUACOES_DISPONIBILIZACAO_MAPA.contains(situacao))
                .podeValidarMapa(contexto.isChefe() && SITUACOES_ANALISE_MAPA.contains(situacao))
                .podeApresentarSugestoes(contexto.isChefe() && SITUACOES_ANALISE_MAPA.contains(situacao))
                .podeVerSugestoes(contexto.isGestorOuAdmin() && SITUACOES_COM_SUGESTOES_MAPA.contains(situacao))
                .podeDevolverMapa(verificarGerirMapa(contexto.isGestorOuAdmin(), situacao))
                .podeAceitarMapa(verificarGerirMapa(contexto.isGestor(), situacao))
                .podeHomologarMapa(verificarGerirMapa(contexto.isAdmin(), situacao))
                .podeVisualizarImpacto(verificarVisualizarImpacto(contexto))
                .build();
    }

    private PermissoesSubprocessoDto construirPermissoesProcessoFinalizado(ContextoConsultaSubprocesso contexto) {
        return PermissoesSubprocessoDto.builder()
                .habilitarAcessoCadastro(verificarAcessoCadastroHabilitado(contexto))
                .habilitarAcessoMapa(verificarAcessoMapaHabilitado(contexto))
                .mesmaUnidade(contexto.mesmaUnidade())
                .habilitarEditarCadastro(false)
                .habilitarDisponibilizarCadastro(false)
                .habilitarDevolverCadastro(false)
                .habilitarAceitarCadastro(false)
                .habilitarHomologarCadastro(false)
                .habilitarEditarMapa(false)
                .habilitarDisponibilizarMapa(false)
                .habilitarValidarMapa(false)
                .habilitarApresentarSugestoes(false)
                .habilitarDevolverMapa(false)
                .habilitarAceitarMapa(false)
                .habilitarHomologarMapa(false)
                .build();
    }

    private boolean verificarAcessoCadastroHabilitado(ContextoConsultaSubprocesso contexto) {
        if (contexto.isChefe()) {
            return contexto.isMesmaUnidadeAlvo();
        }

        boolean cadastroDisponibilizado = verificarCadastroDisponibilizadoParaVisualizacao(contexto.situacao());
        if (!cadastroDisponibilizado) {
            return false;
        }

        return switch (contexto.perfil()) {
            case Perfil.ADMIN -> true;
            case Perfil.GESTOR -> contexto.isUnidadeAlvoNaHierarquiaUsuario();
            case Perfil.CHEFE, Perfil.SERVIDOR -> contexto.isMesmaUnidadeAlvo();
        };
    }

    private boolean verificarAcessoMapaHabilitado(ContextoConsultaSubprocesso contexto) {
        boolean mapaDisponibilizado = verificarMapaDisponibilizadoParaVisualizacao(contexto.situacao());
        boolean mesmaHierarquia = contexto.isUnidadeAlvoNaHierarquiaUsuario();
        boolean mesmaUnidadeAlvo = contexto.isMesmaUnidadeAlvo();

        return switch (contexto.perfil()) {
            case Perfil.ADMIN -> verificarMapaHabilitadoParaAdmin(contexto.situacao());
            case Perfil.GESTOR -> mapaDisponibilizado && mesmaHierarquia;
            case Perfil.CHEFE, Perfil.SERVIDOR -> mapaDisponibilizado && mesmaUnidadeAlvo;
        };
    }

    private boolean verificarCadastroDisponibilizadoParaVisualizacao(SituacaoSubprocesso situacao) {
        return isSituacaoMapeamentoAPartirDe(situacao, MAPEAMENTO_CADASTRO_DISPONIBILIZADO) || isSituacaoRevisaoAPartirDe(situacao, REVISAO_CADASTRO_DISPONIBILIZADA);
    }

    private boolean verificarMapaDisponibilizadoParaVisualizacao(SituacaoSubprocesso situacao) {
        return isSituacaoMapeamentoAPartirDe(situacao, MAPEAMENTO_MAPA_DISPONIBILIZADO) || isSituacaoRevisaoAPartirDe(situacao, REVISAO_MAPA_DISPONIBILIZADO);
    }

    private boolean verificarMapaHabilitadoParaAdmin(SituacaoSubprocesso situacao) {
        return isSituacaoMapeamentoAPartirDe(situacao, MAPEAMENTO_CADASTRO_HOMOLOGADO) || isSituacaoRevisaoAPartirDe(situacao, REVISAO_CADASTRO_HOMOLOGADA);
    }

    private boolean verificarVisualizarImpacto(ContextoConsultaSubprocesso contexto) {
        return contexto.temMapaVigente()
                && impactoMapaService.podeVisualizarImpactos(contexto.subprocesso());
    }

    private boolean verificarEditarMapa(ContextoConsultaSubprocesso contexto) {
        return contexto.isAdmin() && SITUACOES_EDICAO_MAPA.contains(contexto.situacao());
    }

    private boolean verificarGerirMapa(boolean isPermitido, SituacaoSubprocesso situacao) {
        return isPermitido && SITUACOES_GESTAO_MAPA.contains(situacao);
    }

    private boolean isSituacaoMapeamentoAPartirDe(SituacaoSubprocesso situacaoAtual, SituacaoSubprocesso marcoInicial) {
        return isFluxoMapeamento(situacaoAtual) && situacaoAtual.ordinal() >= marcoInicial.ordinal();
    }

    private boolean isSituacaoRevisaoAPartirDe(SituacaoSubprocesso situacaoAtual, SituacaoSubprocesso marcoInicial) {
        return isFluxoRevisao(situacaoAtual) && situacaoAtual.ordinal() >= marcoInicial.ordinal();
    }

    private boolean isFluxoMapeamento(SituacaoSubprocesso situacao) {
        return situacao.name().startsWith("MAPEAMENTO");
    }

    private boolean isFluxoRevisao(SituacaoSubprocesso situacao) {
        return situacao.name().startsWith("REVISAO");
    }

    public List<AtividadeDto> listarAtividadesSubprocesso(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        return listarAtividadesSubprocesso(subprocesso);
    }

    public List<AtividadeDto> listarAtividadesSubprocesso(Subprocesso subprocesso) {
        Long codMapa = obterCodigoMapaObrigatorio(subprocesso);
        return mapaManutencaoService.atividadesMapaCodigoComConhecimentos(codMapa).stream()
                .map(AtividadeDto::fromEntity)
                .toList();
    }

    public List<AtividadeDto> listarAtividadesParaImportacao(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        if (subprocesso.getProcesso() == null || subprocesso.getProcesso().getSituacao() != SituacaoProcesso.FINALIZADO) {
            throw new ErroValidacao("SGC-MSG-100230"); // Utilização simplificada para código temporário
        }
        return listarAtividadesSubprocesso(subprocesso);
    }

    public List<AnaliseHistoricoDto> listarHistoricoCadastro(Long codSubprocesso) {
        return listarHistoricoPorTipo(codSubprocesso, TipoAnalise.CADASTRO);
    }

    public List<AnaliseHistoricoDto> listarHistoricoValidacao(Long codSubprocesso) {
        return listarHistoricoPorTipo(codSubprocesso, TipoAnalise.VALIDACAO);
    }

    private List<AnaliseHistoricoDto> listarHistoricoPorTipo(Long codSubprocesso, TipoAnalise tipo) {
        return analiseHistoricoService.converterLista(listarAnalisesPorTipo(codSubprocesso, tipo));
    }

    public MapaAjusteDto obterMapaParaAjuste(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        Long codMapa = obterCodigoMapaObrigatorio(sp);

        return MapaAjusteDto.of(
                sp,
                obterAnaliseMaisRecentePorTipo(codSubprocesso),
                mapaManutencaoService.competenciasCodMapaSemRels(codMapa),
                mapaManutencaoService.atividadesMapaCodigoSemRels(codMapa),
                mapaManutencaoService.conhecimentosCodMapa(codMapa)
        );
    }

    private Mapa obterMapaObrigatorio(Subprocesso subprocesso) {
        Mapa mapa = subprocesso.getMapa();
        if (mapa == null) {
            throw new IllegalStateException("Subprocesso %s sem mapa associado".formatted(subprocesso.getCodigo()));
        }
        return mapa;
    }

    private Long obterCodigoMapaObrigatorio(Subprocesso subprocesso) {
        return obterMapaObrigatorio(subprocesso).getCodigo();
    }

    private SubprocessoDetalheResponse construirDetalhe(ContextoConsultaSubprocesso contexto, List<Movimentacao> movimentacoes) {
        Subprocesso subprocesso = contexto.subprocesso();
        Unidade unidadeAlvo = contexto.unidadeAlvo();
        Usuario titular = buscarTitularSeInformado(unidadeAlvo);

        return SubprocessoDetalheResponse.builder()
                .subprocesso(SubprocessoResumoDto.fromEntity(subprocesso))
                .responsavel(usuarioFacade.buscarResponsabilidadeDetalhadaAtual(unidadeAlvo.getCodigo()))
                .titular(UsuarioResumoDto.fromEntity(titular))
                .movimentacoes(listarMovimentacoesDto(movimentacoes))
                .localizacaoAtual(contexto.localizacaoAtual().getSigla())
                .permissoes(resolverPermissoes(contexto))
                .build();
    }

    private List<Analise> listarAnalisesPorTipo(Long codSubprocesso, TipoAnalise tipo) {
        return analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codSubprocesso).stream()
                .filter(analise -> analise.getTipo() == tipo)
                .toList();
    }

    private @Nullable Analise obterAnaliseMaisRecentePorTipo(Long codSubprocesso) {
        return listarAnalisesPorTipo(codSubprocesso, TipoAnalise.VALIDACAO).stream().findFirst().orElse(null);
    }

    private @Nullable Usuario buscarTitularSeInformado(Unidade unidade) {
        String tituloTitular = unidade.getTituloTitular();
        if (tituloTitular == null || tituloTitular.isBlank()) {
            return null;
        }
        return usuarioFacade.buscarUsuarioSemAtribuicoes(tituloTitular);
    }

    private List<MovimentacaoDto> listarMovimentacoesDto(List<Movimentacao> movimentacoes) {
        return movimentacoes.stream()
                .map(MovimentacaoDto::from)
                .toList();
    }

    private List<Movimentacao> listarMovimentacoes(Subprocesso sp) {
        return movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(sp.getCodigo());
    }

    private ContextoConsultaSubprocesso montarContextoConsultaLeve(Subprocesso sp) {
        return montarContextoConsulta(sp, List.of());
    }

    private ContextoConsultaSubprocesso montarContextoConsulta(Subprocesso sp, List<Movimentacao> movimentacoes) {
        DadosContextoConsulta dadosContexto = resolverDadosContexto(sp, movimentacoes);
        Long unidadeAtivaCodigo = dadosContexto.unidadeAtivaCodigo();

        Unidade unidadeAlvo = dadosContexto.unidadeAlvo();
        boolean mesmaUnidadeAlvo = Objects.equals(unidadeAtivaCodigo, unidadeAlvo.getCodigo());
        boolean mesmaUnidade = mesmaUnidadeLocalizacao(unidadeAtivaCodigo, dadosContexto.localizacaoAtual(), dadosContexto.processoFinalizado());
        boolean unidadeAlvoNaHierarquiaUsuario = isUnidadeAlvoNaHierarquiaUsuario(unidadeAlvo, dadosContexto.unidadeUsuario(), mesmaUnidadeAlvo);
        boolean temMapaVigente = temMapaVigente(unidadeAlvo.getCodigo(), dadosContexto.processoFinalizado());

        return ContextoConsultaSubprocesso.builder()
                .subprocesso(sp)
                .perfil(dadosContexto.perfil())
                .localizacaoAtual(dadosContexto.localizacaoAtual())
                .processoFinalizado(dadosContexto.processoFinalizado())
                .mesmaUnidade(mesmaUnidade)
                .mesmaUnidadeAlvo(mesmaUnidadeAlvo)
                .unidadeAlvoNaHierarquiaUsuario(unidadeAlvoNaHierarquiaUsuario)
                .temMapaVigente(temMapaVigente)
                .build();
    }

    private DadosContextoConsulta resolverDadosContexto(Subprocesso sp, List<Movimentacao> movimentacoes) {
        ContextoUsuarioAutenticado contextoUsuario = obterContextoUsuarioAutenticado();
        Long unidadeAtivaCodigo = contextoUsuario.unidadeAtivaCodigo();
        return DadosContextoConsulta.builder()
                .perfil(contextoUsuario.perfil())
                .unidadeUsuario(unidadeService.buscarPorCodigoComSuperior(unidadeAtivaCodigo))
                .unidadeAlvo(sp.getUnidade())
                .unidadeAtivaCodigo(unidadeAtivaCodigo)
                .localizacaoAtual(resolverLocalizacaoAtual(sp, movimentacoes))
                .processoFinalizado(processoFinalizado(sp))
                .build();
    }

    private boolean processoFinalizado(Subprocesso subprocesso) {
        Processo processo = subprocesso.getProcesso();
        return processo != null && processo.getSituacao() == SituacaoProcesso.FINALIZADO;
    }

    private boolean mesmaUnidadeLocalizacao(Long unidadeAtivaCodigo, Unidade localizacaoAtual, boolean processoFinalizado) {
        return !processoFinalizado && Objects.equals(unidadeAtivaCodigo, localizacaoAtual.getCodigo());
    }

    private boolean isUnidadeAlvoNaHierarquiaUsuario(Unidade unidadeAlvo, Unidade unidadeUsuario, boolean mesmaUnidadeAlvo) {
        return mesmaUnidadeAlvo || hierarquiaService.ehMesmaOuSubordinada(unidadeAlvo, unidadeUsuario);
    }

    private boolean temMapaVigente(Long unidadeCodigo, boolean processoFinalizado) {
        return !processoFinalizado && unidadeService.temMapaVigente(unidadeCodigo);
    }

    private ContextoUsuarioAutenticado obterContextoUsuarioAutenticado() {
        return Objects.requireNonNull(usuarioFacade.contextoAutenticado(), "contexto autenticado obrigatorio");
    }

    private Unidade resolverLocalizacaoAtual(Subprocesso sp, List<Movimentacao> movimentacoes) {
        return movimentacoes.isEmpty()
                ? localizacaoSubprocessoService.obterLocalizacaoAtual(sp)
                : movimentacoes.getFirst().getUnidadeDestino();
    }

    private PermissoesSubprocessoDto resolverPermissoes(ContextoConsultaSubprocesso contexto) {
        if (contexto.processoFinalizado()) {
            return construirPermissoesProcessoFinalizado(contexto);
        }
        return construirPermissoes(contexto);
    }

    private SubprocessoDetalheResponse construirDetalheCadastro(ContextoConsultaSubprocesso contexto) {
        Subprocesso subprocesso = contexto.subprocesso();

        return SubprocessoDetalheResponse.builder()
                .subprocesso(SubprocessoResumoDto.fromEntity(subprocesso))
                .responsavel(null)
                .titular(null)
                .movimentacoes(List.of())
                .localizacaoAtual(contexto.localizacaoAtual().getSigla())
                .permissoes(resolverPermissoes(contexto))
                .build();
    }

    @Builder
    private record ContextoConsultaSubprocesso(
            Subprocesso subprocesso,
            Perfil perfil,
            Unidade localizacaoAtual,
            boolean processoFinalizado,
            boolean mesmaUnidade,
            boolean mesmaUnidadeAlvo,
            boolean unidadeAlvoNaHierarquiaUsuario,
            boolean temMapaVigente
    ) {

        private Unidade unidadeAlvo() {
            return subprocesso.getUnidade();
        }

        private SituacaoSubprocesso situacao() {
            return subprocesso.getSituacao();
        }

        private boolean isMesmaUnidadeAlvo() {
            return mesmaUnidadeAlvo;
        }

        private boolean isUnidadeAlvoNaHierarquiaUsuario() {
            return unidadeAlvoNaHierarquiaUsuario;
        }

        private boolean isChefe() {
            return perfil == Perfil.CHEFE;
        }

        private boolean isGestor() {
            return perfil == Perfil.GESTOR;
        }

        private boolean isAdmin() {
            return perfil == Perfil.ADMIN;
        }

        private boolean isGestorOuAdmin() {
            return isGestor() || isAdmin();
        }
    }

    @Builder
    private record DadosContextoConsulta(
            Perfil perfil,
            Unidade unidadeUsuario,
            Unidade unidadeAlvo,
            Long unidadeAtivaCodigo,
            Unidade localizacaoAtual,
            boolean processoFinalizado
    ) {}

    @Builder
    private record PermissoesFluxo(
            boolean podeEditarCadastro,
            boolean podeDisponibilizarCadastro,
            boolean podeDevolverCadastro,
            boolean podeAceitarCadastro,
            boolean podeHomologarCadastro,
            boolean podeEditarMapa,
            boolean podeDisponibilizarMapa,
            boolean podeValidarMapa,
            boolean podeApresentarSugestoes,
            boolean podeVerSugestoes,
            boolean podeDevolverMapa,
            boolean podeAceitarMapa,
            boolean podeHomologarMapa,
            boolean podeVisualizarImpacto
    ) {
    }
}
