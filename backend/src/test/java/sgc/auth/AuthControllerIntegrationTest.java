package sgc.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import sgc.dto.LoginRequest;
import sgc.dto.PerfilUnidadeDTO;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfig {
        @Bean
        @Profile("test")
        public SistemaAcessoClient sistemaAcessoClient() {
            return new SistemaAcessoClient() {
                @Override
                public boolean authenticate(String titulo, String senha) {
                    return "validUser".equals(titulo) && "senha".equals(senha);
                }

                @Override
                public List<PerfilUnidadeDTO> fetchPerfis(String titulo) {
                    if ("validUser".equals(titulo)) {
                        return Collections.singletonList(new PerfilUnidadeDTO("CHEFE", 1L, "SESEL"));
                    }
                    return Collections.emptyList();
                }
            };
        }
    }

    @Test
    void postLogin_happy_path_returns_token_and_perfis() throws Exception {
        LoginRequest req = new LoginRequest("validUser", "senha");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.perfis").isArray())
                .andExpect(jsonPath("$.unidades").isArray());
    }

    @Test
    void postLogin_missing_titulo_returns_400() throws Exception {
        // titulo em branco -> validação @NotBlank deve causar 400
        String body = "{\"senha\":\"senha\"}";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}