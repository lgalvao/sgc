package sgc.alerta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.alerta.erros.ErroAlerta;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.RestExceptionHandler;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlertaController.class)
@Import(RestExceptionHandler.class)
class AlertaControllerTest {

    @MockitoBean private AlertaService alertaService;

    @Autowired private MockMvc mockMvc;

    @Test
    @DisplayName("marcarComoLido_quandoSucesso_deveRetornarOk")
    @WithMockUser
    void marcarComoLido_quandoSucesso_deveRetornarOk() throws Exception {
        mockMvc.perform(post("/api/alertas/1/marcar-como-lido")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(alertaService).marcarComoLido(anyString(), anyLong());
    }

    @Test
    @DisplayName("marcarComoLido_quandoAlertaNaoEncontrado_deveRetornarNotFound")
    @WithMockUser
    void marcarComoLido_quandoAlertaNaoEncontrado_deveRetornarNotFound() throws Exception {
        doThrow(new ErroEntidadeNaoEncontrada("Não encontrado"))
                .when(alertaService).marcarComoLido(anyString(), anyLong());

        mockMvc.perform(post("/api/alertas/1/marcar-como-lido")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("marcarComoLido_quandoErroGenerico_deveRetornarInternalServerError")
    @WithMockUser
    void marcarComoLido_quandoErroGenerico_deveRetornarInternalServerError() throws Exception {
        doThrow(new RuntimeException("Erro genérico"))
                .when(alertaService).marcarComoLido(anyString(), anyLong());

        mockMvc.perform(post("/api/alertas/1/marcar-como-lido")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("marcarComoLido_quandoFalhaAlteracaoStatus_deveRetornarConflict")
    @WithMockUser
    void marcarComoLido_quandoFalhaAlteracaoStatus_deveRetornarConflict() throws Exception {
        doThrow(new ErroAlerta("Falha ao alterar status do alerta"))
                .when(alertaService).marcarComoLido(anyString(), anyLong());

        mockMvc.perform(post("/api/alertas/1/marcar-como-lido")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }
}
