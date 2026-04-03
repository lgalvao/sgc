package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.alerta.*;
import sgc.mapa.dto.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoTransicaoService Extra Coverage Test")
class SubprocessoTransicaoServiceExtraCoverageTest {
    @InjectMocks
    private SubprocessoTransicaoService service;

    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private SubprocessoService subprocessoService;
    @Mock
    private SubprocessoConsultaService consultaService;

    @Mock
    private MovimentacaoRepo movimentacaoRepo;

    @Mock
    private AnaliseRepo analiseRepo;

    @Mock
    private AlertaFacade alertaService;

    @Mock
    private SubprocessoValidacaoService validacaoService;

    @Mock
    private SubprocessoNotificacaoService notificacaoService;

    @Mock
    private UnidadeService unidadeService;

    @Mock
    private MapaManutencaoService mapaManutencaoService;

    @Mock
    private HierarquiaService hierarquiaService;

    @Mock
    private ImpactoMapaService impactoMapaService;

    @Mock
    private UsuarioFacade usuarioFacade;

    @Mock
    private EmailService emailService;

    
    @BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient().when(consultaService.obterUnidadeLocalizacao(org.mockito.ArgumentMatchers.any(Subprocesso.class)))
                .thenAnswer(inv -> {
                    Subprocesso sp = inv.getArgument(0);
                    return sp.getLocalizacaoAtual() != null ? sp.getLocalizacaoAtual() : sp.getUnidade();
                });
        org.mockito.Mockito.lenient().when(impactoMapaService.verificarImpactos(org.mockito.ArgumentMatchers.any(Subprocesso.class), org.mockito.ArgumentMatchers.any()))
                .thenReturn(sgc.mapa.dto.ImpactoMapaResponse.semImpacto());
    }

    @Test
    @DisplayName("disponibilizar - destino nulo deve buscar ADMIN")
    void disponibilizar_DestinoNulo() {
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        Unidade u = new Unidade();
        u.setCodigo(1L);
        u.setSigla("U1");
        
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setProcesso(p);
        sp.setUnidade(u);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setMapa(new sgc.mapa.model.Mapa());

        Unidade admin = new Unidade();
        admin.setCodigo(99L);
        admin.setSigla("ADMIN");

        when(unidadeService.buscarPorSigla("ADMIN")).thenReturn(admin);

        invokeMethod(service, "disponibilizar", sp, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO, TipoTransicao.CADASTRO_DISPONIBILIZADO, new Usuario());

        verify(movimentacaoRepo).save(argThat(mov -> Objects.equals(mov.getUnidadeDestino(), admin)));
    }

    @Test
    @DisplayName("disponibilizarRevisao - deve lançar ErroValidacao se situação impediente")
    void disponibilizarRevisao_Impediente() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        
        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);
        doThrow(new sgc.comum.erros.ErroValidacao("erro"))
            .when(validacaoService).validarSituacaoPermitida(any(Subprocesso.class), eq(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO));
        
        assertThatThrownBy(() -> service.disponibilizarRevisao(100L, new Usuario()))
                .isInstanceOf(sgc.comum.erros.ErroValidacao.class);
    }

    @Test
    @DisplayName("obterSituacaoObrigatoria - deve lançar IllegalStateException para situação não configurada")
    void obterSituacaoObrigatoria_Inexistente() {
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(p);
        
        Map<TipoProcesso, SituacaoSubprocesso> situacoes = Map.of(TipoProcesso.REVISAO, SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO);
        
        assertThatThrownBy(() -> invokeMethod(service, "obterSituacaoObrigatoria", situacoes, sp, "contexto"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sem situação configurada");
    }

    @Test
    @DisplayName("apresentarSugestoes - sem unidade superior")
    void apresentarSugestoes_SemSuperior() {
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        
        Unidade u = new Unidade();
        u.setCodigo(1L);
        u.setUnidadeSuperior(null);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setProcesso(p);
        sp.setUnidade(u);
        setField(sp, "situacao", SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
        sp.setMapa(new sgc.mapa.model.Mapa());

        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);

        service.apresentarSugestoes(100L, "sugestoes", new Usuario());

        verify(movimentacaoRepo).save(argThat(mov -> Objects.equals(mov.getUnidadeDestino(), u)));
    }

    @Test
    @DisplayName("validarMapa - sem unidade superior")
    void validarMapa_SemSuperior() {
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        
        Unidade u = new Unidade();
        u.setCodigo(1L);
        u.setUnidadeSuperior(null);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setProcesso(p);
        sp.setUnidade(u);
        setField(sp, "situacao", SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);

        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);

        service.validarMapa(100L, new Usuario());

        verify(movimentacaoRepo).save(argThat(mov -> Objects.equals(mov.getUnidadeDestino(), u)));
    }

    @Test
    @DisplayName("aceitarValidacao - sem unidade superior (homologa)")
    void aceitarValidacao_SemSuperior() {
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        
        Unidade u = new Unidade();
        u.setCodigo(1L);
        u.setUnidadeSuperior(null);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setProcesso(p);
        sp.setUnidade(u);
        setField(sp, "situacao", SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);

        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);

        service.aceitarValidacao(100L, "obs", new Usuario());

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        verify(analiseRepo).save(any());
    }

    @Test
    @DisplayName("enviarAlertasReabertura - loop superiores")
    void enviarAlertasReabertura_Loop() {
        Processo p = new Processo();
        Unidade u = new Unidade();
        Unidade sup1 = new Unidade();
        Unidade sup2 = new Unidade();
        u.setUnidadeSuperior(sup1);
        sup1.setUnidadeSuperior(sup2);

        Subprocesso sp = new Subprocesso();
        sp.setProcesso(p);
        sp.setUnidade(u);

        invokeMethod(service, "enviarAlertasReabertura", sp, "justificativa", false);

        verify(alertaService, times(1)).criarAlertaReaberturaCadastro(p, u);
        verify(alertaService, times(1)).criarAlertaReaberturaCadastroSuperior(p, sup1, u);
        verify(alertaService, times(1)).criarAlertaReaberturaCadastroSuperior(p, sup2, u);
    }

    @Test
    @DisplayName("enviarAlertasReabertura - revisao loop")
    void enviarAlertasReabertura_RevisaoLoop() {
        Processo p = new Processo();
        Unidade u = new Unidade();
        Unidade sup = new Unidade();
        u.setUnidadeSuperior(sup);

        Subprocesso sp = new Subprocesso();
        sp.setProcesso(p);
        sp.setUnidade(u);

        invokeMethod(service, "enviarAlertasReabertura", sp, "justificativa", true);

        verify(alertaService).criarAlertaReaberturaRevisao(p, u, "justificativa");
        verify(alertaService).criarAlertaReaberturaRevisaoSuperior(p, sup, u);
    }

    @Test
    @DisplayName("iniciarRevisaoCadastro - com subprocesso não iniciado")
    void iniciarRevisaoCadastro_ComNaoIniciado() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        setField(sp, "situacao", SituacaoSubprocesso.NAO_INICIADO);
        sp.setMapa(new sgc.mapa.model.Mapa());
        sp.getMapa().setCodigo(1000L);
        sp.setUnidade(new Unidade());
        sp.setProcesso(Processo.builder().tipo(TipoProcesso.REVISAO).build());

        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);

        service.iniciarRevisaoCadastro(100L);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("executarDevolucao - deve encontrar unidade de devolução subordinada")
    void executarDevolucao_EncontraSubordinada() {
        Subprocesso sp = new Subprocesso(); sp.setCodigo(1L); sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        Unidade uBase = new Unidade(); uBase.setCodigo(10L); sp.setUnidade(uBase);
        Processo p = new Processo(); p.setTipo(TipoProcesso.MAPEAMENTO); sp.setProcesso(p);

        Unidade uAnalise = new Unidade(); uAnalise.setCodigo(20L);
        Unidade uOrigem = new Unidade(); uOrigem.setCodigo(15L);
        sp.setLocalizacaoAtual(uAnalise);

        Movimentacao mov = new Movimentacao();
        mov.setUnidadeDestino(uAnalise);
        mov.setUnidadeOrigem(uOrigem);

        when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
        when(movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(1L)).thenReturn(List.of(mov));
        when(hierarquiaService.isSubordinada(uOrigem, uAnalise)).thenReturn(true); // branch 489

        service.devolverCadastro(1L, new Usuario(), "Obs");

        // Deve ter devolvido para uOrigem (destino da transicao)
        verify(notificacaoService).notificarTransicao(argThat(cmd -> cmd.unidadeDestino().equals(uOrigem)));
    }

    @Test
    @DisplayName("executarHomologacao - com impactos em REVISAO")
    void executarHomologacao_ComImpactos() {
        Subprocesso sp = new Subprocesso(); sp.setCodigo(1L); sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        Processo p = new Processo(); p.setTipo(TipoProcesso.REVISAO); sp.setProcesso(p);
        Unidade unidade = new Unidade(); unidade.setCodigo(10L); unidade.setSigla("U10"); sp.setUnidade(unidade);

        when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);

        service.homologarRevisaoCadastro(1L, new Usuario(), "Obs"); // branch 583 (temImpactos = true)

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
    }

    @Test
    @DisplayName("executarHomologacao - sem impactos em REVISAO")
    void executarHomologacao_SemImpactos() {
        Subprocesso sp = new Subprocesso(); sp.setCodigo(1L); sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        Processo p = new Processo(); p.setTipo(TipoProcesso.REVISAO); sp.setProcesso(p);
        Unidade unidade = new Unidade(); unidade.setCodigo(10L); unidade.setSigla("U10"); sp.setUnidade(unidade);

        when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);

        service.homologarRevisaoCadastro(1L, new Usuario(), "Obs"); // branch 583 (temImpactos = false)

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
    }

    @Test
    @DisplayName("executarDisponibilizacaoMapa - valida data limite igual")
    void disponibilizarMapa_DataIgual() {
        Subprocesso sp = new Subprocesso(); sp.setCodigo(1L); 
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        Processo p = new Processo(); p.setTipo(TipoProcesso.MAPEAMENTO); sp.setProcesso(p);
        sp.setMapa(new sgc.mapa.model.Mapa()); sp.getMapa().setCodigo(100L);
        sp.setDataLimiteEtapa1(LocalDateTime.of(2026, 1, 1, 0, 0));
        
        when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
        when(unidadeService.buscarPorSigla("ADMIN")).thenReturn(new Unidade());

        sgc.subprocesso.dto.DisponibilizarMapaRequest req = new sgc.subprocesso.dto.DisponibilizarMapaRequest(java.time.LocalDate.of(2026, 1, 1), "Obs");
        service.disponibilizarMapa(1L, req, new Usuario()); // branch 228 (ultima != null && isBefore = false)

        assertThat(sp.getDataLimiteEtapa2()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
    }

    @Test
    @DisplayName("obterUltimaDataLimite - retorna nulo quando etapa1 e etapa2 ausentes")
    void obterUltimaDataLimite_semDatas() {
        Subprocesso sp = new Subprocesso();

        LocalDate resultado = invokeMethod(service, "obterUltimaDataLimite", sp);

        assertThat(resultado).isNull();
    }

    @Test
    @DisplayName("obterUltimaDataLimite - lança erro quando etapa2 existe sem etapa1")
    void obterUltimaDataLimite_etapa2SemEtapa1() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(123L);
        sp.setDataLimiteEtapa2(LocalDateTime.now().plusDays(5));

        assertThatThrownBy(() -> invokeMethod(service, "obterUltimaDataLimite", sp))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sem data limite da etapa 1");
    }

    @Test
    @DisplayName("obterUltimaDataLimite - lança erro quando etapa1 é posterior à etapa2")
    void obterUltimaDataLimite_etapa1PosteriorEtapa2() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(456L);
        sp.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        sp.setDataLimiteEtapa2(LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> invokeMethod(service, "obterUltimaDataLimite", sp))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("etapa 1 posterior à etapa 2");
    }
}
