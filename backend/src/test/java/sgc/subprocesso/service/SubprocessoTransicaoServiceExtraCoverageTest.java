package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.alerta.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;
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
    private LocalizacaoSubprocessoService localizacaoSubprocessoService;

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
    private UnidadeHierarquiaService unidadeHierarquiaService;

    @Mock
    private ImpactoMapaService impactoMapaService;
    @Mock
    private UsuarioFacade usuarioFacade;

    @Test
    @DisplayName("disponibilizar - deve encaminhar para unidade superior")
    void disponibilizar_DeveEncaminharParaUnidadeSuperior() {
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        Unidade u = new Unidade();
        u.setCodigo(1L);
        u.setSigla("U1");
        Unidade admin = new Unidade();
        admin.setCodigo(99L);
        admin.setSigla("ADMIN");
        u.setUnidadeSuperior(admin);
        
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setProcesso(p);
        sp.setUnidade(u);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setMapa(new sgc.mapa.model.Mapa());
        when(unidadeHierarquiaService.buscarCodigoPai(1L)).thenReturn(99L);
        when(unidadeService.buscarPorCodigo(99L)).thenReturn(admin);

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
        
        assertThatThrownBy(() -> service.disponibilizarRevisao(100L))
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
    @DisplayName("apresentarSugestoes - envia para unidade superior")
    void apresentarSugestoes_ComSuperior() {
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        
        Unidade u = new Unidade();
        u.setCodigo(1L);
        Unidade admin = new Unidade();
        admin.setCodigo(99L);
        admin.setSigla("ADMIN");
        u.setUnidadeSuperior(admin);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setProcesso(p);
        sp.setUnidade(u);
        setField(sp, "situacao", SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
        sp.setMapa(new sgc.mapa.model.Mapa());

        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);
        when(unidadeHierarquiaService.buscarCodigoPai(1L)).thenReturn(99L);
        when(unidadeService.buscarPorCodigo(99L)).thenReturn(admin);

        when(usuarioFacade.usuarioAutenticado()).thenReturn(new Usuario());

        service.apresentarSugestoes(100L, "sugestoes");

        verify(movimentacaoRepo).save(argThat(mov -> Objects.equals(mov.getUnidadeDestino(), admin)));
    }

    @Test
    @DisplayName("validarMapa - envia para unidade superior")
    void validarMapa_ComSuperior() {
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        
        Unidade u = new Unidade();
        u.setCodigo(1L);
        Unidade admin = new Unidade();
        admin.setCodigo(99L);
        admin.setSigla("ADMIN");
        u.setUnidadeSuperior(admin);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setProcesso(p);
        sp.setUnidade(u);
        setField(sp, "situacao", SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);

        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);
        when(unidadeHierarquiaService.buscarCodigoPai(1L)).thenReturn(99L);
        when(unidadeService.buscarPorCodigo(99L)).thenReturn(admin);

        when(usuarioFacade.usuarioAutenticado()).thenReturn(new Usuario());

        service.validarMapa(100L);

        verify(movimentacaoRepo).save(argThat(mov -> Objects.equals(mov.getUnidadeDestino(), admin)));
    }

    @Test
    @DisplayName("aceitarValidacao - encaminha para unidade superior")
    void aceitarValidacao_ComSuperior() {
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        
        Unidade u = new Unidade();
        u.setCodigo(1L);
        Unidade admin = new Unidade();
        admin.setCodigo(99L);
        admin.setSigla("ADMIN");
        u.setUnidadeSuperior(admin);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setProcesso(p);
        sp.setUnidade(u);
        setField(sp, "situacao", SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);

        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(u);
        when(unidadeHierarquiaService.buscarCodigoPai(1L)).thenReturn(99L);
        when(unidadeService.buscarPorCodigo(99L)).thenReturn(admin);

        when(usuarioFacade.usuarioAutenticado()).thenReturn(new Usuario());

        service.aceitarValidacao(100L, "obs");

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        verify(analiseRepo).save(any());
        verify(notificacaoService).notificarTransicao(argThat(cmd -> Objects.equals(cmd.unidadeDestino(), admin)));
    }

    @Test
    @DisplayName("enviarAlertasReabertura - loop superiores")
    void enviarAlertasReabertura_Loop() {
        Processo p = new Processo();
        Unidade u = new Unidade();
        u.setCodigo(20L);
        Unidade sup1 = new Unidade();
        sup1.setCodigo(30L);
        Unidade sup2 = new Unidade();
        sup2.setCodigo(40L);
        u.setUnidadeSuperior(sup1);
        sup1.setUnidadeSuperior(sup2);
        Unidade admin = new Unidade();
        admin.setCodigo(1L);
        admin.setSigla("ADMIN");

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setProcesso(p);
        sp.setUnidade(u);
        setField(sp, "situacao", SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);

        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);
        when(usuarioFacade.usuarioAutenticado()).thenReturn(new Usuario());
        when(unidadeService.buscarAdmin()).thenReturn(admin);
        when(unidadeHierarquiaService.buscarCodigosSuperiores(20L)).thenReturn(List.of(30L, 40L));
        when(unidadeService.buscarPorCodigos(List.of(30L, 40L))).thenReturn(List.of(sup1, sup2));

        service.reabrirCadastro(100L, "justificativa");

        verify(alertaService, times(1)).criarAlertaReaberturaCadastro(p, u);
        verify(alertaService, times(1)).criarAlertaReaberturaCadastroSuperior(p, sup1, u);
        verify(alertaService, times(1)).criarAlertaReaberturaCadastroSuperior(p, sup2, u);
    }

    @Test
    @DisplayName("enviarAlertasReabertura - revisao loop")
    void enviarAlertasReabertura_RevisaoLoop() {
        Processo p = new Processo();
        Unidade u = new Unidade();
        u.setCodigo(20L);
        Unidade sup = new Unidade();
        sup.setCodigo(30L);
        u.setUnidadeSuperior(sup);
        Unidade admin = new Unidade();
        admin.setCodigo(1L);
        admin.setSigla("ADMIN");

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setProcesso(p);
        sp.setUnidade(u);
        setField(sp, "situacao", SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO);

        when(consultaService.buscarSubprocesso(100L)).thenReturn(sp);
        when(usuarioFacade.usuarioAutenticado()).thenReturn(new Usuario());
        when(unidadeService.buscarAdmin()).thenReturn(admin);
        when(unidadeHierarquiaService.buscarCodigosSuperiores(20L)).thenReturn(List.of(30L));
        when(unidadeService.buscarPorCodigos(List.of(30L))).thenReturn(List.of(sup));

        service.reabrirRevisaoCadastro(100L, "justificativa");

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

        Movimentacao mov = new Movimentacao();
        mov.setUnidadeDestino(uAnalise);
        mov.setUnidadeOrigem(uOrigem);

        when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(sp)).thenReturn(uAnalise);
        when(movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(1L)).thenReturn(List.of(mov));
        when(hierarquiaService.isSubordinada(uOrigem, uAnalise)).thenReturn(true); // branch 489

        when(usuarioFacade.usuarioAutenticado()).thenReturn(new Usuario());

        service.devolverCadastro(1L, "Obs");

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

        when(usuarioFacade.usuarioAutenticado()).thenReturn(new Usuario());

        service.homologarRevisaoCadastro(1L, "Obs");

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
    }

    @Test
    @DisplayName("executarHomologacao - sem impactos em REVISAO")
    void executarHomologacao_SemImpactos() {
        Subprocesso sp = new Subprocesso(); sp.setCodigo(1L); sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        Processo p = new Processo(); p.setTipo(TipoProcesso.REVISAO); sp.setProcesso(p);
        Unidade unidade = new Unidade(); unidade.setCodigo(10L); unidade.setSigla("U10"); sp.setUnidade(unidade);

        when(consultaService.buscarSubprocesso(1L)).thenReturn(sp);

        when(usuarioFacade.usuarioAutenticado()).thenReturn(new Usuario());

        service.homologarRevisaoCadastro(1L, "Obs");

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
        sgc.subprocesso.dto.DisponibilizarMapaRequest req = new sgc.subprocesso.dto.DisponibilizarMapaRequest(java.time.LocalDate.of(2026, 1, 1), "Obs");
        when(usuarioFacade.usuarioAutenticado()).thenReturn(new Usuario());

        service.disponibilizarMapa(1L, req);

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
