package sgc.alerta;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.webmvc.test.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.security.test.context.support.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.web.servlet.*;
import sgc.alerta.model.*;
import sgc.comum.config.*;

import java.time.*;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificacaoAdminController.class)
@Import(AlertaDtoMapper.class)
@DisplayName("NotificacaoAdminController")

class NotificacaoAdminControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificacaoService notificacaoService;

    @MockitoBean
    private ConfigAplicacao configAplicacao;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("listar deve retornar lista de notificações individuais")
    void listarDeveRetornarListaDeNotificacoesIndividuais() throws Exception {
        NotificacaoEmail notificacao = NotificacaoEmail.builder()
                .codigo(123L)
                .assunto("Teste")
                .destinatario("teste@teste.com")
                .unidadeOrigemSigla("ADMIN")
                .tipoNotificacao(TipoNotificacao.PROCESSO_INICIADO)
                .situacao(SituacaoNotificacao.ENVIADO)
                .dataHoraCriacao(LocalDateTime.now())
                .build();

        when(notificacaoService.listarTodasAdmin(50)).thenReturn(List.of(notificacao));

        mockMvc.perform(get("/api/admin/notificacoes/listar").param("limite", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value(123))
                .andExpect(jsonPath("$[0].assunto").value("Teste"))
                .andExpect(jsonPath("$[0].destinatario").value("teste@teste.com"))
                .andExpect(jsonPath("$[0].unidadeOrigemSigla").value("ADMIN"))
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

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("buscarUrlLeitorEmailTestes deve retornar a URL configurada")
    void buscarUrlLeitorEmailTestesDeveRetornarUrlConfigurada() throws Exception {
        when(configAplicacao.getUrlLeitorEmailTestes()).thenReturn("https://seseldev05.tre-pe.gov.br:8025");

        mockMvc.perform(get("/api/admin/notificacoes/leitor-email-testes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://seseldev05.tre-pe.gov.br:8025"));
    }
}
