package sgc.alerta;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sgc.alerta.erros.ErroAlerta;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AlertaControllerTest {
    @Mock
    private AlertaService alertaService;

    @InjectMocks
    private AlertaController alertaController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(alertaController)
                .setControllerAdvice(new sgc.comum.erros.RestExceptionHandler())
                .build();
    }

    @Test
    void marcarComoLido_quandoSucesso_deveRetornarOk() throws Exception {
        mockMvc.perform(post("/api/alertas/1/marcar-como-lido")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(alertaService).marcarComoLido(anyString(), anyLong());
    }

    @Test
    void marcarComoLido_quandoAlertaNaoEncontrado_deveRetornarNotFound() throws Exception {
        doThrow(new ErroEntidadeNaoEncontrada("Não encontrado"))
                .when(alertaService).marcarComoLido(anyString(), anyLong());

        mockMvc.perform(post("/api/alertas/1/marcar-como-lido")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void marcarComoLido_quandoErroGenerico_deveRetornarInternalServerError() throws Exception {
        doThrow(new RuntimeException("Erro genérico"))
                .when(alertaService).marcarComoLido(anyString(), anyLong());

        mockMvc.perform(post("/api/alertas/1/marcar-como-lido")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void marcarComoLido_quandoFalhaAlteracaoStatus_deveRetornarConflict() throws Exception {
        doThrow(new ErroAlerta("Falha ao alterar status do alerta"))
                .when(alertaService).marcarComoLido(anyString(), anyLong());

        mockMvc.perform(post("/api/alertas/1/marcar-como-lido")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }
}
