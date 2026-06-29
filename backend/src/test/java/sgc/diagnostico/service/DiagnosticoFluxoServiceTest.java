package sgc.diagnostico.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.model.*;
import sgc.diagnostico.model.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosticoFluxoServiceTest {

    @Mock DiagnosticoRepo diagnosticoRepo;
    @Mock ComumRepo repo;
    @Mock DiagnosticoValidacaoService validacaoService;
    @Mock DiagnosticoNotificacaoService notificacaoService;
    @Mock SubprocessoConsultaService subprocessoConsultaService;
    @Mock SubprocessoTransicaoService transicaoService;
    @Mock SubprocessoValidacaoService subprocessoValidacaoService;
    @Mock SubprocessoFluxoContextoService fluxoContextoService;
    @Mock LocalizacaoSubprocessoService localizacaoSubprocessoService;
    @Mock UnidadeService unidadeService;
    @Mock DiagnosticoUsuarioContextoService usuarioContextoService;
    @Mock UsuarioService usuarioService;
    @Mock ResponsavelUnidadeService responsavelUnidadeService;

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
        Subprocesso subprocesso = subprocessoDiagnostico(unidadeOrigem, SituacaoSubprocesso.DIAGNOSTICO_EM_ANDAMENTO);
        Processo processo = subprocesso.getProcesso();
        Mapa mapaVigente = mapa(101L, competencia(1L, "Competência 1"), competencia(2L, "Competência 2"));

        Usuario servidor1 = usuario("242426", "Duff McKagan");
        Usuario servidor2 = usuario("242427", "Izzy Stradlin");
        processo.adicionarServidoresParticipantes(List.of(
                servidorSnapshot(processo, unidadeOrigem.getCodigo(), servidor1),
                servidorSnapshot(processo, unidadeOrigem.getCodigo(), servidor2)
        ));
        when(usuarioService.buscarPorTitulos(List.of(servidor1.getTituloEleitoral(), servidor2.getTituloEleitoral())))
                .thenReturn(List.of(servidor1, servidor2));
        when(responsavelUnidadeService.buscarResponsavelUnidadeOpt(unidadeOrigem.getCodigo()))
                .thenReturn(java.util.Optional.empty());
        when(unidadeService.buscarMapaVigente(unidadeOrigem.getCodigo())).thenReturn(java.util.Optional.of(mapaVigente));

        ArgumentCaptor<Diagnostico> captor = ArgumentCaptor.forClass(Diagnostico.class);

        service.inicializarDiagnostico(subprocesso);

        verify(diagnosticoRepo).save(captor.capture());
        Diagnostico salvo = captor.getValue();

        assertThat(salvo.getAvaliacaoServidores()).hasSize(4);
        assertThat(salvo.getSituacaoCapacitacoes()).hasSize(4);
        assertThat(salvo.getAvaliacaoServidores())
                .allSatisfy(avaliacao -> {
                    assertThat(avaliacao.getDiagnostico()).isSameAs(salvo);
                    assertThat(avaliacao.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.AUTOAVALIACAO_NAO_INICIADA);
                    assertThat(avaliacao.getServidorNomeDiagnostico()).isNotBlank();
                });
        assertThat(salvo.getSituacaoCapacitacoes())
                .allSatisfy(ocupacao -> {
                    assertThat(ocupacao.getDiagnostico()).isSameAs(salvo);
                    assertThat(ocupacao.getUnidadeCodigoSnapshot()).isEqualTo(unidadeOrigem.getCodigo());
                    assertThat(ocupacao.getUnidadeSiglaSnapshot()).isEqualTo("ASSESSORIA_12");
                    assertThat(ocupacao.getUnidadeNomeSnapshot()).isEqualTo("Assessoria 12");
                });
    }

    @Test
    @DisplayName("inicializarDiagnostico deve usar o mapa vigente da unidade quando o subprocesso nao tiver mapa proprio")
    void inicializarDiagnostico_deveUsarMapaVigenteDaUnidade() {
        Subprocesso subprocesso = subprocessoDiagnostico(unidadeOrigem, SituacaoSubprocesso.DIAGNOSTICO_EM_ANDAMENTO);
        Processo processo = subprocesso.getProcesso();
        Mapa mapaVigente = mapa(101L, competencia(1L, "Competência vigente"));

        Usuario servidor = usuario("242426", "Duff McKagan");
        processo.adicionarServidoresParticipantes(List.of(
                servidorSnapshot(processo, unidadeOrigem.getCodigo(), servidor)
        ));
        when(usuarioService.buscarPorTitulos(List.of(servidor.getTituloEleitoral()))).thenReturn(List.of(servidor));
        when(responsavelUnidadeService.buscarResponsavelUnidadeOpt(unidadeOrigem.getCodigo()))
                .thenReturn(java.util.Optional.empty());
        when(unidadeService.buscarMapaVigente(unidadeOrigem.getCodigo())).thenReturn(java.util.Optional.of(mapaVigente));

        ArgumentCaptor<Diagnostico> captor = ArgumentCaptor.forClass(Diagnostico.class);

        service.inicializarDiagnostico(subprocesso);

        verify(diagnosticoRepo).save(captor.capture());
        Diagnostico salvo = captor.getValue();

        assertThat(salvo.getAvaliacaoServidores()).singleElement().satisfies(avaliacao ->
                assertThat(avaliacao.getCompetencia().getDescricao()).isEqualTo("Competência vigente"));
    }

    @Test
    @DisplayName("inicializarDiagnostico deve falhar com erro interno quando nao houver mapa vigente da unidade")
    void inicializarDiagnostico_deveFalharSemMapaVigente() {
        Subprocesso subprocesso = subprocessoDiagnostico(unidadeOrigem, SituacaoSubprocesso.DIAGNOSTICO_EM_ANDAMENTO);
        Processo processo = subprocesso.getProcesso();
        Usuario servidor = usuario("242426", "Duff McKagan");
        processo.adicionarServidoresParticipantes(List.of(
                servidorSnapshot(processo, unidadeOrigem.getCodigo(), servidor)
        ));
        when(usuarioService.buscarPorTitulos(List.of(servidor.getTituloEleitoral()))).thenReturn(List.of(servidor));
        when(responsavelUnidadeService.buscarResponsavelUnidadeOpt(unidadeOrigem.getCodigo()))
                .thenReturn(java.util.Optional.empty());
        when(unidadeService.buscarMapaVigente(unidadeOrigem.getCodigo())).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.inicializarDiagnostico(subprocesso))
                .hasMessageContaining("Processo de diagnóstico sem mapa vigente");
    }

    @Test
    @DisplayName("inicializarDiagnostico deve excluir o responsável da unidade das avaliações e situações de capacitação")
    void inicializarDiagnostico_deveExcluirResponsavelDaUnidade() {
        Subprocesso subprocesso = subprocessoDiagnostico(unidadeOrigem, SituacaoSubprocesso.DIAGNOSTICO_EM_ANDAMENTO);
        Processo processo = subprocesso.getProcesso();
        Mapa mapaVigente = mapa(101L, competencia(1L, "Competência 1"));

        Usuario responsavel = usuario("151515", "Axl Rose");
        Usuario servidor = usuario("242426", "Duff McKagan");
        processo.adicionarServidoresParticipantes(List.of(
                servidorSnapshot(processo, unidadeOrigem.getCodigo(), responsavel),
                servidorSnapshot(processo, unidadeOrigem.getCodigo(), servidor)
        ));
        when(usuarioService.buscarPorTitulos(List.of(servidor.getTituloEleitoral())))
                .thenReturn(List.of(servidor));
        when(responsavelUnidadeService.buscarResponsavelUnidadeOpt(unidadeOrigem.getCodigo()))
                .thenReturn(java.util.Optional.of(new sgc.organizacao.dto.UnidadeResponsavelDto(
                        unidadeOrigem.getCodigo(),
                        responsavel.getTituloEleitoral(),
                        responsavel.getNome(),
                        null,
                        null
                )));
        when(unidadeService.buscarMapaVigente(unidadeOrigem.getCodigo())).thenReturn(java.util.Optional.of(mapaVigente));

        ArgumentCaptor<Diagnostico> captor = ArgumentCaptor.forClass(Diagnostico.class);

        service.inicializarDiagnostico(subprocesso);

        verify(diagnosticoRepo).save(captor.capture());
        Diagnostico salvo = captor.getValue();

        assertThat(salvo.getAvaliacaoServidores())
                .extracting(avaliacao -> avaliacao.getServidor().getTituloEleitoral())
                .containsExactly("242426");
        assertThat(salvo.getSituacaoCapacitacoes())
                .extracting(situacao -> situacao.getServidor().getTituloEleitoral())
                .containsExactly("242426");
    }

    @Test
    @DisplayName("concluirDiagnosticoUnidade deve concluir diagnóstico, transicionar e notificar superior")
    void concluirDiagnosticoUnidade_deveConcluirEEncaminhar() {
        Long codSubprocesso = 90L;
        Diagnostico diagnostico = new Diagnostico();
        diagnostico.setCodigo(700L);
        Subprocesso subprocesso = subprocessoDiagnostico(unidadeOrigem, SituacaoSubprocesso.DIAGNOSTICO_EM_ANDAMENTO);
        subprocesso.setCodigo(codSubprocesso);

        when(repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso))).thenReturn(diagnostico);
        when(subprocessoConsultaService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);
        when(usuarioContextoService.usuarioAutenticado()).thenReturn(chefe);
        when(fluxoContextoService.buscarSuperiorImediato(unidadeOrigem.getCodigo())).thenReturn(unidadeSuperior);
        doNothing().when(validacaoService).validarConclusaoUnidade(diagnostico.getCodigo());
        doNothing().when(subprocessoValidacaoService)
                .validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_EM_ANDAMENTO);

        service.concluirDiagnosticoUnidade(codSubprocesso);

        assertThat(diagnostico.getDataConclusao()).isNotNull();
        assertThat(subprocesso.getSituacao()).isEqualTo(SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        assertThat(subprocesso.getDataFimEtapa1()).isNotNull();

        verify(transicaoService).registrarTransicaoSemEmail(any(RegistrarTransicaoCommand.class));
        verify(notificacaoService).notificarDiagnosticoConcluido(subprocesso, unidadeSuperior);
    }

    @Test
    @DisplayName("devolverDiagnostico deve retornar para autoavaliação concluída quando devolvido à unidade dona")
    void devolverDiagnostico_deveRetornarParaAutoavaliacaoDaUnidade() {
        Long codSubprocesso = 91L;
        String observacao = "Ajustar consenso pendente";
        Diagnostico diagnostico = new Diagnostico();
        diagnostico.setDataConclusao(LocalDateTime.now());
        AvaliacaoServidor avaliacaoAprovada = new AvaliacaoServidor();
        avaliacaoAprovada.setSituacaoServidor(SituacaoAvaliacaoServidor.CONSENSO_APROVADO);
        avaliacaoAprovada.setSituacaoServidorAnterior(SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
        AvaliacaoServidor avaliacaoImpossibilitada = new AvaliacaoServidor();
        avaliacaoImpossibilitada.setSituacaoServidor(SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA);
        avaliacaoImpossibilitada.setSituacaoServidorAnterior(SituacaoAvaliacaoServidor.CONSENSO_CRIADO);
        diagnostico.setAvaliacaoServidores(List.of(avaliacaoAprovada, avaliacaoImpossibilitada));

        Subprocesso subprocesso = subprocessoDiagnostico(unidadeOrigem, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        subprocesso.setCodigo(codSubprocesso);
        subprocesso.setDataFimEtapa1(LocalDateTime.now());

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setUnidadeOrigem(unidadeOrigem);
        movimentacao.setUnidadeDestino(unidadeSuperior);

        when(repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso))).thenReturn(diagnostico);
        when(subprocessoConsultaService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso)).thenReturn(unidadeSuperior);
        when(fluxoContextoService.buscarUnidadeDevolucaoObrigatoria(subprocesso, unidadeSuperior)).thenReturn(unidadeOrigem);
        when(usuarioContextoService.usuarioAutenticado()).thenReturn(chefe);
        doNothing().when(subprocessoValidacaoService)
                .validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);

        service.devolverDiagnostico(codSubprocesso, observacao);

        assertThat(diagnostico.getDataConclusao()).isNull();
        assertThat(subprocesso.getDataFimEtapa1()).isNotNull();
        assertThat(avaliacaoAprovada.getSituacaoServidor())
                .isEqualTo(SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
        assertThat(avaliacaoAprovada.getSituacaoServidorAnterior()).isNull();
        assertThat(avaliacaoImpossibilitada.getSituacaoServidor())
                .isEqualTo(SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA);
        assertThat(avaliacaoImpossibilitada.getSituacaoServidorAnterior()).isNull();

        ArgumentCaptor<RegistrarWorkflowAnaliseCommand> captor = ArgumentCaptor.forClass(RegistrarWorkflowAnaliseCommand.class);
        verify(transicaoService).registrarWorkflowComDestino(captor.capture());
        assertThat(captor.getValue().novaSituacao()).isEqualTo(SituacaoSubprocesso.DIAGNOSTICO_EM_ANDAMENTO);
        assertThat(captor.getValue().tipoAcaoAnalise()).isEqualTo(TipoAcaoAnalise.DEVOLUCAO_DIAGNOSTICO);
        assertThat(captor.getValue().unidadeDestino()).isEqualTo(unidadeOrigem);
        verify(notificacaoService).notificarDiagnosticoDevolvido(subprocesso, unidadeSuperior, unidadeOrigem, observacao);
    }

    @Test
    @DisplayName("validarDiagnostico deve marcar como validado e encaminhar para unidade superior")
    void validarDiagnostico_deveMarcarComoValidado() {
        Long codSubprocesso = 92L;
        Unidade unidadeGestora = unidade(5L, "COORD_11", "Coordenadoria 11", TipoUnidade.INTERMEDIARIA);
        Unidade unidadeDestino = unidade(2L, "SECRETARIA_1", "Secretaria 1", TipoUnidade.INTEROPERACIONAL);
        Subprocesso subprocesso = subprocessoDiagnostico(unidadeOrigem, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        subprocesso.setCodigo(codSubprocesso);

        when(subprocessoConsultaService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso)).thenReturn(unidadeGestora);
        when(fluxoContextoService.buscarSuperiorImediato(unidadeGestora.getCodigo())).thenReturn(unidadeDestino);
        when(usuarioContextoService.usuarioAutenticado()).thenReturn(chefe);
        doNothing().when(subprocessoValidacaoService)
                .validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);

        service.validarDiagnostico(codSubprocesso, "Pode seguir");

        ArgumentCaptor<RegistrarWorkflowAnaliseCommand> captor = ArgumentCaptor.forClass(RegistrarWorkflowAnaliseCommand.class);
        verify(transicaoService).registrarWorkflowComDestino(captor.capture());
        assertThat(captor.getValue().tipoAnalise()).isEqualTo(TipoAnalise.DIAGNOSTICO);
        assertThat(captor.getValue().tipoAcaoAnalise()).isEqualTo(TipoAcaoAnalise.ACEITE_DIAGNOSTICO);
        assertThat(captor.getValue().tipoTransicao()).isEqualTo(TipoTransicao.DIAGNOSTICO_ACEITO);
        assertThat(captor.getValue().unidadeDestino()).isEqualTo(unidadeDestino);
        verify(notificacaoService).notificarDiagnosticoAceito(subprocesso, unidadeGestora, unidadeDestino);
    }

    @Test
    @DisplayName("aceitarDiagnosticosEmBloco deve registrar aceite e consolidar notificações")
    void aceitarDiagnosticosEmBloco_deveRegistrarAceiteEConsolidarNotificacoes() {
        Unidade unidadeGestora = unidade(5L, "COORD_11", "Coordenadoria 11", TipoUnidade.INTERMEDIARIA);
        Unidade unidadeDestino = unidade(2L, "SECRETARIA_1", "Secretaria 1", TipoUnidade.INTEROPERACIONAL);
        Subprocesso subprocesso = subprocessoDiagnostico(unidadeOrigem, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        subprocesso.setCodigo(94L);

        when(subprocessoConsultaService.buscarSubprocesso(94L)).thenReturn(subprocesso);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso)).thenReturn(unidadeGestora);
        when(fluxoContextoService.buscarSuperiorImediato(unidadeGestora.getCodigo())).thenReturn(unidadeDestino);
        when(usuarioContextoService.usuarioAutenticado()).thenReturn(chefe);
        doNothing().when(subprocessoValidacaoService)
                .validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);

        service.aceitarDiagnosticosEmBloco(List.of(94L));

        ArgumentCaptor<RegistrarWorkflowAnaliseCommand> captor = ArgumentCaptor.forClass(RegistrarWorkflowAnaliseCommand.class);
        verify(transicaoService).registrarWorkflowComDestino(captor.capture());
        assertThat(captor.getValue().tipoAcaoAnalise()).isEqualTo(TipoAcaoAnalise.ACEITE_DIAGNOSTICO);
        assertThat(captor.getValue().tipoTransicao()).isEqualTo(TipoTransicao.DIAGNOSTICO_ACEITO);
        assertThat(captor.getValue().observacoes()).isNull();
        verify(notificacaoService).notificarDiagnosticosAceitosEmBloco(List.of(subprocesso), unidadeDestino);
    }

    @Test
    @DisplayName("homologarDiagnostico deve exigir validado e registrar homologação no admin")
    void homologarDiagnostico_deveHomologar() {
        Long codSubprocesso = 93L;
        Diagnostico diagnostico = new Diagnostico();
        Subprocesso subprocesso = subprocessoDiagnostico(unidadeOrigem, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        subprocesso.setCodigo(codSubprocesso);

        when(subprocessoConsultaService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);
        when(usuarioContextoService.usuarioAutenticado()).thenReturn(chefe);
        doNothing().when(subprocessoValidacaoService)
                .validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        doNothing().when(validacaoService).validarDiagnosticoHomologavel(codSubprocesso);

        service.homologarDiagnostico(codSubprocesso, "Homologado");

        assertThat(subprocesso.getSituacao()).isEqualTo(SituacaoSubprocesso.DIAGNOSTICO_HOMOLOGADO);
        ArgumentCaptor<RegistrarWorkflowAnaliseCommand> captor = ArgumentCaptor.forClass(RegistrarWorkflowAnaliseCommand.class);
        verify(transicaoService).registrarWorkflowDentroDoAdmin(captor.capture());
        assertThat(captor.getValue().tipoTransicao()).isEqualTo(TipoTransicao.DIAGNOSTICO_HOMOLOGADO);
        assertThat(captor.getValue().tipoAnalise()).isEqualTo(TipoAnalise.DIAGNOSTICO);
        assertThat(captor.getValue().tipoAcaoAnalise()).isEqualTo(TipoAcaoAnalise.HOMOLOGACAO_DIAGNOSTICO);
        assertThat(captor.getValue().modoComunicacao()).isEqualTo(RegistrarWorkflowAnaliseCommand.ModoComunicacaoWorkflow.SEM_EMAIL);
        verify(notificacaoService).notificarDiagnosticoHomologado(subprocesso);
    }

    @Test
    @DisplayName("homologarDiagnosticosEmBloco deve homologar sem notificação individual")
    void homologarDiagnosticosEmBloco_deveHomologarSemNotificacaoIndividual() {
        Long codSubprocesso = 95L;
        Subprocesso subprocesso = subprocessoDiagnostico(unidadeOrigem, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        subprocesso.setCodigo(codSubprocesso);

        when(subprocessoConsultaService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);
        when(usuarioContextoService.usuarioAutenticado()).thenReturn(chefe);
        doNothing().when(subprocessoValidacaoService)
                .validarSituacaoPermitida(subprocesso, SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        doNothing().when(validacaoService).validarDiagnosticoHomologavel(codSubprocesso);

        service.homologarDiagnosticosEmBloco(List.of(codSubprocesso));

        assertThat(subprocesso.getSituacao()).isEqualTo(SituacaoSubprocesso.DIAGNOSTICO_HOMOLOGADO);
        ArgumentCaptor<RegistrarWorkflowAnaliseCommand> captor = ArgumentCaptor.forClass(RegistrarWorkflowAnaliseCommand.class);
        verify(transicaoService).registrarWorkflowDentroDoAdmin(captor.capture());
        assertThat(captor.getValue().tipoTransicao()).isEqualTo(TipoTransicao.DIAGNOSTICO_HOMOLOGADO);
        assertThat(captor.getValue().observacoes()).isNull();
        assertThat(captor.getValue().modoComunicacao()).isEqualTo(RegistrarWorkflowAnaliseCommand.ModoComunicacaoWorkflow.SEM_COMUNICACOES);
        verify(notificacaoService, never()).notificarDiagnosticoHomologado(any());
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

    private ServidorProcesso servidorSnapshot(Processo processo, Long unidadeCodigo, Usuario usuario) {
        return ServidorProcesso.criarSnapshot(processo, unidadeCodigo, usuario);
    }
}
