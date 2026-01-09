package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.fixture.UnidadeFixture;
import sgc.fixture.UsuarioFixture;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.organizacao.model.*;
import sgc.seguranca.login.dto.EntrarReq;
import sgc.util.TestUtil;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestSecurityConfig.class)
@DisplayName("Security Auth Bypass Test")
public class SecurityAuthBypassTest extends BaseIntegrationTest {

    @Autowired
    private TestUtil testUtil;
    @Autowired
    private UnidadeRepo unidadeRepo;
    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        try {
            jdbcTemplate.execute("ALTER TABLE SGC.VW_UNIDADE ALTER COLUMN CODIGO RESTART WITH 20000");
        } catch (Exception ignored) {
        }
    }

    @Test
    @DisplayName("Should REJECT getting a token without prior authentication")
    void testAuthBypass() throws Exception {
        // 1. Create a user
        Unidade unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("BYP-UNIT");
        unidade = unidadeRepo.saveAndFlush(unidade);

        Usuario usuario = UsuarioFixture.usuarioComPerfil(unidade, Perfil.ADMIN);
        usuario.setTituloEleitoral("123456789099");
        usuarioRepo.saveAndFlush(usuario);

        jdbcTemplate.update(
                "INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, perfil, unidade_codigo) VALUES (?, ?, ?)",
                usuario.getTituloEleitoral(), "ADMIN", unidade.getCodigo());

        // 2. Try to "login" (entrar) directly, skipping /autenticar
        EntrarReq entrarReq = EntrarReq.builder()
                .tituloEleitoral(usuario.getTituloEleitoral())
                .perfil("ADMIN")
                .unidadeCodigo(unidade.getCodigo())
                .build();

        // 3. Assert that it fails with 401 Unauthorized (or similar)
        mockMvc.perform(post("/api/usuarios/entrar")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(testUtil.toJson(entrarReq)))
                .andExpect(status().isUnauthorized());
    }
}
