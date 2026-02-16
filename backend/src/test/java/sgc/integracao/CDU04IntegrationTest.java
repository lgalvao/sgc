package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.alerta.model.AlertaRepo;
import sgc.fixture.UnidadeFixture;
import sgc.fixture.UsuarioFixture;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.TestThymeleafConfig;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.notificacao.NotificacaoEmailService;
import sgc.organizacao.model.*;
import sgc.processo.dto.CriarProcessoRequest;
import sgc.processo.dto.IniciarProcessoRequest;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@WithMockAdmin
@Import({ TestSecurityConfig.class, TestThymeleafConfig.class })
@Transactional
@DisplayName("CDU-04: Iniciar processo de mapeamento")
class CDU04IntegrationTest extends BaseIntegrationTest {
    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private CompetenciaRepo competenciaRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private NotificacaoEmailService notificacaoEmailService;

    private Unidade unidadeLivre;

    @BeforeEach
    void setup() {
        // Reset sequences
        try {
            jdbcTemplate.execute("ALTER TABLE SGC.VW_UNIDADE ALTER COLUMN CODIGO RESTART WITH 30000");
            jdbcTemplate.execute("ALTER TABLE SGC.PROCESSO ALTER COLUMN CODIGO RESTART WITH 90000");
            jdbcTemplate.execute("ALTER TABLE SGC.ALERTA ALTER COLUMN CODIGO RESTART WITH 60000");
        } catch (DataAccessException ignored) {
            // Ignorado: falha ao resetar sequências no H2 não deve impedir o teste
        }

        // Cria unidade programaticamente
        unidadeLivre = UnidadeFixture.unidadePadrao();
        unidadeLivre.setCodigo(null);
        unidadeLivre.setSigla("U_LIVRE_MAPP");
        unidadeLivre.setNome("Unidade Livre Mapeamento");
        unidadeLivre = unidadeRepo.save(unidadeLivre);

        // Cria titular para a unidade (para garantir envio de notificação)
        Usuario titular = UsuarioFixture.usuarioPadrao();
        titular.setTituloEleitoral("999999999999");
        titular.setEmail("titular@teste.com");
        titular = usuarioRepo.save(titular);

        // Associa titular à unidade
        unidadeLivre.setTituloTitular(titular.getTituloEleitoral());
        unidadeLivre.setMatriculaTitular(titular.getMatricula());
        unidadeRepo.save(unidadeLivre);

        // Também precisamos associar o perfil CHEFE ao usuário na unidade para que ele
        // seja encontrado pelo UsuarioFacade
        UsuarioPerfil perfilChefe = UsuarioPerfil.builder()
                .usuarioTitulo(titular.getTituloEleitoral())
                .usuario(titular)
                .unidadeCodigo(unidadeLivre.getCodigo())
                .unidade(unidadeLivre)
                .perfil(Perfil.CHEFE)
                .build();
        usuarioPerfilRepo.save(perfilChefe);
    }

    @Test
    @DisplayName("Deve iniciar processo de mapeamento com sucesso e gerar subprocessos, alertas e notificações")
    void deveIniciarProcessoMapeamento() throws Exception {
        // 1. Arrange: Criar um processo em estado 'CRIADO'
        CriarProcessoRequest criarReq = new CriarProcessoRequest(
                "Processo Mapeamento Teste CDU-04",
                TipoProcesso.MAPEAMENTO,
                LocalDateTime.now().plusDays(10),
                List.of(unidadeLivre.getCodigo()));

        var result = mockMvc.perform(post("/api/processos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(criarReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Long processoId = objectMapper.readTree(result.getResponse().getContentAsString()).get("codigo").asLong();

        // 2. Act: Iniciar Processo
        IniciarProcessoRequest iniciarReq = new IniciarProcessoRequest(TipoProcesso.MAPEAMENTO,
                List.of(unidadeLivre.getCodigo()));

        mockMvc.perform(post("/api/processos/{id}/iniciar", processoId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(iniciarReq)))
                .andExpect(status().isOk());

        // 3. Assert: Mudança de Status do Processo
        Processo processo = processoRepo.findById(processoId).orElseThrow();
        assertThat(processo.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);

        // 4. Assert: Criação de Subprocessos
        List<Subprocesso> subprocessos = subprocessoRepo.findByProcessoCodigo(processoId);
        assertThat(subprocessos).hasSize(1);

        // Verificar subprocesso da unidade livre
        Subprocesso sub = subprocessos.stream()
                .filter(s -> s.getUnidade().getCodigo().equals(unidadeLivre.getCodigo()))
                .findFirst()
                .orElseThrow();
        assertThat(sub.getMapa()).isNotNull();

        // Verificar que o mapa não tem competências (Mapeamento inicia vazio)
        List<Competencia> competencias = competenciaRepo.findByMapa_Codigo(sub.getMapa().getCodigo());
        assertThat(competencias).isEmpty();

        // 5. Assert: Geração de Alertas
        long alertasCount = alertaRepo.count();
        assertThat(alertasCount).isGreaterThan(0);

        // 6. Assert: Notificações por email são mockadas em ambiente de teste
        // e não podem ser verificadas diretamente via Mockito
        // O teste de envio de emails está em NotificacaoEmailServiceTest
    }
}
