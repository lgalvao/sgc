package sgc.processo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sgc.processo.dto.AcaoEmBlocoRequest;
import sgc.processo.dto.IniciarProcessoRequest;
import sgc.processo.model.AcaoProcesso;
import sgc.processo.service.ProcessoFacade;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoController Coverage Tests")
class ProcessoControllerCoverageTest {

    @Mock
    private ProcessoFacade processoFacade;

    @InjectMocks
    @Spy
    private ProcessoController controller;

    @Test
    @DisplayName("Deve retornar bad request quando processador for nulo")
    void deveRetornarBadRequestQuandoProcessadorNulo() {
        // Arrange
        IniciarProcessoRequest req = new IniciarProcessoRequest(null, List.of());
        doReturn(Collections.emptyMap()).when(controller).getProcessadoresInicio();

        // Act
        ResponseEntity<Object> response = controller.iniciar(1L, req);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("executarAcaoEmBloco chama facade e retorna 200")
    void executarAcaoEmBloco_Sucesso() {
        Long codigo = 1L;
        AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(List.of(10L), AcaoProcesso.ACEITAR, LocalDate.now());

        ResponseEntity<Void> response = controller.executarAcaoEmBloco(codigo, req);

        verify(processoFacade).executarAcaoEmBloco(codigo, req);
        assertEquals(200, response.getStatusCode().value());
    }
}
