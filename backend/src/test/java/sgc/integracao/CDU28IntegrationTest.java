package sgc.integracao;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.*;
import sgc.alerta.model.*;
import sgc.comum.*;
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
    private AlertaRepo alertaRepo;

    @Autowired
    private NotificacaoEmailRepo notificacaoEmailRepo;

    @Autowired
    private EntityManager entityManager;

    private Unidade unidade;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        unidade = unidadeRepo.findById(1L).orElseThrow();

        usuario = UsuarioFixture.usuarioPadrao();
        usuario.setTituloEleitoral("999988887777");
        usuario.setNome("Usuario temporario");
        usuario.setEmail("usuario.temporario@tre-pe.jus.br");
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

        Alerta alerta = alertaRepo.buscarAlertasExclusivosDoUsuario(usuario.getTituloEleitoral()).getFirst();
        assertThat(alerta.getDescricao()).isEqualTo("Atribuição temporária para unidade %s".formatted(unidade.getSigla()));
        assertThat(alerta.getProcesso()).isNull();
        assertThat(alerta.getUnidadeDestino()).isNull();
        assertThat(alerta.getUsuarioDestinoTitulo()).isEqualTo(usuario.getTituloEleitoral());

        NotificacaoEmail notificacao = notificacaoEmailRepo.findAll().stream()
                .filter(item -> usuario.getTituloEleitoral().equals(item.getUsuarioDestinoTitulo()))
                .findFirst()
                .orElseThrow();
        assertThat(notificacao.getSubprocesso()).isNull();
        assertThat(notificacao.getTipoNotificacao()).isEqualTo(TipoNotificacao.ATRIBUICAO_TEMPORARIA);
        assertThat(notificacao.getDestinatario()).isEqualTo(usuario.getEmail());
        assertThat(notificacao.getAssunto()).isEqualTo(
                "SGC: Atribuição de perfil CHEFE na unidade %s".formatted(unidade.getSigla()));
        assertThat(notificacao.getCorpoHtml())
                .contains("Usuario temporario")
                .contains(unidade.getSigla())
                .contains("Férias do titular")
                .contains("http://localhost:5173");
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

    @Test
    @DisplayName("Não deve permitir criar atribuição temporária para usuário sem e-mail")
    @WithMockAdmin
    void criarAtribuicaoTemporaria_usuarioSemEmail_erro() throws Exception {
        entityManager.createNativeQuery("UPDATE sgc.vw_usuario SET email = ' ' WHERE titulo = :titulo")
                .setParameter("titulo", usuario.getTituloEleitoral())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        CriarAtribuicaoRequest request = new CriarAtribuicaoRequest(
                usuario.getTituloEleitoral(),
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                "Sem e-mail"
        );

        mockMvc.perform(
                        post("/api/unidades/{codUnidade}/atribuicoes-temporarias", unidade.getCodigo())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        Mensagens.USUARIO_SEM_EMAIL.formatted(usuario.getNome()))));

        assertThat(alertaRepo.buscarAlertasExclusivosDoUsuario(usuario.getTituloEleitoral())).isEmpty();
        assertThat(notificacaoEmailRepo.findAll().stream()
                .filter(item -> usuario.getTituloEleitoral().equals(item.getUsuarioDestinoTitulo())))
                .isEmpty();
    }

    @Test
    @DisplayName("Deve atualizar uma atribuição temporária com sucesso")
    @WithMockAdmin
    void atualizarAtribuicaoTemporaria_sucesso() throws Exception {
        CriarAtribuicaoRequest criacao = new CriarAtribuicaoRequest(
                usuario.getTituloEleitoral(),
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                "Primeira"
        );

        mockMvc.perform(post("/api/unidades/{codUnidade}/atribuicoes-temporarias", unidade.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criacao)))
                .andExpect(status().isCreated());

        Long codigoAtribuicao = entityManager.createQuery(
                        "select a.codigo from AtribuicaoTemporaria a where a.unidade.codigo = :codUnidade",
                        Long.class
                )
                .setParameter("codUnidade", unidade.getCodigo())
                .getSingleResult();

        CriarAtribuicaoRequest atualizacao = new CriarAtribuicaoRequest(
                usuario.getTituloEleitoral(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(20),
                "Atualizada"
        );

        mockMvc.perform(post("/api/unidades/{codUnidade}/atribuicoes-temporarias/{codigo}/atualizar", unidade.getCodigo(), codigoAtribuicao)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizacao)))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();
        AtribuicaoTemporaria atribuicaoAtualizada = entityManager.find(AtribuicaoTemporaria.class, codigoAtribuicao);
        assertThat(atribuicaoAtualizada.getJustificativa()).isEqualTo("Atualizada");
        assertThat(atribuicaoAtualizada.getDataInicio()).isEqualTo(atualizacao.dataInicio().atStartOfDay());
    }

    @Test
    @DisplayName("Deve remover uma atribuição temporária com sucesso")
    @WithMockAdmin
    void removerAtribuicaoTemporaria_sucesso() throws Exception {
        CriarAtribuicaoRequest criacao = new CriarAtribuicaoRequest(
                usuario.getTituloEleitoral(),
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                "Primeira"
        );

        mockMvc.perform(post("/api/unidades/{codUnidade}/atribuicoes-temporarias", unidade.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criacao)))
                .andExpect(status().isCreated());

        Long codigoAtribuicao = entityManager.createQuery(
                        "select a.codigo from AtribuicaoTemporaria a where a.unidade.codigo = :codUnidade",
                        Long.class
                )
                .setParameter("codUnidade", unidade.getCodigo())
                .getSingleResult();

        mockMvc.perform(post("/api/unidades/{codUnidade}/atribuicoes-temporarias/{codigo}/excluir", unidade.getCodigo(), codigoAtribuicao)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();
        Long quantidade = entityManager.createQuery(
                        "select count(a) from AtribuicaoTemporaria a where a.codigo = :codigo",
                        Long.class
                )
                .setParameter("codigo", codigoAtribuicao)
                .getSingleResult();
        assertThat(quantidade).isZero();
    }

    @Test
    @DisplayName("Não deve permitir atribuição com período sobreposto")
    @WithMockAdmin
    void criarAtribuicaoTemporaria_periodoSobreposto_erro() throws Exception {
        CriarAtribuicaoRequest primeira = new CriarAtribuicaoRequest(
                usuario.getTituloEleitoral(),
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                "Primeira"
        );

        mockMvc.perform(post("/api/unidades/{codUnidade}/atribuicoes-temporarias", unidade.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(primeira)))
                .andExpect(status().isCreated());

        CriarAtribuicaoRequest segunda = new CriarAtribuicaoRequest(
                usuario.getTituloEleitoral(),
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(15),
                "Sobreposta"
        );

        mockMvc.perform(post("/api/unidades/{codUnidade}/atribuicoes-temporarias", unidade.getCodigo())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(segunda)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(Mensagens.ATRIBUICAO_TEMPORARIA_SOBREPOSTA)));
    }
}
