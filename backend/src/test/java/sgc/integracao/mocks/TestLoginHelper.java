package sgc.integracao.mocks;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import sgc.organizacao.model.Perfil;
import sgc.seguranca.dto.AutenticarRequest;
import sgc.seguranca.dto.AutorizarRequest;
import sgc.seguranca.dto.EntrarRequest;
import sgc.seguranca.dto.EntrarResponse;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Helper para realizar login completo em testes de integração.
 * Executa o fluxo real: autenticar -> autorizar -> entrar.
 */
@Component
public class TestLoginHelper {
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Realiza login completo e retorna o token JWT.
     *
     * @param titulo Título eleitoral do usuário
     * @param perfil Perfil desejado
     * @param unidadeCodigo Código da unidade
     * @return Token JWT para uso nos headers
     */
    public String login(MockMvc mockMvc, String titulo, Perfil perfil, Long unidadeCodigo) throws Exception {
        // 1. Autenticar
        AutenticarRequest autenticarRequest = new AutenticarRequest(titulo, "senha123");
        MvcResult autenticarResult = mockMvc.perform(post("/api/usuarios/autenticar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(autenticarRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Extrair cookie de pré-autenticação
        Cookie preAuthCookie = autenticarResult.getResponse().getCookie("SGC_PRE_AUTH");
        if (preAuthCookie == null) {
            throw new IllegalStateException("Cookie de pré-autenticação não encontrado");
        }

        // 2. Autorizar (não precisa do resultado, mas faz parte do fluxo)
        AutorizarRequest autorizarRequest = new AutorizarRequest(titulo);
        mockMvc.perform(post("/api/usuarios/autorizar")
                        .cookie(preAuthCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(autorizarRequest)))
                .andExpect(status().isOk());

        // 3. Entrar com perfil e unidade
        EntrarRequest entrarRequest = new EntrarRequest(titulo, perfil.name(), unidadeCodigo);
        MvcResult entrarResult = mockMvc.perform(post("/api/usuarios/entrar")
                        .cookie(preAuthCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrarRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Extrair token da resposta
        String responseBody = entrarResult.getResponse().getContentAsString();
        EntrarResponse response = objectMapper.readValue(responseBody, EntrarResponse.class);
        return response.token();
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
