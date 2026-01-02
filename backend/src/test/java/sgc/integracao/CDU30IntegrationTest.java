package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.usuario.model.Administrador;
import sgc.usuario.model.AdministradorRepo;
import sgc.usuario.model.Usuario;
import sgc.usuario.model.UsuarioRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, sgc.integracao.mocks.TestThymeleafConfig.class})
@Transactional
@DisplayName("CDU-30: Manter Administradores")
class CDU30IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdministradorRepo administradorRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private EntityManager entityManager;

    private Usuario usuario1;
    private Usuario usuario2;
    private Administrador admin1;

    @BeforeEach
    void setUp() {
        // Criar unidade para os usuários
        Unidade unidade = new Unidade();
        unidade.setCodigo(1000L);
        unidade.setSigla("TEST");
        unidade.setNome("Unidade Teste");
        unidadeRepo.save(unidade);

        // Criar usuário 1 (que será administrador inicial)
        usuario1 = new Usuario("123456789012", "Admin Inicial", "admin@test.com", "1234", unidade);
        usuarioRepo.save(usuario1);

        // Criar usuário 2 (que será adicionado como administrador)
        usuario2 = new Usuario("234567890123", "Novo Admin", "novo@test.com", "5678", unidade);
        usuarioRepo.save(usuario2);

        // Adicionar admin inicial
        admin1 = new Administrador(usuario1.getTituloEleitoral());
        administradorRepo.save(admin1);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Deve listar administradores existentes")
    @WithMockAdmin
    void listarAdministradores_sucesso() throws Exception {
        mockMvc.perform(get("/api/administradores")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].tituloEleitoral").value(usuario1.getTituloEleitoral()))
                .andExpect(jsonPath("$[0].nome").value(usuario1.getNome()));
    }

    @Test
    @DisplayName("Deve adicionar um novo administrador")
    @WithMockAdmin
    void adicionarAdministrador_sucesso() throws Exception {
        Map<String, String> request = Map.of("usuarioTitulo", usuario2.getTituloEleitoral());

        mockMvc.perform(post("/api/administradores")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tituloEleitoral").value(usuario2.getTituloEleitoral()))
                .andExpect(jsonPath("$.nome").value(usuario2.getNome()));

        entityManager.flush();
        entityManager.clear();

        // Verificar que foi adicionado ao banco
        assertThat(administradorRepo.existsById(usuario2.getTituloEleitoral())).isTrue();
    }

    @Test
    @DisplayName("Deve remover um administrador")
    @WithMockAdmin
    void removerAdministrador_sucesso() throws Exception {
        // Adicionar um segundo administrador primeiro
        Administrador admin2 = new Administrador(usuario2.getTituloEleitoral());
        administradorRepo.save(admin2);
        entityManager.flush();
        entityManager.clear();

        // Remover o segundo administrador
        mockMvc.perform(post("/api/administradores/{usuarioTitulo}/remover", usuario2.getTituloEleitoral())
                        .with(csrf()))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        // Verificar que foi removido do banco
        assertThat(administradorRepo.existsById(usuario2.getTituloEleitoral())).isFalse();
        // Mas o primeiro ainda existe
        assertThat(administradorRepo.existsById(usuario1.getTituloEleitoral())).isTrue();
    }

    @Test
    @DisplayName("Não deve permitir adicionar administrador já existente")
    @WithMockAdmin
    void adicionarAdministrador_jaExiste() throws Exception {
        Map<String, String> request = Map.of("usuarioTitulo", usuario1.getTituloEleitoral());

        mockMvc.perform(post("/api/administradores")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Não deve permitir remover o único administrador")
    @WithMockAdmin
    void removerAdministrador_unico() throws Exception {
        mockMvc.perform(post("/api/administradores/{usuarioTitulo}/remover", usuario1.getTituloEleitoral())
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
