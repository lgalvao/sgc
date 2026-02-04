package sgc.processo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import sgc.processo.dto.AcaoEmBlocoRequest;
import sgc.processo.model.AcaoProcesso;
import sgc.processo.service.ProcessoFacade;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProcessoControllerCoverageTest {

    @InjectMocks
    private ProcessoController controller;

    @Mock
    private ProcessoFacade processoFacade;



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
