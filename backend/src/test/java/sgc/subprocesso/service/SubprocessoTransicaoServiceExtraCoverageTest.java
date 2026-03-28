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

    @Test
    @DisplayName("obterUnidadeLocalizacao - código nulo deve retornar unidade base")
    void obterUnidadeLocalizacao_CodigoNulo() {
        Unidade u = new Unidade();
        u.setCodigo(1L);
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(null);
        sp.setUnidade(u);

        Unidade res = invokeMethod(service, "obterUnidadeLocalizacao", sp);
        assertThat(res).isEqualTo(u);
    }

    @Test
    @DisplayName("obterUnidadeLocalizacao - destino nulo")
    void obterUnidadeLocalizacao_DestinoNulo() {
        Unidade u = new Unidade();
        u.setCodigo(1L);
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setUnidade(u);

        Movimentacao mov = new Movimentacao();
        mov.setUnidadeDestino(null);
        when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(100L)).thenReturn(List.of(mov));

        Unidade res = invokeMethod(service, "obterUnidadeLocalizacao", sp);
        assertThat(res).isEqualTo(u);
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
        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(any())).thenReturn(new ArrayList<>());

        invokeMethod(service, "disponibilizar", sp, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO, TipoTransicao.CADASTRO_DISPONIBILIZADO, new Usuario());

        verify(movimentacaoRepo).save(argThat(mov -> mov.getUnidadeDestino().equals(admin)));
    }

    @Test
    @DisplayName("disponibilizarRevisao - deve lançar ErroValidacao se situação impediente")
    void disponibilizarRevisao_Impediente() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        
        when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(100L)).thenReturn(Optional.of(sp));
        doThrow(new sgc.comum.erros.ErroValidacao("erro"))
            .when(validacaoService).validarSituacaoPermitida(any(Subprocesso.class), eq(SituacaoSubprocesso.NAO_INICIADO), eq(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO));
        
        assertThatThrownBy(() -> service.disponibilizarRevisao(100L, new Usuario()))
                .isInstanceOf(sgc.comum.erros.ErroValidacao.class);
    }

    @Test
    @DisplayName("obterUltimaDataLimite - deve lançar IllegalStateException sem etapa 1")
    void obterUltimaDataLimite_SemEtapa1() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setDataLimiteEtapa1(null);
        sp.setDataLimiteEtapa2(LocalDateTime.now());

        assertThatThrownBy(() -> invokeMethod(service, "obterUltimaDataLimite", sp))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sem data limite da etapa 1");
    }

    @Test
    @DisplayName("obterUltimaDataLimite - deve lançar IllegalStateException se etapa 1 > etapa 2")
    void obterUltimaDataLimite_EtapasInvertidas() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        sp.setDataLimiteEtapa1(now.plusDays(2));
        sp.setDataLimiteEtapa2(now.plusDays(1));

        assertThatThrownBy(() -> invokeMethod(service, "obterUltimaDataLimite", sp))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("etapa 1 posterior à etapa 2");
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

        when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(100L)).thenReturn(Optional.of(sp));

        service.apresentarSugestoes(100L, "sugestoes", new Usuario());

        verify(movimentacaoRepo).save(argThat(mov -> mov.getUnidadeDestino().equals(u)));
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

        when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(100L)).thenReturn(Optional.of(sp));

        service.validarMapa(100L, new Usuario());

        verify(movimentacaoRepo).save(argThat(mov -> mov.getUnidadeDestino().equals(u)));
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

        when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(100L)).thenReturn(Optional.of(sp));

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
    @DisplayName("obterUltimaDataLimite - ambas nulas deve retornar null")
    void obterUltimaDataLimite_AmbasNulas() {
        Subprocesso sp = new Subprocesso();
        sp.setDataLimiteEtapa1(null);
        sp.setDataLimiteEtapa2(null);

        java.time.LocalDate res = invokeMethod(service, "obterUltimaDataLimite", sp);
        assertThat(res).isNull();
    }

    @Test
    @DisplayName("disponibilizarRevisao - com subprocesso não iniciado")
    void disponibilizarRevisao_ComNaoIniciado() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        setField(sp, "situacao", SituacaoSubprocesso.NAO_INICIADO);
        sp.setMapa(new sgc.mapa.model.Mapa());
        sp.getMapa().setCodigo(1000L);
        sp.setUnidade(new Unidade());
        sp.setProcesso(new Processo());

        when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(100L)).thenReturn(Optional.of(sp));
        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(anyLong())).thenReturn(List.of());

        service.disponibilizarRevisao(100L, new Usuario());

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        verify(subprocessoRepo).save(sp); // Save chamado na linha 165
    }
}
