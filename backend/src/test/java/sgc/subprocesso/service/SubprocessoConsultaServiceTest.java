package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
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

    @InjectMocks
    private SubprocessoConsultaService service;

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
    @DisplayName("obterSugestoes deve retornar string vazia quando mapa estiver ausente")
    void obterSugestoesDeveRetornarStringVaziaQuandoMapaEstiverAusente() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);

        when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(subprocesso));

        assertThat(service.obterSugestoes(1L)).containsEntry("sugestoes", "");
    }

    @Test
    @DisplayName("listarEntidadesPorProcessoEUnidades deve retornar vazio quando lista de unidades estiver vazia")
    void listarEntidadesPorProcessoEUnidadesDeveRetornarVazioQuandoListaDeUnidadesEstiverVazia() {
        assertThat(service.listarEntidadesPorProcessoEUnidades(1L, List.of())).isEmpty();
        verify(subprocessoRepo, never()).listarPorProcessoEUnidadesComUnidade(anyLong(), anyList());
    }
}
