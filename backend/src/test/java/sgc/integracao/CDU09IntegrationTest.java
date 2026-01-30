package sgc.integracao;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.internet.MimeMessage;
import sgc.alerta.model.AlertaRepo;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.fixture.UsuarioFixture;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.TestThymeleafConfig;
import sgc.integracao.mocks.WithMockChefeSecurityContextFactory;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.ConhecimentoRepo;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.organizacao.model.UsuarioRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import jakarta.persistence.EntityManager;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.subprocesso.model.MovimentacaoRepo;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Tag("integration")
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
    private CompetenciaRepo competenciaRepo;
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;
    @Autowired
    private ConhecimentoRepo conhecimentoRepo;
    @Autowired
    private AlertaRepo alertaRepo;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private JavaMailSender javaMailSender;

    private Unidade unidadeChefe;
    private Unidade unidadeSuperior;
    private Subprocesso sp;

    @BeforeEach
    void setUp() {
        try {
            jdbcTemplate.execute("ALTER SEQUENCE SGC.VW_UNIDADE_SEQ RESTART WITH 1000");
        } catch (DataAccessException e) {
            try {
                jdbcTemplate.execute("ALTER TABLE SGC.VW_UNIDADE ALTER COLUMN codigo RESTART WITH 1000");
            } catch (DataAccessException ex) {
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
        Usuario usuarioChefe = UsuarioFixture.usuarioComTitulo("998877665544");
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

        sp = SubprocessoFixture.subprocessoPadrao(processo, unidadeChefe);
        sp.setCodigo(null);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp = subprocessoRepo.save(sp);

        Mapa mapa = new Mapa();
        mapa.setSubprocesso(sp);
        mapa = mapaRepo.save(mapa);

        sp.setMapa(mapa);
        // No need to save subprocesso again as it is the inverse side, but updating the object is good for consistency.

        // 5. Autenticar
        autenticarUsuario(usuarioChefe, Perfil.CHEFE);
    }

    private void setupUsuarioPerfil(Usuario usuario, Unidade unidade, Perfil perfil) {
        // Insert na ATRIBUICAO_TEMPORARIA (refletirá nas views)
        jdbcTemplate.update("INSERT INTO SGC.ATRIBUICAO_TEMPORARIA (usuario_titulo, unidade_codigo, data_inicio, data_termino) VALUES (?, ?, ?, ?)",
                usuario.getTituloEleitoral(), unidade.getCodigo(), LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        // Atualizar objeto em memória para autenticação do Spring Security
        UsuarioPerfil up = new UsuarioPerfil();
        up.setUsuario(usuario);
        up.setUsuarioTitulo(usuario.getTituloEleitoral());
        up.setUnidade(unidade);
        up.setUnidadeCodigo(unidade.getCodigo());
        up.setPerfil(perfil);

        Set<UsuarioPerfil> atribuicoes = usuario.getTodasAtribuicoes(new HashSet<>());
        atribuicoes = new HashSet<>(atribuicoes); // Create mutable copy
        atribuicoes.add(up);
    }

    private void definirTitular(Unidade unidade, Usuario usuario) {
        jdbcTemplate.update("UPDATE SGC.VW_UNIDADE SET titulo_titular = ? WHERE codigo = ?",
                usuario.getTituloEleitoral(), unidade.getCodigo());
        unidade.setTituloTitular(usuario.getTituloEleitoral());
        unidade.setMatriculaTitular(usuario.getMatricula());
    }

    private void autenticarUsuario(Usuario usuario, Perfil perfil) {
        usuario.setAuthorities(Set.of(new SimpleGrantedAuthority("ROLE_" + perfil.name())));
        Authentication auth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Nested
    @DisplayName("Testes para Disponibilizar Cadastro")
    class DisponibilizarCadastro {
        @Test
        @DisplayName("Deve disponibilizar o cadastro quando todas as condições são atendidas")
        void deveDisponibilizarCadastroComSucesso() throws Exception {
            var competencia = competenciaRepo.save(Competencia.builder().descricao("Competência de Teste").mapa(sp.getMapa()).build());
            var atividade = Atividade.builder().mapa(sp.getMapa()).descricao("Atividade de Teste").build();

            // Establish Many-to-Many - assuming unidirectional or manually syncing:
            // Since it's a test, simply saving Atividade with Competencia might be enough if Cascade works, 
            // but safer to save separately and link.
            atividade = atividadeRepo.save(atividade); // Get ID
            
            // Link Competencia - Atividade
            atividade.getCompetencias().add(competencia);
            atividade = atividadeRepo.save(atividade);
            
            // Link Conhecimento
            conhecimentoRepo.save(Conhecimento.builder()
                    .descricao("Conhecimento de Teste")
                    .atividade(atividade) // Use saved instance with ID
                    .build());

            entityManager.flush();
            entityManager.clear();

            mockMvc.perform(post("/api/subprocessos/{id}/cadastro/disponibilizar", sp.getCodigo()).with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.mensagem", is("Cadastro de atividades disponibilizado")));

            Subprocesso subprocessoAtualizado =
                    subprocessoRepo.findById(sp.getCodigo()).orElseThrow();
            assertThat(subprocessoAtualizado.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            assertThat(subprocessoAtualizado.getDataFimEtapa1()).isNotNull();

            List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoAtualizado.getCodigo());

            assertThat(movimentacoes).hasSize(1);
            Movimentacao movimentacao = movimentacoes.getFirst();
            assertThat(movimentacao.getDescricao()).isEqualTo("Disponibilização do cadastro de atividades");
            assertThat(movimentacao.getUnidadeOrigem().getCodigo()).isEqualTo(unidadeChefe.getCodigo());
            assertThat(movimentacao.getUnidadeDestino().getCodigo()).isEqualTo(unidadeSuperior.getCodigo());

            var alertas = alertaRepo.findByProcessoCodigo(sp.getProcesso().getCodigo());
            assertThat(alertas).hasSize(1);
            var alerta = alertas.getFirst();
            assertThat(alerta.getDescricao()).isEqualTo("Cadastro de atividades/conhecimentos da unidade " + unidadeChefe.getSigla() + " disponibilizado" + " para análise");
            assertThat(alerta.getUnidadeDestino().getCodigo()).isEqualTo(unidadeSuperior.getCodigo());
        }

        @Test
        @DisplayName("Não deve disponibilizar se houver atividade sem conhecimento associado")
        void naoDeveDisponibilizarComAtividadeSemConhecimento() throws Exception {
            Atividade atividade = Atividade.builder().mapa(sp.getMapa()).descricao("Atividade Vazia").build();
            atividadeRepo.save(atividade);

            mockMvc.perform(post("/api/subprocessos/{id}/cadastro/disponibilizar", sp.getCodigo()).with(csrf()))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.message", is("Existem atividades sem conhecimentos associados.")))
                    .andExpect(jsonPath("$.details.atividadesSemConhecimento[0].descricao", is("Atividade Vazia")));

            Subprocesso subprocessoNaoAlterado = subprocessoRepo.findById(sp.getCodigo()).orElseThrow();
            assertThat(subprocessoNaoAlterado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
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
            autenticarUsuario(outroChefe, Perfil.CHEFE);

            mockMvc.perform(post("/api/subprocessos/{id}/cadastro/disponibilizar", sp.getCodigo()).with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }
}
