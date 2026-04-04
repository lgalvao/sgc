package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.model.*;
import sgc.mapa.dto.*;
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
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

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

    @Test
    @DisplayName("criarParaRevisao deve lançar exceção se unidadeMapa não tem mapa vigente")
    void criarParaRevisaoDeveLancarExcecaoSeUnidadeMapaNaoTemMapaVigente() {
        Processo processo = new Processo();
        Unidade unidade = new Unidade();
        unidade.setSigla("UNIT");
        UnidadeMapa unidadeMapa = new UnidadeMapa();
        unidadeMapa.setMapaVigente(null);
        Unidade unidadeOrigem = new Unidade();
        Usuario usuario = new Usuario();

        assertThatThrownBy(() -> service.criarParaRevisao(processo, unidade, unidadeMapa, unidadeOrigem, usuario))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unidade UNIT sem mapa vigente para revisão/diagnóstico");
    }

    @Test
    @DisplayName("criarParaDiagnostico deve lançar exceção se unidadeMapa não tem mapa vigente")
    void criarParaDiagnosticoDeveLancarExcecaoSeUnidadeMapaNaoTemMapaVigente() {
        Processo processo = new Processo();
        Unidade unidade = new Unidade();
        unidade.setSigla("UNIT");
        UnidadeMapa unidadeMapa = new UnidadeMapa();
        unidadeMapa.setMapaVigente(null);
        Unidade unidadeOrigem = new Unidade();
        Usuario usuario = new Usuario();

        assertThatThrownBy(() -> service.criarParaDiagnostico(processo, unidade, unidadeMapa, unidadeOrigem, usuario))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unidade UNIT sem mapa vigente para revisão/diagnóstico");
    }

    @ParameterizedTest
    @CsvSource({
            "MAPEAMENTO_MAPA_CRIADO, MAPEAMENTO_CADASTRO_HOMOLOGADO",
            "REVISAO_MAPA_AJUSTADO, REVISAO_CADASTRO_HOMOLOGADA"
    })
    @DisplayName("removerCompetencia deve atualizar situação quando mapa fica vazio")
    void removerCompetenciaDeveAtualizarSituacaoQuandoMapaFicaVazio(SituacaoSubprocesso situacaoInicial, SituacaoSubprocesso situacaoFinal) {
        Long codSubprocesso = 1L;
        Long codCompetencia = 10L;
        Long codMapa = 100L;

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codSubprocesso);
        sp.setSituacao(situacaoInicial);
        Mapa mapa = new Mapa();
        mapa.setCodigo(codMapa);
        sp.setMapa(mapa);

        when(consultaService.buscarSubprocesso(codSubprocesso)).thenReturn(sp);
        when(mapaManutencaoService.competenciasCodMapa(codMapa)).thenReturn(List.of());
        when(mapaManutencaoService.mapaCodigo(codMapa)).thenReturn(mapa);

        service.removerCompetencia(codSubprocesso, codCompetencia);

        assertThat(sp.getSituacao()).isEqualTo(situacaoFinal);
        verify(subprocessoRepo).save(sp);
    }

    @ParameterizedTest
    @CsvSource({
            "MAPEAMENTO_CADASTRO_HOMOLOGADO, MAPEAMENTO_MAPA_CRIADO",
            "REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO"
    })
    @DisplayName("adicionarCompetencia deve atualizar situação quando mapa deixa de ser vazio")
    void adicionarCompetenciaDeveAtualizarSituacaoQuandoMapaDeixaDeSerVazio(SituacaoSubprocesso situacaoInicial, SituacaoSubprocesso situacaoFinal) {
        Long codSubprocesso = 1L;
        Long codMapa = 100L;
        CompetenciaRequest request = CompetenciaRequest.builder()
                .descricao("Nova Comp")
                .atividadesIds(List.of(10L))
                .build();

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codSubprocesso);
        sp.setSituacao(situacaoInicial);
        Mapa mapa = new Mapa();
        mapa.setCodigo(codMapa);
        sp.setMapa(mapa);

        when(consultaService.buscarSubprocesso(codSubprocesso)).thenReturn(sp);
        when(mapaManutencaoService.competenciasCodMapa(codMapa)).thenReturn(List.of());
        when(mapaManutencaoService.mapaCodigo(codMapa)).thenReturn(mapa);

        service.adicionarCompetencia(codSubprocesso, request);

        assertThat(sp.getSituacao()).isEqualTo(situacaoFinal);
        verify(subprocessoRepo).save(sp);
    }

    @ParameterizedTest
    @CsvSource({
            "MAPEAMENTO_CADASTRO_HOMOLOGADO, MAPEAMENTO_MAPA_CRIADO",
            "REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO"
    })
    @DisplayName("salvarMapaSubprocesso deve atualizar situação quando mapa deixa de ser vazio")
    void salvarMapaSubprocessoDeveAtualizarSituacaoQuandoMapaDeixaDeSerVazio(SituacaoSubprocesso situacaoInicial, SituacaoSubprocesso situacaoFinal) {
        Long codSubprocesso = 1L;
        Long codMapa = 100L;

        SalvarMapaRequest request = SalvarMapaRequest.builder()
                .competencias(List.of(SalvarMapaRequest.CompetenciaRequest.builder()
                        .descricao("Comp")
                        .atividadesCodigos(List.of(10L))
                        .build()))
                .build();

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codSubprocesso);
        sp.setSituacao(situacaoInicial);
        Mapa mapa = new Mapa();
        mapa.setCodigo(codMapa);
        sp.setMapa(mapa);

        when(consultaService.buscarSubprocesso(codSubprocesso)).thenReturn(sp);
        when(mapaManutencaoService.competenciasCodMapa(codMapa)).thenReturn(List.of());
        when(mapaSalvamentoService.salvarMapaCompleto(eq(codMapa), any())).thenReturn(mapa);

        service.salvarMapaSubprocesso(codSubprocesso, request);

        assertThat(sp.getSituacao()).isEqualTo(situacaoFinal);
        verify(subprocessoRepo).save(sp);
    }
}
