package sgc.alerta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.integracao.mocks.TestSecurityConfig;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlertaController.class)
@Import(TestSecurityConfig.class)
@Tag("unit")
@DisplayName("AlertaController")
class AlertaControllerTest {

    private static final String TITULO_TESTE = "12345678901"; // 11 ou 12 dígitos
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AlertaFacade alertaService;

    @Test
    @DisplayName("marcarComoLidos_quandoSucesso_deveRetornarOk")
    void marcarComoLidos_quandoSucesso_deveRetornarOk() throws Exception {
        mockMvc.perform(post("/api/alertas/marcar-como-lidos")
                        .with(user(TITULO_TESTE))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1, 2, 3]"))
                .andExpect(status().isOk());

        verify(alertaService).marcarComoLidos(eq(TITULO_TESTE), anyList());
    }

    @Test
    @DisplayName("marcarComoLidos_quandoListaVazia_deveRetornarOk")
    void marcarComoLidos_quandoListaVazia_deveRetornarOk() throws Exception {
        mockMvc.perform(post("/api/alertas/marcar-como-lidos")
                        .with(user(TITULO_TESTE))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isOk());

        verify(alertaService).marcarComoLidos(eq(TITULO_TESTE), anyList());
    }

    @Test
    @DisplayName("listarAlertas_quandoSucesso_deveRetornarListaDeAlertas")
    void listarAlertas_quandoSucesso_deveRetornarListaDeAlertas() throws Exception {
        when(alertaService.listarAlertasPorUsuario(TITULO_TESTE))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/alertas")
                        .with(user(TITULO_TESTE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(alertaService).listarAlertasPorUsuario(TITULO_TESTE);
    }

    @Test
    @DisplayName("listarNaoLidos_quandoSucesso_deveRetornarListaDeAlertas")
    void listarNaoLidos_quandoSucesso_deveRetornarListaDeAlertas() throws Exception {
        when(alertaService.listarAlertasNaoLidos(TITULO_TESTE))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/alertas/nao-lidos")
                        .with(user(TITULO_TESTE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(alertaService).listarAlertasNaoLidos(TITULO_TESTE);
    }
}
