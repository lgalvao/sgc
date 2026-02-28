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

        CriarAtribuicaoRequest request = new CriarAtribuicaoRequest(
                usuario.getTituloEleitoral(),
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                "Férias do titular"
        );


        mockMvc.perform(
                        post("/api/unidades/{codUnidade}/atribuicoes-temporarias", unidade.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/unidades/atribuicoes")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String content = result.getResponse().getContentAsString();
                    assertThat(content).contains("Férias do titular");
                    assertThat(content).contains(usuario.getNome());
                });
    }

    @Test
    @DisplayName("Não deve permitir criar atribuição com datas inválidas")
    @WithMockAdmin
    void criarAtribuicaoTemporaria_datasInvalidas_erro() throws Exception {

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
