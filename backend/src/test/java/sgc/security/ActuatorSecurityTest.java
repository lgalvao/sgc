package sgc.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import sgc.sgrh.autenticacao.GerenciadorJwt;
import sgc.sgrh.model.Perfil;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("secure-test")
@DisplayName("Testes de Segurança do Actuator")
class ActuatorSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GerenciadorJwt gerenciadorJwt;

    @Test
    @DisplayName("Deve negar acesso anônimo ao Actuator")
    void deveNegarAcessoAnonimoAoActuator() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isUnauthorized()); // Expect 401
    }

    @Test
    @DisplayName("Deve permitir acesso de ADMIN ao Actuator")
    void devePermitirAcessoAdminAoActuator() throws Exception {
        String token = gerenciadorJwt.gerarToken("123456789", Perfil.ADMIN, 1L);

        mockMvc.perform(get("/actuator/health")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve negar acesso de usuário comum ao Actuator")
    void deveNegarAcessoUsuarioComumAoActuator() throws Exception {
        String token = gerenciadorJwt.gerarToken("987654321", Perfil.SERVIDOR, 1L);

        mockMvc.perform(get("/actuator/health")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden()); // Expect 403
    }
}
