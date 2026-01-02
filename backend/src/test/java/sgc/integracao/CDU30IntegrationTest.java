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
import sgc.organizacao.model.Administrador;
import sgc.organizacao.model.AdministradorRepo;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioRepo;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;
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
        // Usar unidade existente
        Unidade unidade = unidadeRepo.findById(1L).orElseThrow();

        // Criar usuário 1 (que será administrador inicial)
        usuario1 = Usuario.builder()
                .tituloEleitoral("123456789012")
                .nome("Admin Inicial")
                .email("admin@test.com")
                .ramal("1234")
                .unidadeLotacao(unidade)
                .build();
        usuarioRepo.save(usuario1);

        // Criar usuário 2 (que será adicionado como administrador)
        usuario2 = Usuario.builder()
                .tituloEleitoral("234567890123")
                .nome("Novo Admin")
                .email("novo@test.com")
                .ramal("5678")
                .unidadeLotacao(unidade)
                .build();
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
                .andExpect(jsonPath("$[?(@.tituloEleitoral == '" + usuario1.getTituloEleitoral() + "')]").exists());
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
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Não deve permitir remover o único administrador")
    @WithMockAdmin
    void removerAdministrador_unico() throws Exception {
        // Remover todos os outros administradores primeiro, deixando apenas usuario1
        long totalAdmins = administradorRepo.count();
        if (totalAdmins > 1) {
            administradorRepo.findAll().stream()
                .filter(adm -> !adm.getUsuarioTitulo().equals(usuario1.getTituloEleitoral()))
                .forEach(adm -> administradorRepo.deleteById(adm.getUsuarioTitulo()));
            entityManager.flush();
            entityManager.clear();
        }
        
        mockMvc.perform(post("/api/administradores/{usuarioTitulo}/remover", usuario1.getTituloEleitoral())
                        .with(csrf()))
                .andExpect(status().is4xxClientError());
    }
}
