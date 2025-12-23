package sgc.integracao;

import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.internal.model.AlertaRepo;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.Conhecimento;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.fixture.*;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.TestThymeleafConfig;
import sgc.integracao.mocks.WithMockChefeSecurityContextFactory;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.MapaRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.sgrh.internal.model.Perfil;
import sgc.sgrh.internal.model.Usuario;
import sgc.sgrh.internal.model.UsuarioPerfil;
import sgc.sgrh.internal.model.UsuarioRepo;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.SubprocessoNotificacaoService;
import sgc.unidade.internal.model.Unidade;
import sgc.unidade.internal.model.UnidadeRepo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Import({
        TestSecurityConfig.class,
        WithMockChefeSecurityContextFactory.class,
        TestThymeleafConfig.class
})
@Transactional
@DisplayName("CDU-09: Disponibilizar Cadastro de Atividades e Conhecimentos")
class CDU09IntegrationTest extends BaseIntegrationTest {
    @Autowired
    private ProcessoRepo processoRepo;
    @Autowired
    private UnidadeRepo unidadeRepo;
    @Autowired
    private SubprocessoRepo subprocessoRepo;
    @Autowired
    private MapaRepo mapaRepo;
    @Autowired
    private AtividadeRepo atividadeRepo;
    @Autowired
    private ConhecimentoRepo conhecimentoRepo;
    @Autowired
    private CompetenciaRepo competenciaRepo;
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;
    @Autowired
    private AlertaRepo alertaRepo;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private EntityManager entityManager;

    @MockitoSpyBean
    private SubprocessoNotificacaoService subprocessoNotificacaoService;

    @MockitoBean
    private JavaMailSender javaMailSender;

    private Unidade unidadeChefe;
    private Unidade unidadeSuperior;
    private Usuario usuarioChefe;
    private Subprocesso subprocessoMapeamento;

    @BeforeEach
    void setUp() {
        // Reset sequence to avoid collision with data.sql (IDs 1-26)
        // Note: data.sql uses explicit IDs. If H2 sequence is not synced, nextval might be 1.
        // We force it to 1000 to be safe.
        try {
            jdbcTemplate.execute("ALTER SEQUENCE SGC.VW_UNIDADE_SEQ RESTART WITH 1000");
        } catch (Exception e) {
             // Fallback if sequence name is standard or different
             try {
                 jdbcTemplate.execute("ALTER TABLE SGC.VW_UNIDADE ALTER COLUMN codigo RESTART WITH 1000");
             } catch (Exception ex) {
                 // Ignore if fails, might depend on H2 version/mode
             }
        }

        when(javaMailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));

        // 1. Criar Unidades (Superior e Chefe)
        unidadeSuperior = UnidadeFixture.unidadeComSigla("SUP_TEST");
        unidadeSuperior.setCodigo(null);
        // If save fails due to ID collision (even with null), we might need to flush or handle it.
        unidadeSuperior = unidadeRepo.save(unidadeSuperior);

        unidadeChefe = UnidadeFixture.unidadeComSigla("SESEL_TEST");
        unidadeChefe.setCodigo(null);
        unidadeChefe.setUnidadeSuperior(unidadeSuperior);
        unidadeChefe = unidadeRepo.save(unidadeChefe);

        // 2. Criar Usuários (Chefe e Superior para receber email)
        // Precisamos de flush para garantir que o Usuário exista antes do insert via JDBC na tabela de relacionamento
        usuarioChefe = UsuarioFixture.usuarioComTitulo("998877665544");
        usuarioChefe.setUnidadeLotacao(unidadeChefe);
        usuarioChefe = usuarioRepo.saveAndFlush(usuarioChefe);

        Usuario usuarioSuperior = UsuarioFixture.usuarioComTitulo("112233445566");
        usuarioSuperior.setUnidadeLotacao(unidadeSuperior);
        usuarioSuperior = usuarioRepo.saveAndFlush(usuarioSuperior);

        // 3. Configurar Perfil e Titularidade (Simulação de Views)
        setupUsuarioPerfil(usuarioChefe, unidadeChefe, Perfil.CHEFE);
        definirTitular(unidadeChefe, usuarioChefe);

        definirTitular(unidadeSuperior, usuarioSuperior);

        // 4. Criar Processo, Mapa e Subprocesso
        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo = processoRepo.save(processo);

        sgc.mapa.model.Mapa mapa = mapaRepo.save(new sgc.mapa.model.Mapa());

        subprocessoMapeamento = SubprocessoFixture.subprocessoPadrao(processo, unidadeChefe);
        subprocessoMapeamento.setCodigo(null);
        subprocessoMapeamento.setMapa(mapa);
        subprocessoMapeamento.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocessoMapeamento = subprocessoRepo.save(subprocessoMapeamento);

