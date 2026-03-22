package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.mockito.quality.*;
import sgc.alerta.*;
import sgc.comum.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
    private SubprocessoValidacaoService validacaoService;

    @Mock
    private SubprocessoNotificacaoService notificacaoService;

    @Mock
    private UnidadeService unidadeService;

    @Mock
    private HierarquiaService hierarquiaService;

    @Mock
    private UsuarioFacade usuarioFacade;

    @Mock
    private ImpactoMapaService impactoMapaService;

    @Mock
    private MapaManutencaoService mapaManutencaoService;

    @Mock
    private EmailService emailService;

    @Mock
    private AlertaFacade alertaService;

    @Mock
    private ComumRepo repo;

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

        // Metodo privado, testado via registrarTransicao ou similar
        // Aqui vou usar Reflection para testar o metodo privado diretamente para garantir cobertura
        Unidade res = (Unidade) org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "obterUnidadeLocalizacao", sp);
        assertThat(res).isEqualTo(u);
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
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
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
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);

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
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);

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

        // Usando Reflection para invocar metodo privado
        org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "enviarAlertasReabertura", sp, "justificativa", false);

        verify(alertaService, times(1)).criarAlertaReaberturaCadastro(eq(p), eq(u), anyString());
        verify(alertaService, times(1)).criarAlertaReaberturaCadastroSuperior(eq(p), eq(sup1), eq(u));
        verify(alertaService, times(1)).criarAlertaReaberturaCadastroSuperior(eq(p), eq(sup2), eq(u));
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

        org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "enviarAlertasReabertura", sp, "justificativa", true);

        verify(alertaService).criarAlertaReaberturaRevisao(eq(p), eq(u), anyString());
        verify(alertaService).criarAlertaReaberturaRevisaoSuperior(eq(p), eq(sup), eq(u));
    }
}
