package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.test.util.*;
import sgc.comum.erros.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
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
    @DisplayName("listarEntidadesPorProcessoEUnidades deve retornar vazio quando lista de unidades estiver vazia")
    void listarEntidadesPorProcessoEUnidadesDeveRetornarVazioQuandoListaDeUnidadesEstiverVazia() {
        assertThat(service.listarEntidadesPorProcessoEUnidades(1L, List.of())).isEmpty();
        verify(subprocessoRepo, never()).listarPorProcessoEUnidadesComUnidade(anyLong(), anyList());
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
