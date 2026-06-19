package sgc.integracao.mocks;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import sgc.organizacao.model.Perfil;
import sgc.seguranca.dto.AutenticarRequest;
import sgc.seguranca.dto.EntrarRequest;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Helper para realizar login completo em testes de integração.
 * Executa o fluxo real: login direto ou login seguido de entrar.
 */
@Component
public class TestLoginHelper {
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Realiza login completo e retorna o token JWT.
     */
    public String login(MockMvc mockMvc, String titulo, Perfil perfil, Long unidadeCodigo) throws Exception {
        AutenticarRequest loginRequest = new AutenticarRequest(titulo, "senha123");
        MvcResult loginResult = mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie jwtCookieDireto = loginResult.getResponse().getCookie("jwtToken");
        if (jwtCookieDireto != null) {
            return jwtCookieDireto.getValue();
        }

        Cookie preAuthCookie = loginResult.getResponse().getCookie("SGC_PRE_AUTH");
        if (preAuthCookie == null) {
            throw new IllegalStateException("Cookie de pré-autenticação não encontrado");
        }

        EntrarRequest entrarRequest = new EntrarRequest(perfil.name(), unidadeCodigo);
        MvcResult entrarResult = mockMvc.perform(post("/api/usuarios/entrar")
                        .cookie(preAuthCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrarRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Extrair token do cookie
        Cookie jwtCookie = entrarResult.getResponse().getCookie("jwtToken");
        if (jwtCookie == null) {
            throw new IllegalStateException("Cookie jwtToken não encontrado");
        }
        return jwtCookie.getValue();
    }

    /**
     * Login como ADMIN na unidade ADMIN (código 1 - raiz da hierarquia).
     */
    public String loginAdmin(MockMvc mockMvc, String titulo) throws Exception {
        return login(mockMvc, titulo, Perfil.ADMIN, 1L);
    }

    /**
     * Login como GESTOR.
     */
    public String loginGestor(MockMvc mockMvc, String titulo, Long unidadeCodigo) throws Exception {
        return login(mockMvc, titulo, Perfil.GESTOR, unidadeCodigo);
    }

    /**
     * Login como CHEFE.
     */
    public String loginChefe(MockMvc mockMvc, String titulo, Long unidadeCodigo) throws Exception {
        return login(mockMvc, titulo, Perfil.CHEFE, unidadeCodigo);
    }

    /**
     * Login como SERVIDOR.
     */
    public String loginServidor(MockMvc mockMvc, String titulo, Long unidadeCodigo) throws Exception {
        return login(mockMvc, titulo, Perfil.SERVIDOR, unidadeCodigo);
    }
}
