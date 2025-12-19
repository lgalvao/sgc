package sgc.integracao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.atividade.AtividadeService;
import sgc.atividade.dto.AtividadeDto;
import sgc.atividade.dto.ConhecimentoDto;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.comum.erros.ErroValidacao;
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.mapa.dto.AtividadeImpactadaDto;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.service.ImpactoMapaService;
import sgc.processo.dto.CriarProcessoReq;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoService;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.dto.SubmeterMapaAjustadoReq;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.SubprocessoCadastroWorkflowService;
import sgc.subprocesso.service.SubprocessoMapaWorkflowService;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Fluxo de Estados: Processo e Subprocesso")
@Import(TestSecurityConfig.class)
@Slf4j
class FluxoEstadosIntegrationTest extends BaseIntegrationTest {
    @Autowired private ProcessoService processoService;
    @Autowired private SubprocessoCadastroWorkflowService cadastroWorkflowService;
    @Autowired private SubprocessoMapaWorkflowService mapaWorkflowService;
    @Autowired private AtividadeService atividadeService;
    @Autowired private SubprocessoRepo subprocessoRepo;
    @Autowired private UnidadeRepo unidadeRepo;
    @Autowired private UsuarioRepo usuarioRepo;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private CompetenciaRepo competenciaRepo;
    @Autowired private AtividadeRepo atividadeRepo;
    @Autowired private MapaRepo mapaRepo;
    @PersistenceContext private EntityManager em;

    @MockitoBean
    private ImpactoMapaService impactoMapaService;

    // Users and Units for Mapeamento (SENIC -> COSINF -> STIC)
    private Long codUnidadeMapeamento = 11L; // SENIC
    private Usuario chefeMapeamento;
    private Usuario gestorMapeamento;
    private Usuario admin;

    // Users and Units for Revisão (SEDIA -> COSIS -> STIC)
    private Long codUnidadeRevisao = 9L; // SEDIA
    private Usuario chefeRevisao;
    private Usuario gestorRevisao;

    @BeforeEach
    void setUp() {
        // Ensure SEDOC exists (critical for homologation)
        if (unidadeRepo.findBySigla("SEDOC").isEmpty()) {
            jdbcTemplate.update(
                "INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular) VALUES (?, ?, ?, ?, ?, ?, ?)",
                15L, "Seção de Documentação", "SEDOC", "OPERACIONAL", "ATIVA", 2L, null
            );
        }

        // --- Setup Mapeamento Users ---
        // Chefe SENIC (11): '12' (Taís Condida) - Already in data.sql
        // Ensure profile CHEFE for 11 exists.
        criarUsuarioETitulo("90001", "Chefe Mapeamento", 11L, "CHEFE", 11L);
        chefeMapeamento = usuarioRepo.findById("90001").orElseThrow();

        // Gestor COSINF (7): Need to create one.
        criarUsuarioETitulo("90002", "Gestor Mapeamento", 7L, "GESTOR", 7L);
        gestorMapeamento = usuarioRepo.findById("90002").orElseThrow();

        // Admin: '6' (Ricardo Alves) - STIC (2) - Already in data.sql
        admin = usuarioRepo.findById("6").orElseThrow();

        // --- Setup Revisao Users ---
        // Chefe SEDIA (9): '333333333333' - Already in data.sql
        // Ensure titular of SEDIA is correct
        jdbcTemplate.update("UPDATE SGC.VW_UNIDADE SET titulo_titular = ? WHERE codigo = ?", "333333333333", 9L);
        chefeRevisao = usuarioRepo.findById("333333333333").orElseThrow();

        // Gestor COSIS (6): '666666666666' - Already in data.sql
        gestorRevisao = usuarioRepo.findById("666666666666").orElseThrow();
    }

