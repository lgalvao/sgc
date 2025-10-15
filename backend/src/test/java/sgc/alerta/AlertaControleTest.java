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
import sgc.comum.erros.ErroDominioNaoEncontrado;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AlertaControleTest {
    @Mock
    private AlertaService alertaService;

    @InjectMocks
    private AlertaControle alertaControle;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(alertaControle)
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
        doThrow(new ErroDominioNaoEncontrado("Não encontrado"))
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
}
