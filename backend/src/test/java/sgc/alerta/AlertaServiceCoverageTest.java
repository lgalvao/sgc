package sgc.alerta;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.alerta.model.*;
import sgc.comum.model.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertaService - Cobertura adicional")
class AlertaServiceCoverageTest {

    @Mock
    private AlertaRepo alertaRepo;

    @Mock
    private AlertaUsuarioRepo alertaUsuarioRepo;

    @InjectMocks
    private AlertaService alertaService;

    @Test
    @DisplayName("dataHoraLeituraAlertaUsuario deve retornar vazio quando nao encontra")
    void deveRetornarVazioSeNaoEncontrarAlerta() {
        when(alertaUsuarioRepo.findById(any())).thenReturn(Optional.empty());
        Optional<LocalDateTime> dataHora = alertaService.dataHoraLeituraAlertaUsuario(1L, "titulo");
        assertThat(dataHora).isEmpty();
    }
}
