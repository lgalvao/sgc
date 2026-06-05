package sgc.diagnostico.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.model.ComumRepo;
import sgc.diagnostico.model.Diagnostico;
import sgc.diagnostico.model.DiagnosticoRepo;
import sgc.diagnostico.model.SituacaoAvaliacaoServidor;
import sgc.diagnostico.model.SituacaoDiagnostico;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.service.HierarquiaService;
import sgc.organizacao.service.UnidadeHierarquiaService;
import sgc.organizacao.service.UnidadeService;
import sgc.organizacao.service.UsuarioService;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.RegistrarTransicaoCommand;
import sgc.subprocesso.dto.RegistrarWorkflowCommand;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.TipoAcaoAnalise;
import sgc.subprocesso.model.TipoAnalise;
import sgc.subprocesso.model.TipoTransicao;
import sgc.subprocesso.service.LocalizacaoSubprocessoService;
import sgc.subprocesso.service.SubprocessoConsultaService;
import sgc.subprocesso.service.SubprocessoTransicaoService;
import sgc.subprocesso.service.SubprocessoValidacaoService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiagnosticoFluxoServiceTest {

    @Mock DiagnosticoRepo diagnosticoRepo;
    @Mock ComumRepo repo;
    @Mock DiagnosticoValidacaoService validacaoService;
    @Mock DiagnosticoNotificacaoService notificacaoService;
    @Mock SubprocessoConsultaService subprocessoConsultaService;
    @Mock SubprocessoTransicaoService transicaoService;
    @Mock SubprocessoValidacaoService subprocessoValidacaoService;
    @Mock LocalizacaoSubprocessoService localizacaoSubprocessoService;
    @Mock UnidadeService unidadeService;
    @Mock UnidadeHierarquiaService unidadeHierarquiaService;
    @Mock HierarquiaService hierarquiaService;
    @Mock DiagnosticoUsuarioContextoService usuarioContextoService;
    @Mock UsuarioService usuarioService;

    @InjectMocks
    DiagnosticoFluxoService service;

    private Unidade unidadeOrigem;
    private Unidade unidadeSuperior;
    private Usuario chefe;

    @BeforeEach
    void setUp() {
        unidadeOrigem = unidade(12L, "ASSESSORIA_12", "Assessoria 12", TipoUnidade.OPERACIONAL);
        unidadeSuperior = unidade(2L, "SECRETARIA_1", "Secretaria 1", TipoUnidade.INTEROPERACIONAL);
        chefe = usuario("151515", "Axl Rose");
    }

    @Test
    @DisplayName("inicializarDiagnostico deve criar avaliações e ocupações com snapshot da unidade")
    void inicializarDiagnostico_deveCriarEstruturaCompleta() {
        Subprocesso subprocesso = subprocessoDiagnostico(unidadeOrigem, SituacaoSubprocesso.DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO);
        subprocesso.setMapa(mapa(101L, competencia(1L, "Competência 1"), competencia(2L, "Competência 2")));

        Usuario servidor1 = usuario("242426", "Duff McKagan");
        Usuario servidor2 = usuario("242427", "Izzy Stradlin");
        when(usuarioService.buscarPorUnidadeLotacao(unidadeOrigem.getCodigo())).thenReturn(List.of(servidor1, servidor2));

        ArgumentCaptor<Diagnostico> captor = ArgumentCaptor.forClass(Diagnostico.class);

        service.inicializarDiagnostico(subprocesso);

        verify(diagnosticoRepo).save(captor.capture());
        Diagnostico salvo = captor.getValue();

        assertThat(salvo.getSituacao()).isEqualTo(SituacaoDiagnostico.EM_ANDAMENTO);
        assertThat(salvo.getAvaliacaoServidores()).hasSize(4);
        assertThat(salvo.getOcupacaoCriticas()).hasSize(4);
        assertThat(salvo.getAvaliacaoServidores())
                .allSatisfy(avaliacao -> {
                    assertThat(avaliacao.getDiagnostico()).isSameAs(salvo);
                    assertThat(avaliacao.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.AUTOAVALIACAO_NAO_INICIADA);
                    assertThat(avaliacao.getServidorNomeDiagnostico()).isNotBlank();
                });
        assertThat(salvo.getOcupacaoCriticas())
                .allSatisfy(ocupacao -> {
                    assertThat(ocupacao.getDiagnostico()).isSameAs(salvo);
                    assertThat(ocupacao.getUnidadeCodigoSnapshot()).isEqualTo(unidadeOrigem.getCodigo());
                    assertThat(ocupacao.getUnidadeSiglaSnapshot()).isEqualTo("ASSESSORIA_12");
                    assertThat(ocupacao.getUnidadeNomeSnapshot()).isEqualTo("Assessoria 12");
                });
    }

    @Test
    @DisplayName("concluirDiagnosticoUnidade deve concluir diagnóstico, transicionar e notificar superior")
    void concluirDiagnosticoUnidade_deveConcluirEEncaminhar() {
        Long codSubprocesso = 90L;
        Diagnostico diagnostico = new Diagnostico();
        diagnostico.setCodigo(700L);
        diagnostico.setSituacao(SituacaoDiagnostico.EM_ANDAMENTO);

        Subprocesso subprocesso = subprocessoDiagnostico(unidadeOrigem, SituacaoSubprocesso.DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO);
        subprocesso.setCodigo(codSubprocesso);

        when(repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso))).thenReturn(diagnostico);
        when(subprocessoConsultaService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);
        when(usuarioContextoService.usuarioAutenticado()).thenReturn(chefe);
        when(unidadeHierarquiaService.buscarCodigoPai(unidadeOrigem.getCodigo())).thenReturn(unidadeSuperior.getCodigo());
        when(unidadeService.buscarPorCodigo(unidadeSuperior.getCodigo())).thenReturn(unidadeSuperior);
        doNothing().when(validacaoService).validarConclusaoUnidade(diagnostico.getCodigo());
        doNothing().when(subprocessoValidacaoService)
                .validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO);

        service.concluirDiagnosticoUnidade(codSubprocesso);

        assertThat(diagnostico.getSituacao()).isEqualTo(SituacaoDiagnostico.CONCLUIDO);
        assertThat(diagnostico.getDataConclusao()).isNotNull();
        assertThat(subprocesso.getSituacao()).isEqualTo(SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        assertThat(subprocesso.getDataFimEtapa1()).isNotNull();

        verify(transicaoService).registrarTransicaoSemEmail(any(RegistrarTransicaoCommand.class));
        verify(notificacaoService).notificarDiagnosticoConcluido(subprocesso, unidadeSuperior);
    }

    @Test
    @DisplayName("devolverDiagnostico deve retornar para autoavaliação quando devolvido à unidade dona")
    void devolverDiagnostico_deveRetornarParaAutoavaliacaoDaUnidade() {
        Long codSubprocesso = 91L;
        String observacao = "Ajustar consenso pendente";
        Diagnostico diagnostico = new Diagnostico();
        diagnostico.setSituacao(SituacaoDiagnostico.CONCLUIDO);
        diagnostico.setDataConclusao(LocalDateTime.now());

        Subprocesso subprocesso = subprocessoDiagnostico(unidadeOrigem, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        subprocesso.setCodigo(codSubprocesso);
        subprocesso.setDataFimEtapa1(LocalDateTime.now());

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setUnidadeOrigem(unidadeOrigem);
        movimentacao.setUnidadeDestino(unidadeSuperior);

        when(repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso))).thenReturn(diagnostico);
        when(subprocessoConsultaService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso)).thenReturn(unidadeSuperior);
        when(subprocessoConsultaService.listarMovimentacoesOrdenadas(codSubprocesso)).thenReturn(List.of(movimentacao));
        when(hierarquiaService.isSubordinada(unidadeOrigem, unidadeSuperior)).thenReturn(true);
        when(usuarioContextoService.usuarioAutenticado()).thenReturn(chefe);
        doNothing().when(subprocessoValidacaoService)
                .validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);

        service.devolverDiagnostico(codSubprocesso, observacao);

        assertThat(diagnostico.getSituacao()).isEqualTo(SituacaoDiagnostico.EM_ANDAMENTO);
        assertThat(diagnostico.getDataConclusao()).isNull();
        assertThat(subprocesso.getDataFimEtapa1()).isNull();

        ArgumentCaptor<RegistrarWorkflowCommand> captor = ArgumentCaptor.forClass(RegistrarWorkflowCommand.class);
        verify(transicaoService).registrarAnaliseSemEmail(captor.capture());
        assertThat(captor.getValue().novaSituacao()).isEqualTo(SituacaoSubprocesso.DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO);
        assertThat(captor.getValue().tipoAcaoAnalise()).isEqualTo(TipoAcaoAnalise.DEVOLUCAO_DIAGNOSTICO);
        assertThat(captor.getValue().unidadeDestinoTransicao()).isEqualTo(unidadeOrigem);
        verify(notificacaoService).notificarDiagnosticoDevolvido(subprocesso, unidadeSuperior, unidadeOrigem, observacao);
    }

    @Test
    @DisplayName("validarDiagnostico deve marcar como validado e encaminhar para unidade superior")
    void validarDiagnostico_deveMarcarComoValidado() {
        Long codSubprocesso = 92L;
        Diagnostico diagnostico = new Diagnostico();
        diagnostico.setSituacao(SituacaoDiagnostico.CONCLUIDO);

        Unidade unidadeGestora = unidade(5L, "COORD_11", "Coordenadoria 11", TipoUnidade.INTERMEDIARIA);
        Unidade unidadeDestino = unidade(2L, "SECRETARIA_1", "Secretaria 1", TipoUnidade.INTEROPERACIONAL);
        Subprocesso subprocesso = subprocessoDiagnostico(unidadeOrigem, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        subprocesso.setCodigo(codSubprocesso);

        when(repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso))).thenReturn(diagnostico);
        when(subprocessoConsultaService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso)).thenReturn(unidadeGestora);
        when(unidadeHierarquiaService.buscarCodigoPai(unidadeGestora.getCodigo())).thenReturn(unidadeDestino.getCodigo());
        when(unidadeService.buscarPorCodigo(unidadeDestino.getCodigo())).thenReturn(unidadeDestino);
        when(usuarioContextoService.usuarioAutenticado()).thenReturn(chefe);
        doNothing().when(subprocessoValidacaoService)
                .validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);

        service.validarDiagnostico(codSubprocesso, "Pode seguir");

        assertThat(diagnostico.getSituacao()).isEqualTo(SituacaoDiagnostico.VALIDADO);
        ArgumentCaptor<RegistrarWorkflowCommand> captor = ArgumentCaptor.forClass(RegistrarWorkflowCommand.class);
        verify(transicaoService).registrarAnaliseSemEmail(captor.capture());
        assertThat(captor.getValue().tipoAnalise()).isEqualTo(TipoAnalise.DIAGNOSTICO);
        assertThat(captor.getValue().tipoAcaoAnalise()).isEqualTo(TipoAcaoAnalise.ACEITE_DIAGNOSTICO);
        assertThat(captor.getValue().tipoTransicao()).isEqualTo(TipoTransicao.DIAGNOSTICO_ACEITO);
        assertThat(captor.getValue().unidadeDestinoTransicao()).isEqualTo(unidadeDestino);
        verify(notificacaoService).notificarDiagnosticoAceito(subprocesso, unidadeGestora, unidadeDestino);
    }

    @Test
    @DisplayName("homologarDiagnostico deve exigir validado e registrar homologação no admin")
    void homologarDiagnostico_deveHomologar() {
        Long codSubprocesso = 93L;
        Diagnostico diagnostico = new Diagnostico();
        diagnostico.setSituacao(SituacaoDiagnostico.VALIDADO);

        Unidade admin = unidade(1L, "ADMIN", "Administração", TipoUnidade.RAIZ);
        Subprocesso subprocesso = subprocessoDiagnostico(unidadeOrigem, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        subprocesso.setCodigo(codSubprocesso);

        when(repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso))).thenReturn(diagnostico);
        when(subprocessoConsultaService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);
        when(unidadeService.buscarAdmin()).thenReturn(admin);
        when(usuarioContextoService.usuarioAutenticado()).thenReturn(chefe);
        doNothing().when(subprocessoValidacaoService)
                .validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        doNothing().when(validacaoService).validarSituacaoDiagnostico(diagnostico, SituacaoDiagnostico.VALIDADO);

        service.homologarDiagnostico(codSubprocesso, "Homologado");

        assertThat(diagnostico.getSituacao()).isEqualTo(SituacaoDiagnostico.HOMOLOGADO);
        ArgumentCaptor<RegistrarTransicaoCommand> captor = ArgumentCaptor.forClass(RegistrarTransicaoCommand.class);
        verify(transicaoService).registrarTransicaoSemEmail(captor.capture());
        assertThat(captor.getValue().tipo()).isEqualTo(TipoTransicao.DIAGNOSTICO_HOMOLOGADO);
        assertThat(captor.getValue().origem()).isEqualTo(admin);
        assertThat(captor.getValue().destino()).isEqualTo(admin);
        verify(notificacaoService).notificarDiagnosticoHomologado(subprocesso);
    }

    private Unidade unidade(Long codigo, String sigla, String nome, TipoUnidade tipo) {
        Unidade unidade = new Unidade();
        unidade.setCodigo(codigo);
        unidade.setSigla(sigla);
        unidade.setNome(nome);
        unidade.setTipo(tipo);
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        return unidade;
    }

    private Usuario usuario(String titulo, String nome) {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(titulo);
        usuario.setNome(nome);
        return usuario;
    }

    private Competencia competencia(Long codigo, String descricao) {
        Competencia competencia = new Competencia();
        competencia.setCodigo(codigo);
        competencia.setDescricao(descricao);
        return competencia;
    }

    private Mapa mapa(Long codigo, Competencia... competencias) {
        Mapa mapa = new Mapa();
        mapa.setCodigo(codigo);
        mapa.setCompetencias(java.util.Set.of(competencias));
        return mapa;
    }

    private Subprocesso subprocessoDiagnostico(Unidade unidade, SituacaoSubprocesso situacao) {
        Processo processo = new Processo();
        processo.setCodigo(500L);
        processo.setTipo(TipoProcesso.DIAGNOSTICO);
        processo.adicionarParticipantes(java.util.Set.of(unidade));

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(600L);
        subprocesso.setProcesso(processo);
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacaoForcada(situacao);
        return subprocesso;
    }
}
