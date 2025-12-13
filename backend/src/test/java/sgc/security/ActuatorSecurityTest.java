package sgc.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // Standard
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc; // Project specific?
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import sgc.comum.util.TokenSimuladoUtil;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("secure-test")
@DisplayName("Testes de Segurança do Actuator")
class ActuatorSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve negar acesso anônimo ao Actuator")
    void deveNegarAcessoAnonimoAoActuator() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isUnauthorized()); // Expect 401
    }

    @Test
    @DisplayName("Deve permitir acesso de ADMIN ao Actuator")
    void devePermitirAcessoAdminAoActuator() throws Exception {
        // Gerar token simulado para ADMIN
        String json = "{\"tituloEleitoral\": \"123456789\", \"perfil\": \"ADMIN\"}";
        String jsonBase64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        String assinatura = TokenSimuladoUtil.assinar(jsonBase64);
        String token = jsonBase64 + "." + assinatura;

        mockMvc.perform(get("/actuator/health")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve negar acesso de usuário comum ao Actuator")
    void deveNegarAcessoUsuarioComumAoActuator() throws Exception {
        // Gerar token simulado para SERVIDOR (não ADMIN)
        String json = "{\"tituloEleitoral\": \"987654321\", \"perfil\": \"SERVIDOR\"}";
        String jsonBase64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        String assinatura = TokenSimuladoUtil.assinar(jsonBase64);
        String token = jsonBase64 + "." + assinatura;

        mockMvc.perform(get("/actuator/health")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden()); // Expect 403
    }
}
