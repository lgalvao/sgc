package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.dto.UnidadeDto;
import sgc.processo.service.ProcessoFacade;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para UnidadeController")
class UnidadeControllerCoverageTest {

    @InjectMocks
    private UnidadeController controller;

    @Mock
    private UnidadeFacade unidadeService;
    @Mock
    private ProcessoFacade processoFacade;

    @Test
    @DisplayName("buscarArvoreComElegibilidade deve lidar com tipo REVISAO")
    void buscarArvoreComElegibilidadeRevisao() {
        when(processoFacade.buscarIdsUnidadesEmProcessosAtivos(any())).thenReturn(Collections.emptySet());
        when(unidadeService.buscarArvoreComElegibilidade(eq(true), anySet())).thenReturn(Collections.emptyList());

        var result = controller.buscarArvoreComElegibilidade("REVISAO", null);
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    @DisplayName("buscarArvoreComElegibilidade deve lidar com tipo DIAGNOSTICO")
    void buscarArvoreComElegibilidadeDiagnostico() {
        when(processoFacade.buscarIdsUnidadesEmProcessosAtivos(any())).thenReturn(Collections.emptySet());
        when(unidadeService.buscarArvoreComElegibilidade(eq(true), anySet())).thenReturn(Collections.emptyList());

        var result = controller.buscarArvoreComElegibilidade("DIAGNOSTICO", null);
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    @DisplayName("buscarArvoreComElegibilidade deve lidar com tipo MAPEAMENTO")
    void buscarArvoreComElegibilidadeMapeamento() {
        when(processoFacade.buscarIdsUnidadesEmProcessosAtivos(any())).thenReturn(Collections.emptySet());
        when(unidadeService.buscarArvoreComElegibilidade(eq(false), anySet())).thenReturn(Collections.emptyList());

        var result = controller.buscarArvoreComElegibilidade("MAPEAMENTO", null);
        assertEquals(200, result.getStatusCode().value());
    }
}