        // 5. Autenticar
        autenticarUsuario(usuarioChefe);
    }

    private void setupUsuarioPerfil(Usuario usuario, Unidade unidade, Perfil perfil) {
        // Insert na VIEW_USUARIO_PERFIL_UNIDADE para consultas nativas/views
        jdbcTemplate.update("INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, unidade_codigo, perfil) VALUES (?, ?, ?)",
                usuario.getTituloEleitoral(), unidade.getCodigo(), perfil.name());

        // Atualizar objeto em memória para autenticação do Spring Security
        UsuarioPerfil up = new UsuarioPerfil();
        up.setUsuario(usuario);
        up.setUsuarioTitulo(usuario.getTituloEleitoral());
        up.setUnidade(unidade);
        up.setUnidadeCodigo(unidade.getCodigo());
        up.setPerfil(perfil);

        Set<UsuarioPerfil> atribuicoes = usuario.getAtribuicoes();
        if (atribuicoes == null || atribuicoes.isEmpty()) { // Handle empty set from Fixture/Immutable
            atribuicoes = new HashSet<>();
        }
        atribuicoes.add(up);
        usuario.setAtribuicoes(atribuicoes);
    }

    private void definirTitular(Unidade unidade, Usuario usuario) {
        jdbcTemplate.update("UPDATE SGC.VW_UNIDADE SET titulo_titular = ? WHERE codigo = ?",
                usuario.getTituloEleitoral(), unidade.getCodigo());
        // Remove entityManager.refresh(unidade) as it causes UnsupportedLockAttemptException for read-only entities
        // If we need the entity updated, we can detach and fetch again, or just manually set the field.
        unidade.setTitular(usuario);
    }

    private void autenticarUsuario(Usuario usuario) {
        Authentication auth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Nested
    @DisplayName("Testes para Disponibilizar Cadastro")
    class DisponibilizarCadastro {
        @Test
        @DisplayName("Deve disponibilizar o cadastro quando todas as condições são atendidas")
        void deveDisponibilizarCadastroComSucesso() throws Exception {
            var competencia =
                    competenciaRepo.save(
                            new Competencia(
                                    "Competência de Teste", subprocessoMapeamento.getMapa()));
            var atividade = new Atividade(subprocessoMapeamento.getMapa(), "Atividade de Teste");
            atividade.getCompetencias().add(competencia);
            atividadeRepo.save(atividade);
            competencia.getAtividades().add(atividade);
            competenciaRepo.save(competencia);
            conhecimentoRepo.save(new Conhecimento("Conhecimento de Teste", atividade));

            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{id}/cadastro/disponibilizar",
                                    subprocessoMapeamento.getCodigo())
                                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", is("Cadastro de atividades disponibilizado")));

            Subprocesso subprocessoAtualizado =
                    subprocessoRepo.findById(subprocessoMapeamento.getCodigo()).orElseThrow();
            assertThat(subprocessoAtualizado.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            assertThat(subprocessoAtualizado.getDataFimEtapa1()).isNotNull();

            List<Movimentacao> movimentacoes =
                    movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(
                            subprocessoAtualizado.getCodigo());
            assertThat(movimentacoes).hasSize(1);
            Movimentacao movimentacao = movimentacoes.getFirst();
            assertThat(movimentacao.getDescricao())
                    .isEqualTo("Disponibilização do cadastro de atividades");
            assertThat(movimentacao.getUnidadeOrigem().getCodigo())
                    .isEqualTo(unidadeChefe.getCodigo());
            assertThat(movimentacao.getUnidadeDestino().getCodigo())
                    .isEqualTo(unidadeSuperior.getCodigo());

            var alertas =
                    alertaRepo.findByProcessoCodigo(
                            subprocessoMapeamento.getProcesso().getCodigo());
            assertThat(alertas).hasSize(1);
            var alerta = alertas.getFirst();
            assertThat(alerta.getDescricao())
                    .isEqualTo(
                            "Cadastro de atividades/conhecimentos da unidade " + unidadeChefe.getSigla() + " disponibilizado"
                                    + " para análise");
            assertThat(alerta.getUnidadeDestino()).isEqualTo(unidadeSuperior);

            verify(subprocessoNotificacaoService)
                    .notificarDisponibilizacaoCadastro(
                            org.mockito.ArgumentMatchers.any(Subprocesso.class),
                            org.mockito.ArgumentMatchers.any(Unidade.class));
        }

        @Test
        @DisplayName("Não deve disponibilizar se houver atividade sem conhecimento associado")
        void naoDeveDisponibilizarComAtividadeSemConhecimento() throws Exception {
            Atividade atividade = new Atividade(subprocessoMapeamento.getMapa(), "Atividade Vazia");
            atividadeRepo.save(atividade);

            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{id}/cadastro/disponibilizar",
                                    subprocessoMapeamento.getCodigo())
                                    .with(csrf()))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(
                            jsonPath(
                                    "$.message",
                                    is("Existem atividades sem conhecimentos associados.")))
                    .andExpect(
                            jsonPath(
                                    "$.details.atividadesSemConhecimento[0].descricao",
                                    is("Atividade Vazia")));

            Subprocesso subprocessoNaoAlterado =
                    subprocessoRepo.findById(subprocessoMapeamento.getCodigo()).orElseThrow();
            assertThat(subprocessoNaoAlterado.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        }
    }

    @Nested
    @DisplayName("Testes de Segurança")
    class Seguranca {
        @Test
        @DisplayName("Não deve permitir que um CHEFE de outra unidade disponibilize o cadastro")
        void naoDevePermitirChefeDeOutraUnidadeDisponibilizar() throws Exception {
            // Autenticar com outro usuário
            Usuario outroChefe = UsuarioFixture.usuarioComTitulo("777888999000");
            outroChefe = usuarioRepo.save(outroChefe);

            Unidade outraUnidade = UnidadeFixture.unidadeComSigla("OUTRA");
            outraUnidade.setCodigo(null);
            outraUnidade = unidadeRepo.save(outraUnidade);

            setupUsuarioPerfil(outroChefe, outraUnidade, Perfil.CHEFE);
            autenticarUsuario(outroChefe);

            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{id}/cadastro/disponibilizar",
                                    subprocessoMapeamento.getCodigo())
                                    .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }
}
