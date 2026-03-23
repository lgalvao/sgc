package sgc.processo.painel;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import sgc.integracao.BaseIntegrationTest;
import sgc.organizacao.model.*;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@DisplayName("PainelController - Reprodução de Vulnerabilidade de Segurança")
class PainelSecurityReproductionTest extends BaseIntegrationTest {

    @Autowired
    private UsuarioRepo usuarioRepo;

    private Usuario servidor;

    @BeforeEach
    void setUp() {
        // Usuário '1' é SERVIDOR na Unidade 10 (SESEL) no data.sql
        servidor = usuarioRepo.findById("1").orElseThrow();
    }

    private void autenticar(Usuario usuario, Perfil perfil, Long unidadeCodigo) {
        usuario.setPerfilAtivo(perfil);
        usuario.setUnidadeAtivaCodigo(unidadeCodigo);
        usuario.setAuthorities(Set.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + perfil.name())));
        Authentication auth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("CENÁRIO POSITIVO: SERVIDOR vê processos da sua unidade")
    void listarProcessos_Sucesso() throws Exception {
        autenticar(servidor, Perfil.SERVIDOR, 10L);

        mockMvc.perform(get("/api/painel/processos")
                        .param("perfil", "SERVIDOR")
                        .param("unidade", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // No data.sql, Processo 50002 está associado à unidade 10
                .andExpect(jsonPath("$.content[*].codigo", hasItem(50002)));
    }

    @Test
    @DisplayName("SEGURANÇA: Sistema ignora perfil malicioso na URL e usa o Perfil do Token")
    void listarProcessos_BypassPerfil_DeveEstarCorrigido() throws Exception {
        // Usuário está autenticado como SERVIDOR na Unidade 10
        autenticar(servidor, Perfil.SERVIDOR, 10L);

        // Atacante tenta passar ADMIN e Unidade 1 na URL
        mockMvc.perform(get("/api/painel/processos")
                        .param("perfil", "ADMIN")
                        .param("unidade", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Deve retornar apenas processos da unidade 10 (onde ele é servidor)
                .andExpect(jsonPath("$.content[*].codigo", hasItem(50002)))
                // NÃO deve conter o processo 50000 (que é da unidade 1 e só visível para ADMIN)
                .andExpect(jsonPath("$.content[*].codigo", not(hasItem(50000))));
    }

    @Test
    @DisplayName("SEGURANÇA: Sistema ignora unidade maliciosa na URL e usa a Unidade do Token")
    void listarAlertas_AcessoUnidadeAlheia_DeveEstarCorrigido() throws Exception {
        // Usuário está autenticado na Unidade 10
        autenticar(servidor, Perfil.SERVIDOR, 10L);

        // Atacante tenta listar alertas da Unidade 8 pela URL
        mockMvc.perform(get("/api/painel/alertas")
                        .param("unidade", "8")
                        .param("usuarioTitulo", "TITULO_ALHEIO")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // NÃO deve conter o alerta 70002 (que é da unidade 8)
                .andExpect(jsonPath("$.content[*].codigo", not(hasItem(70002))));
    }
}
