package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import sgc.integracao.mocks.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para validação do comportamento real do RestExceptionHandler
 * sob requisições HTTP reais.
 */
@Tag("integration")
@DisplayName("RestExceptionHandler — integração")
class RestExceptionHandlerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Deve tratar NoResourceFoundException (404) para rota inexistente")
    @WithMockChefe
    void tratarNoResourceFoundException() throws Exception {
        mockMvc.perform(get("/api/recurso-inexistente-total")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Deve tratar HttpMessageNotReadable (400) para JSON malformado")
    @WithMockChefe
    void tratarHttpMessageNotReadable() throws Exception {
        mockMvc.perform(post("/api/atividades")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mapaCodigo\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Requisição JSON malformada"));
    }

    @Test
    @DisplayName("Deve tratar MethodArgumentNotValidException (400) para DTO com dados inválidos")
    @WithMockChefe
    void tratarMethodArgumentNotValid() throws Exception {
        // Envia requisição sem os campos obrigatórios em CriarAtividadeRequest
        mockMvc.perform(post("/api/atividades")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mapaCodigo\":null, \"descricao\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A requisição contém dados de entrada inválidos."));
    }
}
