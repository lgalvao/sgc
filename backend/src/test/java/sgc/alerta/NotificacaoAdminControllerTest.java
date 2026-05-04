package sgc.alerta;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import sgc.alerta.model.*;

import java.time.*;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificacaoAdminController.class)
@DisplayName("NotificacaoAdminController")
@SuppressWarnings("NullAway.Init")
class NotificacaoAdminControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificacaoService notificacaoService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("listar deve retornar lista de notificações individuais")
    void listarDeveRetornarListaDeNotificacoesIndividuais() throws Exception {
        NotificacaoEmail notificacao = NotificacaoEmail.builder()
                .codigo(123L)
                .assunto("Teste")
                .destinatario("teste@teste.com")
                .situacao(SituacaoNotificacao.ENVIADO)
                .dataHoraCriacao(LocalDateTime.now())
                .build();

        when(notificacaoService.listarTodasAdmin(50)).thenReturn(List.of(notificacao));

        mockMvc.perform(get("/api/admin/notificacoes/listar").param("limite", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value(123))
                .andExpect(jsonPath("$[0].assunto").value("Teste"))
                .andExpect(jsonPath("$[0].destinatario").value("teste@teste.com"))
                .andExpect(jsonPath("$[0].situacao").value("ENVIADO"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("reenviar deve reenfileirar notificação específica")
    void reenviarDeveReenfileirarNotificacaoEspecifica() throws Exception {
        when(notificacaoService.reenviarPorCodigo(123L)).thenReturn(1);

        mockMvc.perform(post("/api/admin/notificacoes/123/reenviar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(123))
                .andExpect(jsonPath("$.reenfileiradas").value(1));
    }
}
