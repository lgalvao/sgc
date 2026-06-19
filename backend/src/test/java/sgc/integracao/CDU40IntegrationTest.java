package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import sgc.feedback.FeedbackRegistro;
import sgc.feedback.FeedbackRepo;
import sgc.feedback.FeedbackStatus;
import sgc.feedback.FeedbackTipo;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.integracao.mocks.WithMockServidor;

import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("CDU-40 — Consultar feedbacks enviados")
@ActiveProfiles({"test", "hom"})
class CDU40IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private FeedbackRepo feedbackRepo;

    @BeforeEach
    void seed() {
        feedbackRepo.deleteAll();

        feedbackRepo.save(FeedbackRegistro.builder()
                .tipo(FeedbackTipo.BUG)
                .nota("Erro na tela A")
                .usuarioCodigo("123")
                .usuarioNome("Fulano")
                .enviadoEm(OffsetDateTime.now().minusDays(1))
                .rota("/tela-a")
                .status(FeedbackStatus.NOVO)
                .build());

        feedbackRepo.save(FeedbackRegistro.builder()
                .tipo(FeedbackTipo.SUGESTAO)
                .nota("Melhoria na tela B")
                .usuarioCodigo("456")
                .usuarioNome("Beltrano")
                .enviadoEm(OffsetDateTime.now())
                .rota("/tela-b")
                .status(FeedbackStatus.NOVO)
                .build());
    }

    @Test
    @DisplayName("Deve listar feedbacks para ADMIN ordenados pelo mais recente")
    @WithMockAdmin
    void deveListarFeedbacksParaAdmin() throws Exception {
        mockMvc.perform(get("/api/feedback/listar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tipo").value("sugestao"))
                .andExpect(jsonPath("$[0].usuarioNome").value("Beltrano"))
                .andExpect(jsonPath("$[1].tipo").value("bug"))
                .andExpect(jsonPath("$[1].usuarioNome").value("Fulano"));
    }

    @Test
    @DisplayName("Não deve permitir listagem por usuário comum")
    @WithMockServidor
    void naoDevePermitirListagemPorUsuarioComum() throws Exception {
        mockMvc.perform(get("/api/feedback/listar"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar screenshot de feedback sem imagem")
    @WithMockAdmin
    void deveRetornar404ParaScreenshotInexistente() throws Exception {
        FeedbackRegistro registro = feedbackRepo.findAll().get(0);
        mockMvc.perform(get("/api/feedback/{codigo}/screenshot", registro.getCodigo()))
                .andExpect(status().isNotFound());
    }
}
