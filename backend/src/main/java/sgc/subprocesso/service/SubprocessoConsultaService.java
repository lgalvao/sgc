package sgc.subprocesso.service;

import lombok.*;
import lombok.extern.slf4j.*;
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
    private static final Set<SituacaoSubprocesso> SITUACOES_IMPACTO_ADMIN = Set.of(
            REVISAO_CADASTRO_DISPONIBILIZADA,
            REVISAO_CADASTRO_HOMOLOGADA,
            REVISAO_MAPA_AJUSTADO);

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

    public MapaVisualizacaoResponse mapaParaVisualizacao(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return mapaVisualizacaoService.obterMapaParaVisualizacao(sp);
    }

    public ImpactoMapaResponse verificarImpactos(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return impactoMapaService.verificarImpactos(sp, usuario);
    }

    public Mapa mapaCompletoPorSubprocesso(Long codSubprocesso) {
        return mapaManutencaoService.mapaCompletoSubprocesso(codSubprocesso);
    }

    public MapaCompletoDto mapaCompletoDtoPorSubprocesso(Long codSubprocesso) {
        return MapaCompletoDto.fromEntity(mapaCompletoPorSubprocesso(codSubprocesso));
    }

    public Map<String, Object> obterSugestoes(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        String sugestoes = Optional.ofNullable(subprocesso.getMapa())
                .map(Mapa::getSugestoes)
                .orElse("");
        return Map.of("sugestoes", sugestoes);
    }

    public Subprocesso buscarSubprocesso(Long codigo) {
        return subprocessoRepo.buscarPorCodigoComMapaEAtividades(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(NOME_ENTIDADE, codigo));
    }

    public Subprocesso buscarSubprocessoComMapa(Long codigo) {
        return buscarSubprocesso(codigo);
    }

    public List<Subprocesso> listarEntidadesPorProcesso(Long codProcesso) {
        return subprocessoRepo.listarPorProcessoComUnidade(codProcesso);
    }

    public SubprocessoSituacaoDto obterStatus(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
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
        List<AtividadeDto> atividades = listarAtividadesSubprocesso(codSubprocesso);
        return SubprocessoCadastroDto.fromEntity(subprocesso, atividades);
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

    public Unidade obterUnidadeLocalizacao(Subprocesso sp) {
        return localizacaoSubprocessoService.obterLocalizacaoAtual(sp);
    }

    public SubprocessoDetalheResponse obterDetalhes(Long codigo, Usuario usuarioAutenticado) {
        Subprocesso sp = buscarSubprocesso(codigo);
        return obterDetalhes(sp, usuarioAutenticado);
    }

    public SubprocessoDetalheResponse obterDetalhes(Subprocesso sp, Usuario usuarioAutenticado) {
        DadosDetalheSubprocesso dadosDetalhe = montarDadosDetalhe(sp, usuarioAutenticado);

        return SubprocessoDetalheResponse.builder()
                .subprocesso(SubprocessoResumoDto.fromEntity(sp))
                .responsavel(dadosDetalhe.responsavel())
                .titular(UsuarioResumoDto.fromEntity(dadosDetalhe.titular()))
                .movimentacoes(dadosDetalhe.movimentacoes())
                .localizacaoAtual(dadosDetalhe.localizacaoAtual())
                .permissoes(dadosDetalhe.permissoes())
                .build();
    }

    public ContextoEdicaoResponse obterContextoEdicao(Long codSubprocesso) {
        Usuario usuario = usuarioFacade.usuarioAutenticado();
        DadosContextoEdicao dadosContexto = montarDadosContextoEdicao(codSubprocesso, usuario);
        return new ContextoEdicaoResponse(
                dadosContexto.unidade(),
                SubprocessoResumoDto.fromEntity(dadosContexto.subprocesso()),
                dadosContexto.detalhes(),
                dadosContexto.mapa(),
                dadosContexto.atividades()
        );
    }

    public PermissoesSubprocessoDto obterPermissoesUI(Subprocesso sp, Usuario usuario) {
        ContextoPermissaoSubprocesso contexto = montarContextoPermissao(sp, usuario);
        if (contexto.processoFinalizado()) {
            return construirPermissoesProcessoFinalizado(contexto, sp);
        }
        return construirPermissoes(contexto, sp);
    }

    private PermissoesSubprocessoDto construirPermissoes(ContextoPermissaoSubprocesso contexto, Subprocesso sp) {
        boolean habilitarAcessoCadastro = verificarAcessoCadastroHabilitado(contexto, sp.getUnidade());
        boolean habilitarAcessoMapa = verificarAcessoMapaHabilitado(contexto, sp.getUnidade());

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

    private PermissoesSubprocessoDto construirPermissoesProcessoFinalizado(ContextoPermissaoSubprocesso contexto, Subprocesso sp) {
        return PermissoesSubprocessoDto.builder()
                .habilitarAcessoCadastro(verificarAcessoCadastroHabilitado(contexto, sp.getUnidade()))
                .habilitarAcessoMapa(verificarAcessoMapaHabilitado(contexto, sp.getUnidade()))
                .build();
    }

    private boolean verificarAcessoCadastroHabilitado(ContextoPermissaoSubprocesso contexto, Unidade unidadeAlvo) {
        if (contexto.isChefe()) return Objects.equals(unidadeAlvo.getCodigo(), contexto.unidadeUsuario().getCodigo());
        boolean cadastroDisponibilizado = verificarCadastroDisponibilizadoParaVisualizacao(contexto.situacao());
        if (contexto.isAdmin()) return cadastroDisponibilizado;
        if (contexto.isGestor()) return cadastroDisponibilizado && hierarquiaService.ehMesmaOuSubordinada(unidadeAlvo, contexto.unidadeUsuario());
        return cadastroDisponibilizado && Objects.equals(unidadeAlvo.getCodigo(), contexto.unidadeUsuario().getCodigo());
    }

    private boolean verificarAcessoMapaHabilitado(ContextoPermissaoSubprocesso contexto, Unidade unidadeAlvo) {
        if (contexto.isAdmin()) return verificarMapaHabilitadoParaAdmin(contexto.situacao());
        if (contexto.isGestor()) return verificarMapaDisponibilizadoParaVisualizacao(contexto.situacao()) && hierarquiaService.ehMesmaOuSubordinada(unidadeAlvo, contexto.unidadeUsuario());
        if (contexto.isChefe() || contexto.isServidor()) return verificarMapaDisponibilizadoParaVisualizacao(contexto.situacao()) && Objects.equals(unidadeAlvo.getCodigo(), contexto.unidadeUsuario().getCodigo());
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
                && ((contexto.mesmaUnidade() && contexto.isChefe() && contexto.situacao() == REVISAO_CADASTRO_EM_ANDAMENTO)
                || (contexto.mesmaUnidade() && contexto.isGestor() && contexto.situacao() == REVISAO_CADASTRO_DISPONIBILIZADA)
                || (contexto.isAdmin() && SITUACOES_IMPACTO_ADMIN.contains(contexto.situacao())));
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
        Long codMapa = obterCodigoMapaObrigatorio(subprocesso);
        List<Atividade> todasAtividades = mapaManutencaoService.atividadesMapaCodigoComConhecimentos(codMapa);
        return todasAtividades.stream().map(AtividadeDto::fromEntity).toList();
    }

    public List<AtividadeDto> listarAtividadesParaImportacao(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);
        if (subprocesso.getProcesso() == null || subprocesso.getProcesso().getSituacao() != SituacaoProcesso.FINALIZADO) {
            throw new ErroValidacao("SGC-MSG-100230"); // Utilização simplificada para código temporário
        }
        return listarAtividadesSubprocesso(codSubprocesso);
    }

    public List<Analise> listarAnalisesPorSubprocesso(Long codSubprocesso) {
        return analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codSubprocesso);
    }

    public List<Analise> listarAnalisesPorSubprocesso(Long codSubprocesso, TipoAnalise tipo) {
        return analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codSubprocesso).stream().filter(a -> a.getTipo() == tipo).toList();
    }

    public List<AnaliseHistoricoDto> listarHistoricoCadastro(Long codSubprocesso) {
        return listarHistoricoPorTipo(codSubprocesso, TipoAnalise.CADASTRO);
    }

    public List<AnaliseHistoricoDto> listarHistoricoValidacao(Long codSubprocesso) {
        return listarHistoricoPorTipo(codSubprocesso, TipoAnalise.VALIDACAO);
    }

    private List<AnaliseHistoricoDto> listarHistoricoPorTipo(Long codSubprocesso, TipoAnalise tipo) {
        return listarAnalisesPorSubprocesso(codSubprocesso, tipo).stream().map(this::paraHistoricoDto).toList();
    }

    public AnaliseHistoricoDto paraHistoricoDto(Analise analise) {
        Unidade unidade = unidadeService.buscarPorCodigo(analise.getUnidadeCodigo());
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

    public List<Subprocesso> listarEntidadesPorProcessoComUnidade(Long codProcesso) {
        return subprocessoRepo.listarPorProcessoComUnidade(codProcesso);
    }

    public Unidade obterLocalizacaoAtual(Subprocesso sp) {
        return localizacaoSubprocessoService.obterLocalizacaoAtual(sp);
    }

    public MapaAjusteDto obterMapaParaAjuste(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        Long codMapa = obterCodigoMapaObrigatorio(sp);

        Analise analise = listarAnalisesPorSubprocesso(codSubprocesso, TipoAnalise.VALIDACAO).stream().findFirst().orElse(null);
        List<sgc.mapa.model.Competencia> competencias = mapaManutencaoService.competenciasCodMapaSemRels(codMapa);
        List<Atividade> atividades = mapaManutencaoService.atividadesMapaCodigoSemRels(codMapa);
        List<sgc.mapa.model.Conhecimento> conhecimentos = mapaManutencaoService.conhecimentosCodMapa(codMapa);

        return MapaAjusteDto.of(sp, analise, competencias, atividades, conhecimentos);
    }

    public Mapa obterMapaObrigatorio(Subprocesso subprocesso) {
        Mapa mapa = subprocesso.getMapa();
        if (mapa == null) {
            throw new IllegalStateException("Subprocesso %s sem mapa associado".formatted(subprocesso.getCodigo()));
        }
        return mapa;
    }

    public Long obterCodigoMapaObrigatorio(Subprocesso subprocesso) {
        return obterMapaObrigatorio(subprocesso).getCodigo();
    }

    private DadosDetalheSubprocesso montarDadosDetalhe(Subprocesso sp, Usuario usuarioAutenticado) {
        String siglaUnidade = sp.getUnidade().getSigla();
        return new DadosDetalheSubprocesso(
                obterUnidadeLocalizacao(sp).getSigla(),
                usuarioFacade.buscarResponsabilidadeDetalhadaAtual(siglaUnidade),
                buscarTitularSeInformado(sp.getUnidade()),
                listarMovimentacoesDto(sp),
                obterPermissoesUI(sp, usuarioAutenticado)
        );
    }

    private DadosContextoEdicao montarDadosContextoEdicao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return new DadosContextoEdicao(
                sp,
                sp.getUnidade(),
                obterDetalhes(sp, usuario),
                mapaCompletoDtoPorSubprocesso(codSubprocesso),
                listarAtividadesSubprocesso(codSubprocesso)
        );
    }

    private Usuario buscarTitularSeInformado(Unidade unidade) {
        String tituloTitular = unidade.getTituloTitular();
        if (tituloTitular == null || tituloTitular.isBlank()) {
            return null;
        }
        return usuarioFacade.buscarPorLogin(tituloTitular);
    }

    private List<MovimentacaoDto> listarMovimentacoesDto(Subprocesso sp) {
        return movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(sp.getCodigo()).stream()
                .map(MovimentacaoDto::from)
                .toList();
    }

    private ContextoPermissaoSubprocesso montarContextoPermissao(Subprocesso sp, Usuario usuario) {
        Processo processo = sp.getProcesso();
        Unidade unidadeUsuario = unidadeService.buscarPorCodigo(usuario.getUnidadeAtivaCodigo());
        boolean processoFinalizado = processo != null && processo.getSituacao() == SituacaoProcesso.FINALIZADO;
        boolean mesmaUnidade = !processoFinalizado
                && Objects.equals(usuario.getUnidadeAtivaCodigo(), obterUnidadeLocalizacao(sp).getCodigo());
        boolean temMapaVigente = !processoFinalizado && unidadeService.temMapaVigente(sp.getUnidade().getCodigo());
        return new ContextoPermissaoSubprocesso(
                usuario.getPerfilAtivo(),
                sp.getSituacao(),
                unidadeUsuario,
                mesmaUnidade,
                temMapaVigente,
                processoFinalizado
        );
    }

    private record DadosDetalheSubprocesso(
            String localizacaoAtual,
            ResponsavelDto responsavel,
            Usuario titular,
            List<MovimentacaoDto> movimentacoes,
            PermissoesSubprocessoDto permissoes
    ) {
    }

    private record DadosContextoEdicao(
            Subprocesso subprocesso,
            Unidade unidade,
            SubprocessoDetalheResponse detalhes,
            MapaCompletoDto mapa,
            List<AtividadeDto> atividades
    ) {
    }

    private record ContextoPermissaoSubprocesso(
            Perfil perfil,
            SituacaoSubprocesso situacao,
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
