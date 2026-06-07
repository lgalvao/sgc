package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.*;
import sgc.integracao.mocks.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para validação do comportamento real do RestExceptionHandler
 * sob requisições HTTP reais.
 */
@Tag("integration")
@Transactional
@DisplayName("RestExceptionHandler — integração")
class RestExceptionHandlerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Deve tratar NoResourceFoundException (404) para rota inexistente")
    @WithMockChefe
    void tratarNoResourceFoundException() throws Exception {
        mockMvc.perform(get("/api/recurso-inexistente-total"))
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
        mockMvc.perform(post("/api/atividades")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mapaCodigo\":null, \"descricao\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A requisição contém dados de entrada inválidos."));
    }

    @Test
    @DisplayName("Deve tratar AccessDeniedException (403) quando perfil não possui acesso")
    @WithMockChefe
    void tratarAccessDeniedException() throws Exception {
        mockMvc.perform(get("/api/configuracoes"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Acesso negado."));
    }

    @Test
    @DisplayName("Deve tratar ErroInterno (500) quando configuração inexistente é atualizada")
    @WithMockAdmin
    void tratarErroInterno() throws Exception {
        mockMvc.perform(post("/api/configuracoes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"codigo\":999999,\"chave\":\"CHAVE_INEXISTENTE\",\"descricao\":\"Teste\",\"valor\":\"1\"}]"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Erro interno do sistema."))
                .andExpect(jsonPath("$.code").value("ERRO_INTERNO"));
    }

    @Test
    @DisplayName("Deve tratar IllegalArgumentException remanescente como erro interno (500)")
    @WithMockChefe
    void tratarIllegalArgumentException() throws Exception {
        mockMvc.perform(get("/api/unidades/arvore-com-elegibilidade")
                        .param("tipoProcesso", "TIPO_INVALIDO"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Erro inesperado."))
                .andExpect(jsonPath("$.code").value("ERRO_INTERNO"));
    }

    @Test
    @DisplayName("Deve tratar ErroNegocioBase (404) para unidade inexistente")
    @WithMockChefe
    void tratarErroNegocioBase() throws Exception {
        mockMvc.perform(get("/api/unidades/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("&#39;Unidade&#39; com codigo &#39;999999&#39; não encontrado(a)."))
                .andExpect(jsonPath("$.code").value("ENTIDADE_NAO_ENCONTRADA"));
    }
}
