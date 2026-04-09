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
        ContextoConsultaSubprocesso contexto = montarContextoConsulta(sp, usuarioFacade.usuarioAutenticado(), movimentacoes);
        return construirDetalhe(contexto, movimentacoes);
    }

    public ContextoEdicaoResponse obterContextoEdicao(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        Mapa mapaCompleto = mapaManutencaoService.mapaCompletoSubprocesso(subprocesso.getCodigo());

        return new ContextoEdicaoResponse(
                subprocesso.getUnidade(),
                SubprocessoResumoDto.fromEntity(subprocesso),
                obterDetalhes(subprocesso),
                MapaCompletoDto.fromEntity(mapaCompleto),
                mapaCompleto.getAtividades().stream()
                        .map(AtividadeDto::fromEntity)
                        .toList()
        );
    }

    public PermissoesSubprocessoDto obterPermissoesUI(Subprocesso sp) {
        return resolverPermissoes(montarContextoPermissao(montarContextoConsulta(sp, usuarioFacade.usuarioAutenticado())));
    }

    private PermissoesSubprocessoDto construirPermissoes(ContextoPermissaoSubprocesso contexto) {
        boolean habilitarAcessoCadastro = verificarAcessoCadastroHabilitado(contexto);
        boolean habilitarAcessoMapa = verificarAcessoMapaHabilitado(contexto);

        return PermissoesSubprocessoDto.builder()
                .podeEditarCadastro(contexto.isChefe() && SITUACOES_EDICAO_CADASTRO.contains(contexto.situacao()))
                .podeDisponibilizarCadastro(contexto.isChefe() && SITUACOES_DISPONIBILIZACAO_CADASTRO.contains(contexto.situacao()))
                .podeDevolverCadastro(contexto.isGestorOuAdmin() && SITUACOES_ANALISE_CADASTRO.contains(contexto.situacao()))
                .podeAceitarCadastro(contexto.isGestor() && SITUACOES_ANALISE_CADASTRO.contains(contexto.situacao()))
                .podeHomologarCadastro(contexto.isAdmin() && SITUACOES_ANALISE_CADASTRO.contains(contexto.situacao()))
                .podeEditarMapa(verificarEditarMapa(contexto))
                .podeDisponibilizarMapa(contexto.isAdmin() && SITUACOES_DISPONIBILIZACAO_MAPA.contains(contexto.situacao()))
                .podeValidarMapa(contexto.isChefe() && SITUACOES_ANALISE_MAPA.contains(contexto.situacao()))
                .podeApresentarSugestoes(contexto.isChefe() && SITUACOES_ANALISE_MAPA.contains(contexto.situacao()))
                .podeVerSugestoes(contexto.isGestorOuAdmin() && SITUACOES_COM_SUGESTOES_MAPA.contains(contexto.situacao()))
                .podeDevolverMapa(verificarGerirMapa(contexto.isGestorOuAdmin(), contexto.situacao()))
                .podeAceitarMapa(verificarGerirMapa(contexto.isGestor(), contexto.situacao()))
                .podeHomologarMapa(verificarGerirMapa(contexto.isAdmin(), contexto.situacao()))
                .podeVisualizarImpacto(verificarVisualizarImpacto(contexto))
                .podeAlterarDataLimite(contexto.isAdmin())
                .podeReabrirCadastro(contexto.isAdmin() && isSituacaoMapeamentoAPartirDe(contexto.situacao(), MAPEAMENTO_MAPA_HOMOLOGADO))
                .podeReabrirRevisao(contexto.isAdmin() && isSituacaoRevisaoAPartirDe(contexto.situacao(), REVISAO_MAPA_HOMOLOGADO))
                .podeEnviarLembrete(contexto.isAdmin())
                .mesmaUnidade(contexto.mesmaUnidade())
                .habilitarAcessoCadastro(habilitarAcessoCadastro)
                .habilitarAcessoMapa(habilitarAcessoMapa)
                .build();
    }

    private PermissoesSubprocessoDto construirPermissoesProcessoFinalizado(ContextoPermissaoSubprocesso contexto) {
        return PermissoesSubprocessoDto.builder()
                .habilitarAcessoCadastro(verificarAcessoCadastroHabilitado(contexto))
                .habilitarAcessoMapa(verificarAcessoMapaHabilitado(contexto))
                .build();
    }

    private boolean verificarAcessoCadastroHabilitado(ContextoPermissaoSubprocesso contexto) {
        if (contexto.isChefe()) return Objects.equals(contexto.unidadeAlvo().getCodigo(), contexto.unidadeUsuario().getCodigo());
        boolean cadastroDisponibilizado = verificarCadastroDisponibilizadoParaVisualizacao(contexto.situacao());
        if (contexto.isAdmin()) return cadastroDisponibilizado;
        if (contexto.isGestor()) return cadastroDisponibilizado && hierarquiaService.ehMesmaOuSubordinada(contexto.unidadeAlvo(), contexto.unidadeUsuario());
        return cadastroDisponibilizado && Objects.equals(contexto.unidadeAlvo().getCodigo(), contexto.unidadeUsuario().getCodigo());
    }

    private boolean verificarAcessoMapaHabilitado(ContextoPermissaoSubprocesso contexto) {
        if (contexto.isAdmin()) return verificarMapaHabilitadoParaAdmin(contexto.situacao());
        if (contexto.isGestor()) return verificarMapaDisponibilizadoParaVisualizacao(contexto.situacao()) && hierarquiaService.ehMesmaOuSubordinada(contexto.unidadeAlvo(), contexto.unidadeUsuario());
        if (contexto.isChefe() || contexto.isServidor()) return verificarMapaDisponibilizadoParaVisualizacao(contexto.situacao()) && Objects.equals(contexto.unidadeAlvo().getCodigo(), contexto.unidadeUsuario().getCodigo());
        return false;
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

    private boolean verificarVisualizarImpacto(ContextoPermissaoSubprocesso contexto) {
        return contexto.temMapaVigente()
                && impactoMapaService.podeVisualizarImpactos(contexto.subprocesso());
    }

    private boolean verificarEditarMapa(ContextoPermissaoSubprocesso contexto) {
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
                .permissoes(resolverPermissoes(montarContextoPermissao(contexto)))
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

    private ContextoConsultaSubprocesso montarContextoConsulta(Subprocesso sp, Usuario usuario) {
        return montarContextoConsulta(sp, usuario, listarMovimentacoes(sp));
    }

    private ContextoConsultaSubprocesso montarContextoConsulta(Subprocesso sp, Usuario usuario, List<Movimentacao> movimentacoes) {
        Processo processo = sp.getProcesso();
        Unidade unidadeUsuario = unidadeService.buscarPorCodigoComSuperior(usuario.getUnidadeAtivaCodigo());
        Unidade localizacaoAtual = resolverLocalizacaoAtual(sp, movimentacoes);
        boolean processoFinalizado = processo != null && processo.getSituacao() == SituacaoProcesso.FINALIZADO;
        boolean mesmaUnidade = !processoFinalizado
                && Objects.equals(usuario.getUnidadeAtivaCodigo(), localizacaoAtual.getCodigo());
        boolean temMapaVigente = !processoFinalizado && unidadeService.temMapaVigente(sp.getUnidade().getCodigo());
        return new ContextoConsultaSubprocesso(
                sp,
                usuario,
                unidadeUsuario,
                localizacaoAtual,
                processoFinalizado,
                mesmaUnidade,
                temMapaVigente
        );
    }

    private Unidade resolverLocalizacaoAtual(Subprocesso sp, List<Movimentacao> movimentacoes) {
        if (!movimentacoes.isEmpty()) {
            return movimentacoes.getFirst().getUnidadeDestino();
        }
        return localizacaoSubprocessoService.obterLocalizacaoAtual(sp);
    }

    private ContextoPermissaoSubprocesso montarContextoPermissao(ContextoConsultaSubprocesso contextoConsulta) {
        Subprocesso sp = contextoConsulta.subprocesso();
        return new ContextoPermissaoSubprocesso(
                sp,
                contextoConsulta.usuario().getPerfilAtivo(),
                sp.getSituacao(),
                sp.getUnidade(),
                contextoConsulta.unidadeUsuario(),
                contextoConsulta.mesmaUnidade(),
                contextoConsulta.temMapaVigente(),
                contextoConsulta.processoFinalizado()
        );
    }

    private PermissoesSubprocessoDto resolverPermissoes(ContextoPermissaoSubprocesso contexto) {
        if (contexto.processoFinalizado()) {
            return construirPermissoesProcessoFinalizado(contexto);
        }
        return construirPermissoes(contexto);
    }

    private record ContextoConsultaSubprocesso(
            Subprocesso subprocesso,
            Usuario usuario,
            Unidade unidadeUsuario,
            Unidade localizacaoAtual,
            boolean processoFinalizado,
            boolean mesmaUnidade,
            boolean temMapaVigente
    ) {

        private Unidade unidadeAlvo() {
            return subprocesso.getUnidade();
        }
    }

    private record ContextoPermissaoSubprocesso(
            Subprocesso subprocesso,
            Perfil perfil,
            SituacaoSubprocesso situacao,
            Unidade unidadeAlvo,
            Unidade unidadeUsuario,
            boolean mesmaUnidade,
            boolean temMapaVigente,
            boolean processoFinalizado
    ) {

        private boolean isChefe() {
            return perfil == Perfil.CHEFE;
        }

        private boolean isGestor() {
            return perfil == Perfil.GESTOR;
        }

        private boolean isAdmin() {
            return perfil == Perfil.ADMIN;
        }

        private boolean isServidor() {
            return perfil == Perfil.SERVIDOR;
        }

        private boolean isGestorOuAdmin() {
            return isGestor() || isAdmin();
        }
    }
}
