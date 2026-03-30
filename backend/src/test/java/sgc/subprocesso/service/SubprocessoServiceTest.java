package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.model.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.time.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoService")
@SuppressWarnings("NullAway.Init")
class SubprocessoServiceTest {

    @Mock
    private ComumRepo repo;
    @Mock
    private AnaliseRepo analiseRepo;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private UsuarioFacade usuarioFacade;
    @Mock
    private ImpactoMapaService impactoMapaService;
    @Mock
    private MapaSalvamentoService mapaSalvamentoService;
    @Mock
    private SgcPermissionEvaluator permissionEvaluator;
    @Mock
    private MapaVisualizacaoService mapaVisualizacaoService;
    @Mock
    private SubprocessoValidacaoService validacaoService;
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private MovimentacaoRepo movimentacaoRepo;
    @Mock
    private CopiaMapaService copiaMapaService;
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private HierarquiaService hierarquiaService;
    @Mock
    private SubprocessoConsultaService consultaService;

    @InjectMocks
    private SubprocessoService service;



    @Test
    @DisplayName("criarEntidade deve persistir subprocesso e mapa associado")
    void criarEntidadeDevePersistirSubprocessoEMapaAssociado() {
        Processo processo = new Processo();
        processo.setCodigo(10L);

        Unidade unidade = new Unidade();
        unidade.setCodigo(20L);

        LocalDateTime dataLimiteEtapa1 = LocalDateTime.of(2026, 4, 30, 12, 0);
        CriarSubprocessoRequest request = CriarSubprocessoRequest.builder()
                .codProcesso(10L)
                .codUnidade(20L)
                .dataLimiteEtapa1(dataLimiteEtapa1)
                .build();

        Subprocesso subprocessoPrimeiroSave = new Subprocesso();
        subprocessoPrimeiroSave.setCodigo(100L);
        subprocessoPrimeiroSave.setProcesso(processo);
        subprocessoPrimeiroSave.setUnidade(unidade);
        subprocessoPrimeiroSave.setDataLimiteEtapa1(dataLimiteEtapa1);

        Mapa mapaSalvo = new Mapa();
        mapaSalvo.setCodigo(200L);
        mapaSalvo.setSubprocesso(subprocessoPrimeiroSave);

        when(repo.buscar(Processo.class, 10L)).thenReturn(processo);
        when(unidadeService.buscarPorCodigo(20L)).thenReturn(unidade);
        when(subprocessoRepo.save(any(Subprocesso.class)))
                .thenReturn(subprocessoPrimeiroSave)
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapaManutencaoService.salvarMapa(any(Mapa.class))).thenReturn(mapaSalvo);

        Subprocesso resultado = service.criarEntidade(request);

        verify(subprocessoRepo, times(2)).save(any(Subprocesso.class));
        verify(mapaManutencaoService).salvarMapa(argThat(mapa -> mapa.getSubprocesso().equals(subprocessoPrimeiroSave)));
        assertThat(resultado.getProcesso()).isEqualTo(processo);
        assertThat(resultado.getUnidade()).isEqualTo(unidade);
        assertThat(resultado.getDataLimiteEtapa1()).isEqualTo(dataLimiteEtapa1);
        assertThat(resultado.getMapa()).isEqualTo(mapaSalvo);
    }
}