    private void criarUsuarioETitulo(String titulo, String nome, Long unidadeLotacao, String perfil, Long unidadePerfil) {
        // Clean up if exists
        jdbcTemplate.update("DELETE FROM SGC.VW_USUARIO_PERFIL_UNIDADE WHERE usuario_titulo = ?", titulo);
        jdbcTemplate.update("DELETE FROM SGC.VW_USUARIO WHERE TITULO = ?", titulo);

        // Insert User
        jdbcTemplate.update(
            "INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo) VALUES (?, ?, ?, ?, ?)",
            titulo, nome, "teste@teste.com", "0000", unidadeLotacao
        );

        // Insert Profile
        jdbcTemplate.update(
            "INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, perfil, unidade_codigo) VALUES (?, ?, ?)",
            titulo, perfil, unidadePerfil
        );

        // Update Unit Titular if CHEFE
        if ("CHEFE".equals(perfil)) {
             jdbcTemplate.update("UPDATE SGC.VW_UNIDADE SET titulo_titular = ? WHERE codigo = ?", titulo, unidadePerfil);
        }
    }

    private void autenticar(Usuario usuario, String role) {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                String.valueOf(usuario.getTituloEleitoral()),
                "senha",
                List.of(new SimpleGrantedAuthority(role))
            )
        );
    }

    @Nested
    @DisplayName("Fluxo de Mapeamento")
    class FluxoMapeamento {

        @Test
        @DisplayName("Fluxo Mapeamento Happy Path: Inicio -> Cadastro -> Mapa -> Finalização")
        void fluxoMapeamentoCompleto() {
            try {
                // 1. Criar Processo (Admin)
                autenticar(admin, "ROLE_ADMIN");
                CriarProcessoReq req = CriarProcessoReq.builder()
                        .descricao("Processo Mapeamento Teste")
                        .tipo(TipoProcesso.MAPEAMENTO)
                        .dataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                        .unidades(List.of(codUnidadeMapeamento))
                        .build();
                var processoDto = processoService.criar(req);
                Long codProcesso = processoDto.getCodigo();

                // 2. Iniciar Processo (Admin)
                autenticar(admin, "ROLE_ADMIN");
                processoService.iniciarProcessoMapeamento(codProcesso, List.of(codUnidadeMapeamento));

                SubprocessoDto subprocessoDto = processoService.listarTodosSubprocessos(codProcesso).get(0);
                Long codSubprocesso = subprocessoDto.getCodigo();

                verificarSituacao(codSubprocesso, NAO_INICIADO);

                // 3. Adicionar Atividade/Conhecimento (Chefe)
                // Chefe adiciona atividade
                autenticar(chefeMapeamento, "ROLE_CHEFE");
                AtividadeDto ativReq = AtividadeDto.builder()
                        .descricao("Atividade Teste")
                        .mapaCodigo(subprocessoDto.getCodMapa())
                        .build();
                AtividadeDto ativCriada = atividadeService.criar(ativReq, chefeMapeamento.getTituloEleitoral());

                // Chefe adiciona conhecimento (necessário para validação)
                autenticar(chefeMapeamento, "ROLE_CHEFE");
                ConhecimentoDto conReq = ConhecimentoDto.builder()
                        .descricao("Conhecimento Teste")
                        .build();
                atividadeService.criarConhecimento(ativCriada.getCodigo(), conReq);

                em.flush();

                verificarSituacao(codSubprocesso, MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

                // 4. Disponibilizar Cadastro (Chefe)
                autenticar(chefeMapeamento, "ROLE_CHEFE");
                cadastroWorkflowService.disponibilizarCadastro(codSubprocesso, chefeMapeamento);
                verificarSituacao(codSubprocesso, MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

                // 5. Aceitar Cadastro (Gestor)
                autenticar(gestorMapeamento, "ROLE_GESTOR");
                cadastroWorkflowService.aceitarCadastro(codSubprocesso, "Aceito pelo Gestor", gestorMapeamento);
                verificarSituacao(codSubprocesso, MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

                // 6. Homologar Cadastro (Admin)
                autenticar(admin, "ROLE_ADMIN");
                cadastroWorkflowService.homologarCadastro(codSubprocesso, "Homologado pelo Admin", admin);
                verificarSituacao(codSubprocesso, MAPEAMENTO_CADASTRO_HOMOLOGADO);

                // 7. Disponibilizar Mapa (Admin - SEDOC cria mapa)
                autenticar(admin, "ROLE_ADMIN");

                // === CREATE COMPETENCY AND LINK (SEDOC WORK) ===
                Long codMapa = subprocessoRepo.findById(codSubprocesso).get().getMapa().getCodigo();
                Mapa mapaEntity = mapaRepo.findById(codMapa).orElseThrow();

                Competencia comp = new Competencia("Competencia Mapeamento", mapaEntity);
                comp = competenciaRepo.save(comp);

                // Link using JPA properly
                Atividade ativEntity = atividadeRepo.findById(ativCriada.getCodigo()).orElseThrow();
                ativEntity.setCompetencias(new HashSet<>(Collections.singletonList(comp)));
                atividadeRepo.save(ativEntity);

                em.flush(); // Ensure DB is updated
                em.clear(); // Ensure clean state for validation query

                mapaWorkflowService.disponibilizarMapa(codSubprocesso,
                        DisponibilizarMapaRequest.builder()
                                .observacoes("Mapa Inicial")
                                .dataLimite(LocalDate.now().plusDays(5))
                                .build(),
                        admin);
                verificarSituacao(codSubprocesso, MAPEAMENTO_MAPA_DISPONIBILIZADO);

                // 8. Validar Mapa (Chefe)
                autenticar(chefeMapeamento, "ROLE_CHEFE");
                mapaWorkflowService.validarMapa(codSubprocesso, chefeMapeamento);
                verificarSituacao(codSubprocesso, MAPEAMENTO_MAPA_VALIDADO);

                // 9. Aceitar Validação (Gestor)
                autenticar(gestorMapeamento, "ROLE_GESTOR");
                mapaWorkflowService.aceitarValidacao(codSubprocesso, gestorMapeamento);
                verificarSituacao(codSubprocesso, MAPEAMENTO_MAPA_VALIDADO);

                // 10. Homologar Validação (Admin)
                autenticar(admin, "ROLE_ADMIN");
                mapaWorkflowService.homologarValidacao(codSubprocesso, admin);
                verificarSituacao(codSubprocesso, MAPEAMENTO_MAPA_HOMOLOGADO);

                // 11. Finalizar Processo
                autenticar(admin, "ROLE_ADMIN");
                processoService.finalizar(codProcesso);
                assertThat(processoService.obterPorId(codProcesso).get().getSituacao())
                    .isEqualTo(sgc.processo.model.SituacaoProcesso.FINALIZADO);
            } catch (ErroValidacao e) {
                log.error("VALIDATION ERROR Mapeamento: {}", e.getMessage());
                if (e.getDetails() != null) e.getDetails().forEach((k, v) -> log.error(" - {}: {}", k, v));
                throw e;
            } catch (Exception e) {
                log.error("TEST ERROR Mapeamento: {}", e.getMessage(), e);
                throw e;
            }
        }

        @Test
        @DisplayName("Fluxo Mapeamento com Devolução de Cadastro")
        void fluxoMapeamentoDevolucaoCadastro() {
            // Setup inicial
            autenticar(admin, "ROLE_ADMIN");
            CriarProcessoReq req = CriarProcessoReq.builder()
                    .descricao("Processo Mapeamento Devolucao")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                    .unidades(List.of(codUnidadeMapeamento))
                    .build();
            Long codProcesso = processoService.criar(req).getCodigo();
            processoService.iniciarProcessoMapeamento(codProcesso, List.of(codUnidadeMapeamento));
            Long codSubprocesso = processoService.listarTodosSubprocessos(codProcesso).get(0).getCodigo();
            Long codMapa = processoService.listarTodosSubprocessos(codProcesso).get(0).getCodMapa();

            // Adicionar dados
            autenticar(chefeMapeamento, "ROLE_CHEFE");
            AtividadeDto ativ = atividadeService.criar(
                AtividadeDto.builder().descricao("A").mapaCodigo(codMapa).build(),
                chefeMapeamento.getTituloEleitoral());
            atividadeService.criarConhecimento(ativ.getCodigo(), ConhecimentoDto.builder().descricao("C").build());

            em.flush();

            // Disponibilizar
            autenticar(chefeMapeamento, "ROLE_CHEFE");
            cadastroWorkflowService.disponibilizarCadastro(codSubprocesso, chefeMapeamento);
            verificarSituacao(codSubprocesso, MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

            // Gestor DEVOLVE
            autenticar(gestorMapeamento, "ROLE_GESTOR");
            cadastroWorkflowService.devolverCadastro(codSubprocesso, "Ajuste necessário", gestorMapeamento);
            verificarSituacao(codSubprocesso, MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

            // Chefe ajusta (não precisa fazer nada real, só tentar disponibilizar de novo)
            autenticar(chefeMapeamento, "ROLE_CHEFE");
            cadastroWorkflowService.disponibilizarCadastro(codSubprocesso, chefeMapeamento);
            verificarSituacao(codSubprocesso, MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        }
    }

    @Nested
    @DisplayName("Fluxo de Revisão")
    class FluxoRevisao {

        @Test
        @DisplayName("Fluxo Revisão Happy Path com Impactos e Ajuste de Mapa")
        void fluxoRevisaoComImpactos() {
            try {
                // Mock Impactos
                when(impactoMapaService.verificarImpactos(any(), any()))
                    .thenReturn(ImpactoMapaDto.comImpactos(
                        List.of(AtividadeImpactadaDto.builder().descricao("Ativ 1").build()),
                        List.of(), List.of(), List.of()));

                // 1. Criar Processo Revisão (Admin)
                autenticar(admin, "ROLE_ADMIN");
                CriarProcessoReq req = CriarProcessoReq.builder()
                        .descricao("Processo Revisão Teste")
                        .tipo(TipoProcesso.REVISAO)
                        .dataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                        .unidades(List.of(codUnidadeRevisao))
                        .build();
                Long codProcesso = processoService.criar(req).getCodigo();

                // 2. Iniciar Processo
                autenticar(admin, "ROLE_ADMIN");
                processoService.iniciarProcessoRevisao(codProcesso, List.of(codUnidadeRevisao));

                SubprocessoDto subprocessoDto = processoService.listarTodosSubprocessos(codProcesso).get(0);
                Long codSubprocesso = subprocessoDto.getCodigo();
                Long codMapa = subprocessoDto.getCodMapa();

                verificarSituacao(codSubprocesso, NAO_INICIADO);

                // 3. Chefe inicia revisão (adiciona atividade -> Em Andamento)
                autenticar(chefeRevisao, "ROLE_CHEFE");
                AtividadeDto ativ = atividadeService.criar(
                    AtividadeDto.builder().descricao("Nova Ativ Revisao").mapaCodigo(codMapa).build(),
                    chefeRevisao.getTituloEleitoral()
                );
                atividadeService.criarConhecimento(ativ.getCodigo(), ConhecimentoDto.builder().descricao("C").build());

                // Link activity to ALL competencies to satisfy validation
                List<Competencia> competencias = competenciaRepo.findByMapaCodigo(codMapa);
                if (!competencias.isEmpty()) {
                    Atividade atividadeEntity = atividadeRepo.findById(ativ.getCodigo()).orElseThrow();
                    atividadeEntity.setCompetencias(new HashSet<>(competencias));
                    atividadeRepo.save(atividadeEntity);
                }

                em.flush();
                em.clear(); // Ensure clean reload for validation

                verificarSituacao(codSubprocesso, REVISAO_CADASTRO_EM_ANDAMENTO);

                // 4. Disponibilizar Revisão
                autenticar(chefeRevisao, "ROLE_CHEFE");
                cadastroWorkflowService.disponibilizarRevisao(codSubprocesso, chefeRevisao);
                verificarSituacao(codSubprocesso, REVISAO_CADASTRO_DISPONIBILIZADA);

                // 5. Aceitar Revisão (Gestor)
                autenticar(gestorRevisao, "ROLE_GESTOR");
                cadastroWorkflowService.aceitarRevisaoCadastro(codSubprocesso, "Ok", gestorRevisao);
                verificarSituacao(codSubprocesso, REVISAO_CADASTRO_DISPONIBILIZADA);

                // 6. Homologar Revisão (Admin) - Com Impactos (Simulado)
                autenticar(admin, "ROLE_ADMIN");
                cadastroWorkflowService.homologarRevisaoCadastro(codSubprocesso, "Homologado", admin);
                verificarSituacao(codSubprocesso, REVISAO_CADASTRO_HOMOLOGADA);

                // 7. Submeter Mapa Ajustado (Admin)
                autenticar(admin, "ROLE_ADMIN");
                SubmeterMapaAjustadoReq ajusteReq = SubmeterMapaAjustadoReq.builder()
                        .dataLimiteEtapa2(LocalDateTime.now().plusDays(5))
                        .build();
                mapaWorkflowService.submeterMapaAjustado(codSubprocesso, ajusteReq, admin);
                verificarSituacao(codSubprocesso, REVISAO_MAPA_DISPONIBILIZADO);

                // 8. Validar Mapa (Chefe)
                autenticar(chefeRevisao, "ROLE_CHEFE");
                mapaWorkflowService.validarMapa(codSubprocesso, chefeRevisao);
                verificarSituacao(codSubprocesso, REVISAO_MAPA_VALIDADO);

                // 9. Homologar Validação (Admin) (Pula gestor só pra variar, assumindo hierarquia permite ou admin força?
                // Na verdade, homologarValidacao checa estado. Se estado é REVISAO_MAPA_VALIDADO, admin pode homologar)
                autenticar(admin, "ROLE_ADMIN");
                mapaWorkflowService.homologarValidacao(codSubprocesso, admin);
                verificarSituacao(codSubprocesso, REVISAO_MAPA_HOMOLOGADO);
            } catch (Exception e) {
                log.error("TEST ERROR Revisao: {}", e.getMessage(), e);
                throw e;
            }
        }

        @Test
        @DisplayName("Fluxo Revisão Sem Impactos")
        void fluxoRevisaoSemImpactos() {
             // Mock Sem Impactos
            when(impactoMapaService.verificarImpactos(any(), any()))
                .thenReturn(ImpactoMapaDto.semImpacto());

            // 1. Criar Processo
            autenticar(admin, "ROLE_ADMIN");
            Long codProcesso = processoService.criar(CriarProcessoReq.builder()
                    .descricao("Rev Sem Impacto")
                    .tipo(TipoProcesso.REVISAO)
                    .dataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                    .unidades(List.of(codUnidadeRevisao))
                    .build()).getCodigo();

            // 2. Iniciar
            autenticar(admin, "ROLE_ADMIN");
            processoService.iniciarProcessoRevisao(codProcesso, List.of(codUnidadeRevisao));
            Long codSubprocesso = processoService.listarTodosSubprocessos(codProcesso).get(0).getCodigo();
            Long codMapa = processoService.listarTodosSubprocessos(codProcesso).get(0).getCodMapa();

            // 3. Chefe faz alteração
            autenticar(chefeRevisao, "ROLE_CHEFE");
            AtividadeDto ativ = atividadeService.criar(
                AtividadeDto.builder().descricao("Ativ").mapaCodigo(codMapa).build(),
                chefeRevisao.getTituloEleitoral()
            );
            atividadeService.criarConhecimento(ativ.getCodigo(), ConhecimentoDto.builder().descricao("C").build());

            em.flush();

            // 4. Disponibilizar
            autenticar(chefeRevisao, "ROLE_CHEFE");
            cadastroWorkflowService.disponibilizarRevisao(codSubprocesso, chefeRevisao);

            // 5. Homologar (Admin) - Sem Impactos
            autenticar(admin, "ROLE_ADMIN");
            cadastroWorkflowService.homologarRevisaoCadastro(codSubprocesso, "Ok", admin);

            // Deve ir direto para MAPA_HOMOLOGADO (ou REVISAO_MAPA_HOMOLOGADO)
            verificarSituacao(codSubprocesso, REVISAO_MAPA_HOMOLOGADO);
        }
    }

    private void verificarSituacao(Long codSubprocesso, SituacaoSubprocesso esperada) {
        Subprocesso sp = subprocessoRepo.findById(codSubprocesso).orElseThrow();
        assertThat(sp.getSituacao()).as("Situação incorreta para subprocesso " + codSubprocesso)
            .isEqualTo(esperada);
    }
}