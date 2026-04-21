package sgc.alerta;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import sgc.alerta.model.*;
import sgc.seguranca.*;

import java.time.*;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificacaoEmailController.class)
@DisplayName("NotificacaoEmailController")
@SuppressWarnings("NullAway.Init")
class NotificacaoEmailControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificacaoEmailService notificacaoEmailService;

    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;

    @Test
    @WithMockUser
    @DisplayName("listarPorSubprocesso deve retornar notificacoes autorizadas")
    void listarPorSubprocessoDeveRetornarNotificacoesAutorizadas() throws Exception {
        when(permissionEvaluator.hasPermission(any(), eq(60000L), eq("Subprocesso"), eq("VISUALIZAR_SUBPROCESSO")))
                .thenReturn(true);
        when(notificacaoEmailService.listarPorSubprocesso(60000L, 5)).thenReturn(List.of(notificacao()));

        mockMvc.perform(get("/api/subprocessos/60000/notificacoes-email?limite=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value(10))
                .andExpect(jsonPath("$[0].subprocessoCodigo").value(60000))
                .andExpect(jsonPath("$[0].destinatario").value("destino@tre-pe.jus.br"))
                .andExpect(jsonPath("$[0].situacao").value("PENDENTE"));
    }

    private NotificacaoEmail notificacao() {
        return NotificacaoEmail.builder()
                .codigo(10L)
                .tipoNotificacao("PROCESSO_INICIADO")
                .destinatario("destino@tre-pe.jus.br")
                .assunto("Assunto")
                .corpoHtml("<p>corpo</p>")
                .situacao(SituacaoNotificacaoEmail.PENDENTE)
                .tentativas(0)
                .dataHoraCriacao(LocalDateTime.of(2026, 4, 21, 9, 0))
                .build();
    }
}
