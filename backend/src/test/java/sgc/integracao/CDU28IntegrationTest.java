package sgc.integracao;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.*;
import sgc.fixture.*;
import sgc.integracao.mocks.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Transactional
@DisplayName("CDU-28: Manter atribuição temporária")
class CDU28IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private EntityManager entityManager;

    private Unidade unidade;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        unidade = unidadeRepo.findById(1L).orElseThrow();

        usuario = UsuarioFixture.usuarioPadrao();
        usuario.setTituloEleitoral("999988887777");
        usuario.setNome("Usuario Temporario");
        usuarioRepo.save(usuario);

        entityManager.flush();
        entityManager.clear();

        unidade = unidadeRepo.findById(1L).orElseThrow();
    }

    @Test
    @DisplayName("Deve criar uma atribuição temporária com sucesso")
    @WithMockAdmin
    void criarAtribuicaoTemporaria_sucesso() throws Exception {
        // Given
        CriarAtribuicaoRequest request = new CriarAtribuicaoRequest(
                usuario.getTituloEleitoral(),
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                "Férias do titular"
        );

        // When
        mockMvc.perform(
                        post("/api/unidades/{codUnidade}/atribuicoes-temporarias", unidade.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Then
        // We need to flush changes because we are in the same transaction and the service might rely on DB state or queries that need flushed data?
        // Actually, creating calls save, so it should be there.
        // However, AtribuicaoTemporaria.usuario is mapped with insertable=false, updatable=false
        // And populated via @JoinColumn(name = "usuario_titulo", ...).
        // If we just save with usuarioTitulo set, the 'usuario' relation might be null in the managed entity until refreshed or reloaded?
        // Let's force a clear/flush to ensure full reload.
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/unidades/atribuicoes")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String content = result.getResponse().getContentAsString();
                    assertThat(content).contains("Férias do titular");
                    // The DTO mapping uses 'usuario' relationship. If it was null initially, it might be null here.
                    // But we cleared the context, so it should fetch eagerly or lazily from DB.
                    assertThat(content).contains(usuario.getNome());
                });
    }

    @Test
    @DisplayName("Não deve permitir criar atribuição com datas inválidas")
    @WithMockAdmin
    void criarAtribuicaoTemporaria_datasInvalidas_erro() throws Exception {
        // Given: Data término antes do início
        CriarAtribuicaoRequest request = new CriarAtribuicaoRequest(
                usuario.getTituloEleitoral(),
                LocalDate.now().plusDays(10),
                LocalDate.now(),
                "Datas inválidas"
        );

        // When/Then
        mockMvc.perform(
                        post("/api/unidades/{codUnidade}/atribuicoes-temporarias", unidade.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(422));
    }
}
