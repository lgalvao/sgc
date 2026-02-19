package sgc.integracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import sgc.integracao.mocks.TestLoginHelper;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@Import({TestLoginHelper.class})
@DisplayName("Teste de Login Real")
class LoginRealTest extends BaseIntegrationTest {

    @Autowired
    private TestLoginHelper loginHelper;

    @Test
    @DisplayName("Deve fazer login como CHEFE e obter token JWT válido")
    void deveFazerLoginComoChefe() throws Exception {
        // Usuário "3" é CHEFE na unidade 8 (conforme data.sql)
        String token = loginHelper.loginChefe(mockMvc, "3", 8L);
        
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token).contains(".");  // JWT tem formato xxx.yyy.zzz
    }

    @Test
    @DisplayName("Deve fazer login como GESTOR e obter token JWT válido")
    void deveFazerLoginComoGestor() throws Exception {
        // Usuário "8" é GESTOR na unidade 8 (conforme data.sql)
        String token = loginHelper.loginGestor(mockMvc, "8", 8L);
        
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token).contains(".");
    }

    @Test
    @DisplayName("Deve fazer login como ADMIN e obter token JWT válido")
    void deveFazerLoginComoAdmin() throws Exception {
        // Usuário "6" é ADMIN na unidade 2 (conforme data.sql)
        String token = loginHelper.loginAdmin(mockMvc, "6");
        
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token).contains(".");
    }

    @Test
    @DisplayName("Deve fazer login como SERVIDOR e obter token JWT válido")
    void deveFazerLoginComoServidor() throws Exception {
        // Usuário "1" é SERVIDOR na unidade 10 (conforme data.sql)
        String token = loginHelper.loginServidor(mockMvc, "1", 10L);
        
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token).contains(".");
    }
}
