package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.*;
import sgc.fixture.UnidadeFixture;
import sgc.fixture.UsuarioFixture;
import sgc.integracao.mocks.WithMockAdmin;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
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
@WithMockAdmin
@Transactional
@DisplayName("CDU-04: Iniciar processo de mapeamento")
class CDU04IntegrationTest extends BaseIntegrationTest {
    private static final String SQL_INSERIR_RESPONSABILIDADE = """
            INSERT INTO SGC.VW_RESPONSABILIDADE (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio)
            VALUES (?, ?, ?, ?, ?)
            """;

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private CompetenciaRepo competenciaRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @Autowired
    private NotificacaoEmailRepo notificacaoEmailRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Unidade unidadeLivre;

    @BeforeEach
    void setup() {

        // Cria unidade programaticamente
        unidadeLivre = UnidadeFixture.unidadePadrao();
        unidadeLivre.setCodigo(null);
        unidadeLivre.setSigla("U_LIVRE_MAPP");
        unidadeLivre.setNome("Unidade livre mapeamento");
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
        // seja encontrado pelo UsuarioAplicacaoService
        UsuarioPerfil perfilChefe = UsuarioPerfil.builder()
                .usuarioTitulo(titular.getTituloEleitoral())
                .unidadeCodigo(unidadeLivre.getCodigo())
                .perfil(Perfil.CHEFE)
                .build();
        usuarioPerfilRepo.save(perfilChefe);

        jdbcTemplate.update(SQL_INSERIR_RESPONSABILIDADE,
                unidadeLivre.getCodigo(),
                titular.getTituloEleitoral(),
                titular.getMatricula(),
                "TITULAR",
                LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve iniciar processo de mapeamento com sucesso e gerar subprocessos, alertas e notificações")
    void deveIniciarProcessoMapeamento() throws Exception {

        CriarProcessoRequest criarReq = new CriarProcessoRequest(
                "Processo mapeamento teste CDU-04",
                TipoProcesso.MAPEAMENTO,
                LocalDateTime.now().plusDays(10),
                List.of(unidadeLivre.getCodigo()));

        var result = mockMvc.perform(post("/api/processos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Long codProcesso = objectMapper.readTree(result.getResponse().getContentAsString()).get("codigo").asLong();

        IniciarProcessoRequest iniciarReq = new IniciarProcessoRequest(TipoProcesso.MAPEAMENTO,
                List.of(unidadeLivre.getCodigo()));

        mockMvc.perform(post("/api/processos/{codigo}/iniciar", codProcesso)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(iniciarReq)))
                .andExpect(status().isOk());

        Processo processo = processoRepo.findById(codProcesso).orElseThrow();
        assertThat(processo.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);

        List<Subprocesso> subprocessos = subprocessoRepo.listarPorProcessoComUnidade(codProcesso);
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

        long alertasCount = alertaRepo.count();
        assertThat(alertasCount).isGreaterThan(0);

        List<NotificacaoEmail> notificacoes = notificacaoEmailRepo.findAll().stream()
                .filter(n -> n.getTipoNotificacao() == TipoNotificacao.PROCESSO_INICIADO)
                .toList();
        assertThat(notificacoes).hasSize(1);

        NotificacaoEmail notificacao = notificacoes.getFirst();
        assertThat(notificacao.getUnidadeDestinoSigla()).isEqualTo("U_LIVRE_MAPP");
        assertThat(notificacao.getDestinatario()).isEqualTo("u_livre_mapp@tre-pe.jus.br");
        assertThat(notificacao.getAssunto()).isEqualTo("SGC: Início de processo de mapeamento de competências");
        assertThat(notificacao.getCorpoHtml())
                .contains("Comunicamos o início do processo <strong>Processo mapeamento teste CDU-04</strong> para a sua unidade.")
                .contains("cadastro de atividades e conhecimentos")
                .contains("O prazo para conclusão desta etapa do processo é");
        assertThat(notificacao.getSituacao()).isEqualTo(SituacaoNotificacao.PENDENTE);

        aguardarEmail(1);
        assertThat(algumEmailPara("u_livre_mapp@tre-pe.jus.br")).isTrue();
        assertThat(algumEmailComAssunto("[SGC-TEST] Início de processo de mapeamento de competências")).isTrue();
        assertThat(algumEmailContem("Comunicamos o início do processo")).isTrue();
        assertThat(algumEmailContem("Processo mapeamento teste CDU-04")).isTrue();
        assertThat(algumEmailContem("cadastro de atividades e conhecimentos")).isTrue();
        assertThat(algumEmailContem("O prazo para conclusão desta etapa do processo é")).isTrue();
    }
}
