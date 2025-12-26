package sgc.integracao;

import jakarta.mail.internet.MimeMessage;
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
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.AlertaRepo;
import sgc.analise.model.Analise;
import sgc.analise.model.AnaliseRepo;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.ConhecimentoRepo;
import sgc.fixture.*;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.integracao.mocks.WithMockChefeSecurityContextFactory;
import sgc.mapa.model.CompetenciaRepo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.usuario.model.Perfil;
import sgc.usuario.model.Usuario;
import sgc.usuario.model.UsuarioPerfil;
import sgc.usuario.model.UsuarioRepo;
import sgc.subprocesso.model.*;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Import({
        TestSecurityConfig.class,
        WithMockChefeSecurityContextFactory.class,
        sgc.integracao.mocks.TestThymeleafConfig.class
})
@Transactional
@DisplayName("CDU-10: Disponibilizar Revisão do Cadastro de Atividades e Conhecimentos")
class CDU10IntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProcessoRepo processoRepo;
    @Autowired
    private UnidadeRepo unidadeRepo;
    @Autowired
    private SubprocessoRepo subprocessoRepo;
    @Autowired
    private sgc.mapa.model.MapaRepo mapaRepo;
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
    private AnaliseRepo analiseRepo;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UsuarioRepo usuarioRepo;

    @MockitoBean
    private JavaMailSender javaMailSender;

    private Unidade unidadeChefe;
    private Unidade unidadeSuperior;
    private Subprocesso subprocessoRevisao;

    @BeforeEach
    void setUp() {
        when(javaMailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));

        // Reset sequence to avoid collision
        try {
            jdbcTemplate.execute("ALTER SEQUENCE SGC.VW_UNIDADE_SEQ RESTART WITH 1000");
        } catch (Exception e) {
            try {
                jdbcTemplate.execute("ALTER TABLE SGC.VW_UNIDADE ALTER COLUMN codigo RESTART WITH 1000");
            } catch (Exception ex) {
                // Ignore
            }
        }

        // 1. Criar Unidades (Superior e Chefe)
        unidadeSuperior = UnidadeFixture.unidadeComSigla("COSIS");
        unidadeSuperior.setCodigo(null);
        unidadeSuperior = unidadeRepo.save(unidadeSuperior);

        unidadeChefe = UnidadeFixture.unidadeComSigla("SESEL");
        unidadeChefe.setCodigo(null);
        unidadeChefe.setUnidadeSuperior(unidadeSuperior);
        unidadeChefe = unidadeRepo.save(unidadeChefe);

        // 2. Criar Usuários
        Usuario usuarioChefe = UsuarioFixture.usuarioComTitulo("333333333333");
        usuarioChefe.setUnidadeLotacao(unidadeChefe);
        usuarioChefe = usuarioRepo.saveAndFlush(usuarioChefe);

        Usuario usuarioSuperior = UsuarioFixture.usuarioComTitulo("666666666666");
        usuarioSuperior.setUnidadeLotacao(unidadeSuperior);
        usuarioSuperior = usuarioRepo.saveAndFlush(usuarioSuperior);

        // 3. Configurar Perfil e Titularidade
        setupUsuarioPerfil(usuarioChefe, unidadeChefe, Perfil.CHEFE);
        definirTitular(unidadeChefe, usuarioChefe);
        definirTitular(unidadeSuperior, usuarioSuperior);

        // 4. Criar Processo, Mapa e Subprocesso
        sgc.processo.model.Processo processoRevisao = ProcessoFixture.processoPadrao();
        processoRevisao.setCodigo(null);
        processoRevisao.setTipo(TipoProcesso.REVISAO);
        processoRevisao.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processoRevisao.setDataLimite(LocalDateTime.now().plusDays(30));
        processoRevisao = processoRepo.save(processoRevisao);

        var mapa = mapaRepo.save(new sgc.mapa.model.Mapa());

        subprocessoRevisao = SubprocessoFixture.subprocessoPadrao(processoRevisao, unidadeChefe);
        subprocessoRevisao.setCodigo(null);
        subprocessoRevisao.setMapa(mapa);
        subprocessoRevisao.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        subprocessoRevisao.setDataLimiteEtapa1(processoRevisao.getDataLimite());
        subprocessoRevisao = subprocessoRepo.save(subprocessoRevisao);

        // 5. Autenticar
        autenticarUsuario(usuarioChefe);
    }

    private void setupUsuarioPerfil(Usuario usuario, Unidade unidade, Perfil perfil) {
        jdbcTemplate.update("INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, unidade_codigo, perfil) VALUES (?, ?, ?)",
                usuario.getTituloEleitoral(), unidade.getCodigo(), perfil.name());

        UsuarioPerfil up = new UsuarioPerfil();
        up.setUsuario(usuario);
        up.setUsuarioTitulo(usuario.getTituloEleitoral());
        up.setUnidade(unidade);
        up.setUnidadeCodigo(unidade.getCodigo());
        up.setPerfil(perfil);

        Set<UsuarioPerfil> atribuicoes = usuario.getAtribuicoes();
        if (atribuicoes == null || atribuicoes.isEmpty()) {
            atribuicoes = new HashSet<>();
        }
        atribuicoes.add(up);
        usuario.setAtribuicoes(atribuicoes);
    }

    private void definirTitular(Unidade unidade, Usuario usuario) {
        jdbcTemplate.update("UPDATE SGC.VW_UNIDADE SET titulo_titular = ? WHERE codigo = ?",
                usuario.getTituloEleitoral(), unidade.getCodigo());
        unidade.setTituloTitular(usuario.getTituloEleitoral());
        unidade.setMatriculaTitular(usuario.getMatricula());
    }

    private void autenticarUsuario(Usuario usuario) {
        Authentication auth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Nested
    @DisplayName("Testes para Disponibilizar Revisão do Cadastro")
    class DisponibilizarRevisaoCadastro {
        @Test
        @DisplayName("Deve disponibilizar a revisão do cadastro quando todas as condições são atendidas")
        void deveDisponibilizarRevisaoComSucesso() throws Exception {
            var competencia = competenciaRepo.save(CompetenciaFixture.competenciaPadrao(subprocessoRevisao.getMapa()));
            var atividade = AtividadeFixture.atividadePadrao(subprocessoRevisao.getMapa());

            // Associar
            atividade.getCompetencias().add(competencia);
            atividade = atividadeRepo.save(atividade);
            competencia.getAtividades().add(atividade);
            competenciaRepo.save(competencia);

            conhecimentoRepo.save(new sgc.mapa.model.Conhecimento("Conhecimento de Teste", atividade));

            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{id}/disponibilizar-revisao",
                                    subprocessoRevisao.getCodigo()))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath(
                                    "$.message",
                                    is("Revisão do cadastro de atividades disponibilizada")));

            Subprocesso subprocessoAtualizado =
                    subprocessoRepo.findById(subprocessoRevisao.getCodigo()).orElseThrow();
            assertThat(subprocessoAtualizado.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
            assertThat(subprocessoAtualizado.getDataFimEtapa1()).isNotNull();

            List<Movimentacao> movimentacoes =
                    movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(
                            subprocessoAtualizado.getCodigo());
            assertThat(movimentacoes).hasSize(1);
            Movimentacao movimentacao = movimentacoes.getFirst();
            assertThat(movimentacao.getDescricao())
                    .isEqualTo("Disponibilização da revisão do cadastro de atividades");
            assertThat(movimentacao.getUnidadeOrigem().getCodigo())
                    .isEqualTo(unidadeChefe.getCodigo());
            assertThat(movimentacao.getUnidadeDestino().getCodigo())
                    .isEqualTo(unidadeSuperior.getCodigo());

            var alertas =
                    alertaRepo.findByProcessoCodigo(subprocessoRevisao.getProcesso().getCodigo());
            assertThat(alertas).hasSize(1);
            var alerta = alertas.getFirst();
            assertThat(alerta.getDescricao())
                    .isEqualTo(
                            "Revisão do cadastro da unidade SESEL"
                                    + " disponibilizada para análise");
            assertThat(alerta.getUnidadeDestino()).isEqualTo(unidadeSuperior);
        }

        @Test
        @DisplayName("Não deve disponibilizar se houver atividade sem conhecimento associado")
        void naoDeveDisponibilizarComAtividadeSemConhecimento() throws Exception {
            AtividadeFixture.atividadePadrao(subprocessoRevisao.getMapa());
            atividadeRepo.save(AtividadeFixture.atividadePadrao(subprocessoRevisao.getMapa()));

            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{id}/disponibilizar-revisao",
                                    subprocessoRevisao.getCodigo()))
                    .andExpect(status().isUnprocessableContent());

            Subprocesso subprocessoNaoAlterado =
                    subprocessoRepo.findById(subprocessoRevisao.getCodigo()).orElseThrow();
            assertThat(subprocessoNaoAlterado.getSituacao())
                    .isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        }
    }

    @Nested
    @DisplayName("Testes de Segurança")
    class Seguranca {
        @Test
        @DisplayName("Não deve permitir que um CHEFE de outra unidade disponibilize a revisão")
        void naoDevePermitirChefeDeOutraUnidadeDisponibilizar() throws Exception {
            // Autenticar com outro usuário
            Usuario outroChefe = UsuarioFixture.usuarioComTitulo("999999999999");
            outroChefe = usuarioRepo.save(outroChefe);

            Unidade outraUnidade = UnidadeFixture.unidadeComSigla("OUTRA");
            outraUnidade.setCodigo(null);
            outraUnidade = unidadeRepo.save(outraUnidade);

            setupUsuarioPerfil(outroChefe, outraUnidade, Perfil.CHEFE);
            autenticarUsuario(outroChefe);

            mockMvc.perform(
                            post(
                                    "/api/subprocessos/{id}/disponibilizar-revisao",
                                    subprocessoRevisao.getCodigo()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Cenário 4: Histórico de análise deve ser excluído após nova disponibilização")
    class HistoricoAnaliseExcluido {

        @Test
        @DisplayName("Deve excluir histórico de análises anteriores quando disponibilizar revisão novamente")
        void deveExcluirHistoricoAposNovaDisponibilizacao() throws Exception {
            // Preparar: criar atividade com conhecimento para permitir disponibilização
            var competencia = competenciaRepo.save(CompetenciaFixture.competenciaPadrao(subprocessoRevisao.getMapa()));
            var atividade = AtividadeFixture.atividadePadrao(subprocessoRevisao.getMapa());

            atividade.getCompetencias().add(competencia);
            atividade = atividadeRepo.save(atividade);
            competencia.getAtividades().add(atividade);
            competenciaRepo.save(competencia);

            conhecimentoRepo.save(new sgc.mapa.model.Conhecimento("Conhecimento de Teste", atividade));

            Long subprocessoId = subprocessoRevisao.getCodigo();

            // 1. Primeira disponibilização
            mockMvc.perform(post("/api/subprocessos/{id}/disponibilizar-revisao", subprocessoId))
                    .andExpect(status().isOk());

            // 2. Criar análise de "Aceite" manualmente
            Analise analiseAceite = new Analise();
            analiseAceite.setSubprocesso(subprocessoRevisao);
            analiseAceite.setUnidadeCodigo(unidadeSuperior.getCodigo());
            analiseAceite.setAcao(TipoAcaoAnalise.ACEITE_REVISAO);
            analiseAceite.setDataHora(LocalDateTime.now());
            analiseRepo.saveAndFlush(analiseAceite);

            // 3. Criar análise de "Devolução" manualmente
            Analise analiseDevolucao = new Analise();
            analiseDevolucao.setSubprocesso(subprocessoRevisao);
            analiseDevolucao.setUnidadeCodigo(unidadeSuperior.getCodigo());
            analiseDevolucao.setAcao(TipoAcaoAnalise.DEVOLUCAO_REVISAO);
            analiseDevolucao.setObservacoes("Segunda devolução");
            analiseDevolucao.setDataHora(LocalDateTime.now());
            analiseRepo.saveAndFlush(analiseDevolucao);

            // 4. Simular devolução
            subprocessoRevisao.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            subprocessoRevisao.setDataFimEtapa1(null);
            subprocessoRepo.saveAndFlush(subprocessoRevisao);

            // 5. Nova disponibilização
            mockMvc.perform(post("/api/subprocessos/{id}/disponibilizar-revisao", subprocessoId))
                    .andExpect(status().isOk());

            // 6. Verificar que histórico foi excluído
            List<Analise> analisesDepois =
                    analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(subprocessoId);
            assertThat(analisesDepois).isEmpty();
        }
    }
}
