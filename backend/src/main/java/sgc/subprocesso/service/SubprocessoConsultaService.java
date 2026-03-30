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
        return subprocessoRepo.findByProcessoCodigoComUnidade(codProcesso);
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
        return subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoInWithUnidade(codProcesso, codUnidades);
    }

    public List<Subprocesso> listarPorProcessoESituacoes(Long codProcesso, List<SituacaoSubprocesso> situacoes) {
        return subprocessoRepo.findByProcessoCodigoAndSituacaoInWithUnidade(codProcesso, situacoes);
    }

    public List<Subprocesso> listarPorProcessoEUnidadeCodigosESituacoes(Long codProcesso, List<Long> codigosUnidades, List<SituacaoSubprocesso> situacoes) {
        return subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoInWithUnidade(codProcesso, codigosUnidades).stream()
                .filter(sp -> situacoes.contains(sp.getSituacao()))
                .toList();
    }

    public List<Subprocesso> listarPorProcessoUnidadeESituacoes(Long codProcesso, Long codUnidade, List<SituacaoSubprocesso> situacoes) {
        return subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoAndSituacaoInComUnidade(codProcesso, codUnidade, situacoes);
    }

    public ValidacaoCadastroDto validarCadastro(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return validacaoService.validarCadastro(sp);
    }

    @org.jspecify.annotations.NonNull public Unidade obterUnidadeLocalizacao(Subprocesso sp) {
        if (sp.getLocalizacaoAtual() != null) return sp.getLocalizacaoAtual();
        Unidade unidade = sp.getUnidade();
        if (sp.getCodigo() == null) return unidade;
        List<Movimentacao> movs = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());
        if (movs.isEmpty()) return unidade;
        Unidade destino = movs.getFirst().getUnidadeDestino();
        return (destino != null) ? destino : unidade;
    }

    public SubprocessoDetalheResponse obterDetalhes(Long codigo, Usuario usuarioAutenticado) {
        Subprocesso sp = buscarSubprocesso(codigo);
        return obterDetalhes(sp, usuarioAutenticado);
    }

    public SubprocessoDetalheResponse obterDetalhes(Subprocesso sp, Usuario usuarioAutenticado) {
        String siglaUnidade = sp.getUnidade().getSigla();
        String localizacaoAtual = obterUnidadeLocalizacao(sp).getSigla();
        ResponsavelDto responsavel = usuarioFacade.buscarResponsabilidadeDetalhadaAtual(siglaUnidade);
        Usuario titular = usuarioFacade.buscarPorLogin(sp.getUnidade().getTituloTitular());
        List<Movimentacao> movs = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());
        List<MovimentacaoDto> movimentacoes = movs.stream().map(MovimentacaoDto::from).toList();
        PermissoesSubprocessoDto permissoes = obterPermissoesUI(sp, usuarioAutenticado);

        return SubprocessoDetalheResponse.builder()
                .subprocesso(SubprocessoResumoDto.fromEntity(sp))
                .responsavel(responsavel)
                .titular(UsuarioResumoDto.fromEntity(titular))
                .movimentacoes(movimentacoes)
                .localizacaoAtual(localizacaoAtual)
                .permissoes(permissoes)
                .build();
    }

    public ContextoEdicaoResponse obterContextoEdicao(Long codSubprocesso) {
        Usuario usuario = usuarioFacade.usuarioAutenticado();
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        SubprocessoDetalheResponse detalhes = obterDetalhes(sp, usuario);
        Unidade unidadeSp = sp.getUnidade();
        List<AtividadeDto> atividades = listarAtividadesSubprocesso(codSubprocesso);

        return new ContextoEdicaoResponse(
                unidadeSp,
                SubprocessoResumoDto.fromEntity(sp),
                detalhes,
                mapaCompletoDtoPorSubprocesso(codSubprocesso),
                atividades
        );
    }

    public PermissoesSubprocessoDto obterPermissoesUI(Subprocesso sp, Usuario usuario) {
        Processo processo = sp.getProcesso();
        Perfil perfil = usuario.getPerfilAtivo();
        SituacaoSubprocesso situacao = sp.getSituacao();

        if (processo != null && processo.getSituacao() == SituacaoProcesso.FINALIZADO) {
            Unidade unidadeUsuario = unidadeService.buscarPorCodigo(usuario.getUnidadeAtivaCodigo());
            return PermissoesSubprocessoDto.builder()
                    .habilitarAcessoCadastro(verificarAcessoCadastroHabilitado(perfil, situacao, sp.getUnidade(), unidadeUsuario))
                    .habilitarAcessoMapa(verificarAcessoMapaHabilitado(perfil, situacao, sp.getUnidade(), unidadeUsuario))
                    .build();
        }

        Long codUnidadeUsuario = usuario.getUnidadeAtivaCodigo();
        Long codUnidadeLocalizacao = obterUnidadeLocalizacao(sp).getCodigo();
        boolean mesmaUnidade = Objects.equals(codUnidadeUsuario, codUnidadeLocalizacao);
        boolean temMapaVigente = unidadeService.verificarMapaVigente(sp.getUnidade().getCodigo());
        return construirPermissoes(mesmaUnidade, usuario, sp, temMapaVigente);
    }

    private PermissoesSubprocessoDto construirPermissoes(boolean mesmaUnidade, Usuario usuario, Subprocesso sp, boolean temMapaVigente) {
        Perfil perfil = usuario.getPerfilAtivo();
        SituacaoSubprocesso situacao = sp.getSituacao();
        Unidade unidadeUsuario = unidadeService.buscarPorCodigo(usuario.getUnidadeAtivaCodigo());

        boolean isChefe = perfil == Perfil.CHEFE;
        boolean isGestor = perfil == Perfil.GESTOR;
        boolean isAdmin = perfil == Perfil.ADMIN;

        boolean habilitarAcessoCadastro = verificarAcessoCadastroHabilitado(perfil, situacao, sp.getUnidade(), unidadeUsuario);
        boolean habilitarAcessoMapa = verificarAcessoMapaHabilitado(perfil, situacao, sp.getUnidade(), unidadeUsuario);

        return PermissoesSubprocessoDto.builder()
                .podeEditarCadastro(isChefe && SITUACOES_EDICAO_CADASTRO.contains(situacao))
                .podeDisponibilizarCadastro(isChefe && SITUACOES_DISPONIBILIZACAO_CADASTRO.contains(situacao))
                .podeDevolverCadastro((isGestor || isAdmin) && SITUACOES_ANALISE_CADASTRO.contains(situacao))
                .podeAceitarCadastro(isGestor && SITUACOES_ANALISE_CADASTRO.contains(situacao))
                .podeHomologarCadastro(isAdmin && SITUACOES_ANALISE_CADASTRO.contains(situacao))
                .podeEditarMapa(verificarEditarMapa(isAdmin, situacao))
                .podeDisponibilizarMapa(isAdmin && SITUACOES_DISPONIBILIZACAO_MAPA.contains(situacao))
                .podeValidarMapa(isChefe && SITUACOES_ANALISE_MAPA.contains(situacao))
                .podeApresentarSugestoes(isChefe && SITUACOES_ANALISE_MAPA.contains(situacao))
                .podeVerSugestoes((isGestor || isAdmin) && SITUACOES_COM_SUGESTOES_MAPA.contains(situacao))
                .podeDevolverMapa(verificarGerirMapa(isGestor || isAdmin, situacao))
                .podeAceitarMapa(verificarGerirMapa(isGestor, situacao))
                .podeHomologarMapa(verificarGerirMapa(isAdmin, situacao))
                .podeVisualizarImpacto(verificarVisualizarImpacto(temMapaVigente, mesmaUnidade, isChefe, isGestor, isAdmin, situacao))
                .podeAlterarDataLimite(isAdmin)
                .podeReabrirCadastro(isAdmin && isSituacaoMapeamentoAPartirDe(situacao, MAPEAMENTO_MAPA_HOMOLOGADO))
                .podeReabrirRevisao(isAdmin && isSituacaoRevisaoAPartirDe(situacao, REVISAO_MAPA_HOMOLOGADO))
                .podeEnviarLembrete(isAdmin)
                .mesmaUnidade(mesmaUnidade)
                .habilitarAcessoCadastro(habilitarAcessoCadastro)
                .habilitarAcessoMapa(habilitarAcessoMapa)
                .build();
    }

    private boolean verificarAcessoCadastroHabilitado(Perfil perfil, SituacaoSubprocesso situacao, Unidade unidadeAlvo, Unidade unidadeUsuario) {
        if (perfil == Perfil.CHEFE) return Objects.equals(unidadeAlvo.getCodigo(), unidadeUsuario.getCodigo());
        boolean cadastroDisponibilizado = verificarCadastroDisponibilizadoParaVisualizacao(situacao);
        if (perfil == Perfil.ADMIN) return cadastroDisponibilizado;
        if (perfil == Perfil.GESTOR) return cadastroDisponibilizado && hierarquiaService.ehMesmaOuSubordinada(unidadeAlvo, unidadeUsuario);
        return cadastroDisponibilizado && Objects.equals(unidadeAlvo.getCodigo(), unidadeUsuario.getCodigo());
    }

    private boolean verificarAcessoMapaHabilitado(Perfil perfil, SituacaoSubprocesso situacao, Unidade unidadeAlvo, Unidade unidadeUsuario) {
        if (perfil == Perfil.ADMIN) return verificarMapaHabilitadoParaAdmin(situacao);
        if (perfil == Perfil.GESTOR) return verificarMapaDisponibilizadoParaVisualizacao(situacao) && hierarquiaService.ehMesmaOuSubordinada(unidadeAlvo, unidadeUsuario);
        if (perfil == Perfil.CHEFE || perfil == Perfil.SERVIDOR) return verificarMapaDisponibilizadoParaVisualizacao(situacao) && Objects.equals(unidadeAlvo.getCodigo(), unidadeUsuario.getCodigo());
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

    private boolean verificarVisualizarImpacto(boolean temMapaVigente, boolean mesmaUnidade, boolean isChefe, boolean isGestor, boolean isAdmin, SituacaoSubprocesso situacao) {
        return temMapaVigente && ((mesmaUnidade && isChefe && situacao == REVISAO_CADASTRO_EM_ANDAMENTO) || (mesmaUnidade && isGestor && situacao == REVISAO_CADASTRO_DISPONIBILIZADA) || (isAdmin && SITUACOES_IMPACTO_ADMIN.contains(situacao)));
    }

    private boolean verificarEditarMapa(boolean isAdmin, SituacaoSubprocesso situacao) {
        return isAdmin && SITUACOES_EDICAO_MAPA.contains(situacao);
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
        Long codMapa = subprocesso.getMapa().getCodigo();
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
        return subprocessoRepo.findByProcessoCodigoComUnidade(codProcesso);
    }

    @org.jspecify.annotations.NonNull public Unidade obterLocalizacaoAtual(Subprocesso sp) {
        if (sp.getLocalizacaoAtual() != null) return sp.getLocalizacaoAtual();
        if (sp.getCodigo() == null) return sp.getUnidade();
        Unidade loc = movimentacaoRepo.findFirstBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo())
                .filter(m -> m.getUnidadeDestino() != null)
                .map(Movimentacao::getUnidadeDestino)
                .orElse(sp.getUnidade());
        sp.setLocalizacaoAtual(loc);
        return loc;
    }

    public MapaAjusteDto obterMapaParaAjuste(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        Long codMapa = sp.getMapa().getCodigo();

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
}
