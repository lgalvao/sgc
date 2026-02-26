package sgc.integracao;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.*;
import sgc.integracao.mocks.*;
import sgc.organizacao.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Transactional
@DisplayName("CDU-30: Manter Administradores")
class CDU30IntegrationTest extends BaseIntegrationTest {
    @Autowired
    private AdministradorRepo administradorRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private EntityManager entityManager;

    private Usuario usuario1;
    private Usuario usuario2;

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
        Administrador admin1 = Administrador.builder().usuarioTitulo(usuario1.getTituloEleitoral()).build();
        administradorRepo.save(admin1);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Deve listar administradores existentes")
    @WithMockAdmin
    void listarAdministradores_sucesso() throws Exception {
        mockMvc.perform(get("/api/usuarios/administradores")
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

        mockMvc.perform(post("/api/usuarios/administradores")
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
        Administrador admin2 = Administrador.builder().usuarioTitulo(usuario2.getTituloEleitoral()).build();
        administradorRepo.save(admin2);
        entityManager.flush();
        entityManager.clear();

        // Remover o segundo administrador
        mockMvc.perform(post("/api/usuarios/administradores/{usuarioTitulo}/remover", usuario2.getTituloEleitoral())
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

        mockMvc.perform(post("/api/usuarios/administradores/{usuarioTitulo}/remover", usuario1.getTituloEleitoral())
                        .with(csrf()))
                .andExpect(status().is4xxClientError());
    }
}
