package sgc.alerta;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import sgc.alerta.dto.*;
import sgc.subprocesso.model.*;

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
    private NotificacaoEmailService notificacaoEmailService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("listarResumoSubprocessosAtivos deve retornar resumo para admin")
    void listarResumoSubprocessosAtivosDeveRetornarResumoParaAdmin() throws Exception {
        when(notificacaoEmailService.listarResumoSubprocessosAtivos()).thenReturn(List.of(resumo()));

        mockMvc.perform(get("/api/admin/notificacoes/subprocessos-ativos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].subprocessoCodigo").value(60000))
                .andExpect(jsonPath("$[0].processoDescricao").value("Processo"))
                .andExpect(jsonPath("$[0].statusGeral").value("FALHA_DEFINITIVA"))
                .andExpect(jsonPath("$[0].podeReenviar").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("reenviarFalhasDefinitivas deve reenfileirar para admin")
    void reenviarFalhasDefinitivasDeveReenfileirarParaAdmin() throws Exception {
        when(notificacaoEmailService.reenfileirarFalhasDefinitivasPorSubprocesso(60000L)).thenReturn(2);

        mockMvc.perform(post("/api/admin/notificacoes/subprocessos/60000/reenviar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subprocessoCodigo").value(60000))
                .andExpect(jsonPath("$.reenfileiradas").value(2));
    }

    private NotificacaoSubprocessoResumoDto resumo() {
        return new NotificacaoSubprocessoResumoDto(
                60000L,
                1L,
                "Processo",
                "SEC",
                SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                3,
                0,
                0,
                1,
                0,
                2,
                StatusGeralNotificacao.FALHA_DEFINITIVA,
                LocalDateTime.of(2026, 4, 22, 9, 0),
                null,
                5,
                "SMTP fora",
                true
        );
    }
}
