package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.test.util.*;
import sgc.comum.erros.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.dto.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoConsultaService")
@SuppressWarnings("NullAway.Init")
class SubprocessoConsultaServiceTest {
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private AnaliseRepo analiseRepo;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private UsuarioFacade usuarioFacade;
    @Mock
    private LocalizacaoSubprocessoService localizacaoSubprocessoService;
    @Mock
    private HierarquiaService hierarquiaService;
    @Mock
    private sgc.mapa.service.MapaManutencaoService mapaManutencaoService;

    @InjectMocks
    private SubprocessoConsultaService service;

    @BeforeEach
    void configurarDependenciasAdicionais() {
        ReflectionTestUtils.setField(service, "analiseHistoricoService", new AnaliseHistoricoService(unidadeService));
    }

    @Test
    @DisplayName("buscarSubprocesso deve falhar quando codigo nao existir")
    void buscarSubprocessoDeveFalharQuandoCodigoNaoExistir() {
        when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarSubprocesso(99L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("Subprocesso")
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("obterSugestoes deve retornar string vazia quando mapa estiver ausente fora de etapa de mapa")
    void obterSugestoesDeveRetornarStringVaziaQuandoMapaEstiverAusenteForaDeEtapaDeMapa() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(subprocesso));

        assertThat(service.obterSugestoes(1L)).containsEntry("sugestoes", "");
    }

    @Test
    @DisplayName("obterSugestoes deve falhar quando mapa estiver ausente em etapa de mapa")
    void obterSugestoesDeveFalharQuandoMapaEstiverAusenteEmEtapaDeMapa() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);

        when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(subprocesso));

        assertThatThrownBy(() -> service.obterSugestoes(1L))
                .isInstanceOf(ErroInconsistenciaInterna.class)
                .hasMessageContaining("sem mapa vinculado para leitura de sugestoes");
    }

    @Test
    @DisplayName("listarPorProcessoEUnidadeCodigosESituacoes deve retornar vazio se unidades for vazia")
    void listarPorProcessoEUnidadesCodigosESituacoesDeveRetornarVazioSeUnidadesVazia() {
        assertThat(service.listarPorProcessoEUnidadeCodigosESituacoes(1L, List.of(), List.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO))).isEmpty();
        verify(subprocessoRepo, never()).listarPorProcessoEUnidadesComUnidade(anyLong(), anyList());
    }

    @Test
    @DisplayName("listarPorProcessoEUnidadeCodigosESituacoes deve retornar vazio se situacoes for vazia")
    void listarPorProcessoEUnidadesCodigosESituacoesDeveRetornarVazioSeSituacoesVazia() {
        assertThat(service.listarPorProcessoEUnidadeCodigosESituacoes(1L, List.of(1L), List.of())).isEmpty();
        verify(subprocessoRepo, never()).listarPorProcessoEUnidadesComUnidade(anyLong(), anyList());
    }

    @Test
    @DisplayName("listarEntidadesPorProcessoEUnidades deve retornar vazio quando lista de unidades estiver vazia")
    void listarEntidadesPorProcessoEUnidadesDeveRetornarVazioQuandoListaDeUnidadesEstiverVazia() {
        assertThat(service.listarEntidadesPorProcessoEUnidades(1L, List.of())).isEmpty();
        verify(subprocessoRepo, never()).listarPorProcessoEUnidadesComUnidade(anyLong(), anyList());
    }

    @Test
    @DisplayName("obterContextoCadastroAtividades deve retornar contexto")
    void obterContextoCadastroAtividadesDeveRetornarContexto() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("U10");
        unidade.setNome("Unidade 10");
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sgc.processo.model.Processo proc = new sgc.processo.model.Processo();
        proc.setCodigo(5L);
        subprocesso.setProcesso(proc);

        sgc.mapa.model.Mapa mapa = new sgc.mapa.model.Mapa();
        mapa.setCodigo(100L);
        mapa.setSubprocesso(subprocesso);
        subprocesso.setMapa(mapa);

        when(subprocessoRepo.buscarPorCodigoComMapa(1L)).thenReturn(Optional.of(subprocesso));

        ContextoUsuarioAutenticado ctxUsuario = new ContextoUsuarioAutenticado("123", 20L, sgc.organizacao.model.Perfil.SERVIDOR);
        when(usuarioFacade.contextoAutenticado()).thenReturn(ctxUsuario);

        when(unidadeService.buscarPorCodigoComSuperior(20L)).thenReturn(new Unidade());

        Unidade locAtual = new Unidade();
        locAtual.setSigla("LOC");
        when(localizacaoSubprocessoService.obterLocalizacaoAtual(subprocesso)).thenReturn(locAtual);

        var result = service.obterContextoCadastroAtividades(1L);

        assertThat(result).isNotNull();
        assertThat(result.mapa()).isNotNull();
        assertThat(result.mapa().codigo()).isEqualTo(100L);
    }

    @Test
    @DisplayName("listarHistoricoCadastro deve carregar unidades em lote")
    void listarHistoricoCadastroDeveCarregarUnidadesEmLote() {
        Analise analise1 = new Analise();
        analise1.setUnidadeCodigo(10L);
        analise1.setTipo(TipoAnalise.CADASTRO);

        Analise analise2 = new Analise();
        analise2.setUnidadeCodigo(20L);
        analise2.setTipo(TipoAnalise.CADASTRO);

        when(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(1L)).thenReturn(List.of(analise1, analise2));
        when(unidadeService.buscarResumosPorCodigos(List.of(10L, 20L))).thenReturn(List.of(
                new UnidadeResumoLeitura(10L, "Unidade 10", "U10", TipoUnidade.OPERACIONAL),
                new UnidadeResumoLeitura(20L, "Unidade 20", "U20", TipoUnidade.OPERACIONAL)
        ));

        assertThat(service.listarHistoricoCadastro(1L)).hasSize(2);
        verify(unidadeService).buscarResumosPorCodigos(List.of(10L, 20L));
        verify(unidadeService, never()).buscarPorCodigo(anyLong());
    }
}
