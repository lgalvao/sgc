package sgc.comum.erros;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sgc.seguranca.SgcPermissionEvaluator;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TestSecurityController.class)
@ActiveProfiles("test")
@Import(RestExceptionHandler.class)
class RestExceptionHandlerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SgcPermissionEvaluator permissionEvaluator;

    @Test
    void deveOcultarValorRejeitadoNoErroDeValidacao() throws Exception {
        String payload = "{\"dadoSensivel\": \"123\"}"; // Too short, min=5

        mockMvc.perform(post("/test/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.subErrors[0].rejectedValue").doesNotExist());
    }
}
