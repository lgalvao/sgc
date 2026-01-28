package sgc.integracao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
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
import sgc.integracao.mocks.TestSecurityConfig;
import sgc.mapa.dto.*;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.ConhecimentoService;
import sgc.mapa.service.ImpactoMapaService;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Usuario;
import sgc.processo.dto.CriarProcessoRequest;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoFacade;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.dto.SubmeterMapaAjustadoRequest;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.workflow.SubprocessoWorkflowService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Tag("integration")
@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Fluxo de Estados: Processo e Subprocesso")
@Import(TestSecurityConfig.class)
class FluxoEstadosIntegrationTest extends BaseIntegrationTest {
    // Users and Units for Mapeamento (SENIC -> COSINF -> STIC)
    private final Long codUnidadeMapeamento = 11L; // SENIC
    // Users and Units for Revisão (SEDIA -> COSIS -> STIC)
    private final Long codUnidadeRevisao = 9L; // SEDIA
    @Autowired
    private ProcessoFacade processoFacade;
    @Autowired
    private SubprocessoWorkflowService workflowService;
    @Autowired
    private AtividadeService atividadeService;
    @Autowired
    private ConhecimentoService conhecimentoService;
    @Autowired
    private UsuarioFacade usuarioService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CompetenciaRepo competenciaRepo;
    @PersistenceContext
    private EntityManager em;
    @MockitoBean
    private ImpactoMapaService impactoMapaService;
    private Usuario chefeMapeamento;
    private Usuario gestorMapeamento;
    private Usuario admin;
    private Usuario chefeRevisao;
    private Usuario gestorRevisao;

    @BeforeEach
    void setUp() {
        // Ensure SEDOC exists (critical for homologation)
        if (unidadeRepo.findBySigla("SEDOC").isEmpty()) {
            jdbcTemplate.update(
                    "INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    15L, "Seção de Documentação", "SEDOC", "OPERACIONAL", "ATIVA", 2L, null);
        }

        // --- Setup Mapeamento Users ---
        // Chefe SENIC (11): '12' (Taís Condida) - Already in data.sql
        // Ensure profile CHEFE for 11 exists.
        criarUsuarioETitulo("90001", "Chefe Mapeamento", 11L, "CHEFE", 11L);
        chefeMapeamento = usuarioService.buscarPorLogin("90001");

        // Gestor COSINF (7): Need to create one.
        criarUsuarioETitulo("90002", "Gestor Mapeamento", 7L, "GESTOR", 7L);
        gestorMapeamento = usuarioService.buscarPorLogin("90002");

        // Admin: '6' (Ricardo Alves) - STIC (2) - Already in data.sql
        admin = usuarioService.buscarPorLogin("6");

        // --- Setup Revisao Users ---
        // Chefe SEDIA (9): '333333333333' - Already in data.sql
        // Ensure titular of SEDIA is correct
        jdbcTemplate.update("UPDATE SGC.VW_UNIDADE SET titulo_titular = ? WHERE codigo = ?", "333333333333",
                9L);
        chefeRevisao = usuarioService.buscarPorLogin("333333333333");

        // Gestor COSIS (6): '666666666666' - Already in data.sql
        gestorRevisao = usuarioService.buscarPorLogin("666666666666");
    }

    private void criarUsuarioETitulo(String titulo, String nome, Long unidadeLotacao, String perfil,
                                     Long unidadePerfil) {
        // Clean up if exists
        jdbcTemplate.update("DELETE FROM SGC.VW_USUARIO_PERFIL_UNIDADE WHERE usuario_titulo = ?", titulo);
        jdbcTemplate.update("DELETE FROM SGC.VW_USUARIO WHERE TITULO = ?", titulo);

        // Insert User
        jdbcTemplate.update(
                "INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo) VALUES (?, ?, ?, ?, ?)",
                titulo, nome, "teste@teste.com", "0000", unidadeLotacao);

        // Insert Profile
        jdbcTemplate.update(
                "INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, perfil, unidade_codigo) VALUES (?, ?, ?)",
                titulo, perfil, unidadePerfil);

        // Update Unit Titular if CHEFE
        if ("CHEFE".equals(perfil)) {
            jdbcTemplate.update("UPDATE SGC.VW_UNIDADE SET titulo_titular = ? WHERE codigo = ?", titulo,
                    unidadePerfil);
        }
    }

    private void autenticar(Usuario usuario, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        usuario.getTituloEleitoral(),
                        "senha",
                        List.of(new SimpleGrantedAuthority(role))));
    }

    @Test
    @DisplayName("Fluxo Mapeamento Happy Path: Inicio -> Cadastro -> Mapa -> Finalização")
    void fluxoMapeamentoCompleto() {
        // 1. Criar Processo (Admin)
        autenticar(admin, "ROLE_ADMIN");
        CriarProcessoRequest req = CriarProcessoRequest.builder()
                .descricao("Processo Mapeamento Teste")
                .tipo(TipoProcesso.MAPEAMENTO)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                .unidades(List.of(codUnidadeMapeamento))
                .build();
        var processoDto = processoFacade.criar(req);
        Long codProcesso = processoDto.getCodigo();

        // 2. Iniciar Processo (Admin)
        autenticar(admin, "ROLE_ADMIN");
        processoFacade.iniciarProcessoMapeamento(codProcesso, List.of(codUnidadeMapeamento));

        SubprocessoDto subprocessoDto = processoFacade.listarTodosSubprocessos(codProcesso).getFirst();
        Long codSubprocesso = subprocessoDto.getCodigo();

        verificarSituacao(codSubprocesso, NAO_INICIADO);

        // 3. Adicionar Atividade/Conhecimento (Chefe)
        // Chefe adiciona atividade
        autenticar(chefeMapeamento, "ROLE_CHEFE");
        CriarAtividadeRequest ativReq = CriarAtividadeRequest.builder()
                .descricao("Atividade Teste")
                .mapaCodigo(subprocessoDto.getCodMapa())
                .build();
        AtividadeResponse ativCriada = atividadeService.criar(ativReq);

        // Chefe adiciona conhecimento (necessário para validação)
        autenticar(chefeMapeamento, "ROLE_CHEFE");
        CriarConhecimentoRequest conReq = CriarConhecimentoRequest.builder()
                .descricao("Conhecimento Teste")
                .atividadeCodigo(ativCriada.codigo())
                .build();
        conhecimentoService.criar(ativCriada.codigo(), conReq);

        em.flush();
        em.clear();

        verificarSituacao(codSubprocesso, MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        // 4. Disponibilizar Cadastro (Chefe)
        autenticar(chefeMapeamento, "ROLE_CHEFE");
        workflowService.disponibilizarCadastro(codSubprocesso, chefeMapeamento);
        verificarSituacao(codSubprocesso, MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

        // 5. Aceitar Cadastro (Gestor)
        autenticar(gestorMapeamento, "ROLE_GESTOR");
        workflowService.aceitarCadastro(codSubprocesso, "Aceito pelo Gestor", gestorMapeamento);
        verificarSituacao(codSubprocesso, MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

        // 6. Homologar Cadastro (Admin)
        autenticar(admin, "ROLE_ADMIN");
        workflowService.homologarCadastro(codSubprocesso, "Homologado pelo Admin", admin);
        verificarSituacao(codSubprocesso, MAPEAMENTO_CADASTRO_HOMOLOGADO);

        // 7. Disponibilizar Mapa (Admin - SEDOC cria mapa)
        autenticar(admin, "ROLE_ADMIN");

        // === CREATE COMPETENCY AND LINK (SEDOC WORK) ===
        Long codMapa = subprocessoRepo.findById(codSubprocesso).orElseThrow().getMapa().getCodigo();
        Mapa mapaEntity = mapaRepo.findById(codMapa).orElseThrow();

        Competencia comp = Competencia.builder().descricao("Competencia Mapeamento").mapa(mapaEntity).build();
        comp = competenciaRepo.save(comp);

        // Link using JPA properly
        Atividade ativEntity = atividadeRepo.findById(ativCriada.codigo()).orElseThrow();
        ativEntity.setCompetencias(new HashSet<>(Collections.singletonList(comp)));
        atividadeRepo.save(ativEntity);

        em.flush(); // Ensure DB is updated
        em.clear(); // Ensure clean state for validation query

        workflowService.disponibilizarMapa(codSubprocesso,
                DisponibilizarMapaRequest.builder()
                        .observacoes("Mapa Inicial")
                        .dataLimite(LocalDate.now().plusDays(5))
                        .build(),
                admin);
        verificarSituacao(codSubprocesso, MAPEAMENTO_MAPA_DISPONIBILIZADO);

        // 8. Validar Mapa (Chefe)
        autenticar(chefeMapeamento, "ROLE_CHEFE");
        workflowService.validarMapa(codSubprocesso, chefeMapeamento);
        verificarSituacao(codSubprocesso, MAPEAMENTO_MAPA_VALIDADO);

        // 9. Aceitar Validação (Gestor)
        autenticar(gestorMapeamento, "ROLE_GESTOR");
        workflowService.aceitarValidacao(codSubprocesso, gestorMapeamento);
        verificarSituacao(codSubprocesso, MAPEAMENTO_MAPA_VALIDADO);

        // 10. Homologar Validação (Admin)
        autenticar(admin, "ROLE_ADMIN");
        workflowService.homologarValidacao(codSubprocesso, admin);
        verificarSituacao(codSubprocesso, MAPEAMENTO_MAPA_HOMOLOGADO);

        // 11. Finalizar Processo
        autenticar(admin, "ROLE_ADMIN");
        processoFacade.finalizar(codProcesso);
        assertThat(processoFacade.obterPorId(codProcesso).orElseThrow().getSituacao())
                .isEqualTo(sgc.processo.model.SituacaoProcesso.FINALIZADO);
    }

    @Test
    @DisplayName("Fluxo Mapeamento com Devolução de Cadastro")
    void fluxoMapeamentoDevolucaoCadastro() {
        // Setup inicial
        autenticar(admin, "ROLE_ADMIN");
        CriarProcessoRequest req = CriarProcessoRequest.builder()
                .descricao("Processo Mapeamento Devolucao")
                .tipo(TipoProcesso.MAPEAMENTO)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                .unidades(List.of(codUnidadeMapeamento))
                .build();
        Long codProcesso = processoFacade.criar(req).getCodigo();
        processoFacade.iniciarProcessoMapeamento(codProcesso, List.of(codUnidadeMapeamento));
        Long codSubprocesso = processoFacade.listarTodosSubprocessos(codProcesso).getFirst().getCodigo();
        Long codMapa = processoFacade.listarTodosSubprocessos(codProcesso).getFirst().getCodMapa();

        // Adicionar dados
        autenticar(chefeMapeamento, "ROLE_CHEFE");
        AtividadeResponse ativ = atividadeService.criar(
                CriarAtividadeRequest.builder().descricao("A").mapaCodigo(codMapa).build());
        conhecimentoService.criar(ativ.codigo(),
                CriarConhecimentoRequest.builder().descricao("C").atividadeCodigo(ativ.codigo())
                        .build());

        em.flush();
        em.clear();

        // Disponibilizar
        autenticar(chefeMapeamento, "ROLE_CHEFE");
        workflowService.disponibilizarCadastro(codSubprocesso, chefeMapeamento);
        verificarSituacao(codSubprocesso, MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

        // Gestor DEVOLVE
        autenticar(gestorMapeamento, "ROLE_GESTOR");
        workflowService.devolverCadastro(codSubprocesso, "Ajuste necessário", gestorMapeamento);
        verificarSituacao(codSubprocesso, MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        // Chefe ajusta (não precisa fazer nada real, só tentar disponibilizar de novo)
        autenticar(chefeMapeamento, "ROLE_CHEFE");
        workflowService.disponibilizarCadastro(codSubprocesso, chefeMapeamento);
        verificarSituacao(codSubprocesso, MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
    }

    @Test
    @DisplayName("Fluxo Revisão Happy Path com Impactos e Ajuste de Mapa")
    void fluxoRevisaoComImpactos() {
        // Mock Impactos
        when(impactoMapaService.verificarImpactos(any(), any()))
                .thenReturn(ImpactoMapaDto.comImpactos(
                        List.of(AtividadeImpactadaDto.builder().descricao("Ativ 1").build()),
                        List.of(), List.of(), List.of()));

        // 1. Criar Processo Revisão (Admin)
        autenticar(admin, "ROLE_ADMIN");
        CriarProcessoRequest req = CriarProcessoRequest.builder()
                .descricao("Processo Revisão Teste")
                .tipo(TipoProcesso.REVISAO)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                .unidades(List.of(codUnidadeRevisao))
                .build();
        Long codProcesso = processoFacade.criar(req).getCodigo();

        // 2. Iniciar Processo
        autenticar(admin, "ROLE_ADMIN");
        processoFacade.iniciarProcessoRevisao(codProcesso, List.of(codUnidadeRevisao));

        SubprocessoDto subprocessoDto = processoFacade.listarTodosSubprocessos(codProcesso).getFirst();
        Long codSubprocesso = subprocessoDto.getCodigo();
        Long codMapa = subprocessoDto.getCodMapa();

        verificarSituacao(codSubprocesso, NAO_INICIADO);

        // 3. Chefe inicia revisão (adiciona atividade -> Em Andamento)
        autenticar(chefeRevisao, "ROLE_CHEFE");
        AtividadeResponse ativ = atividadeService.criar(
                CriarAtividadeRequest.builder().descricao("Nova Ativ Revisao").mapaCodigo(codMapa)
                        .build());
        conhecimentoService.criar(ativ.codigo(),
                CriarConhecimentoRequest.builder().descricao("C").atividadeCodigo(ativ.codigo())
                        .build());

        // Link activity to ALL competencies to satisfy validation
        List<Competencia> competencias = competenciaRepo.findByMapaCodigo(codMapa);
        if (!competencias.isEmpty()) {
            Atividade atividadeEntity = atividadeRepo.findById(ativ.codigo()).orElseThrow();
            atividadeEntity.setCompetencias(new HashSet<>(competencias));
            atividadeRepo.save(atividadeEntity);
        }

        em.flush();
        em.clear(); // Ensure clean reload for validation

        verificarSituacao(codSubprocesso, REVISAO_CADASTRO_EM_ANDAMENTO);

        // 4. Disponibilizar Revisão
        autenticar(chefeRevisao, "ROLE_CHEFE");
        workflowService.disponibilizarRevisao(codSubprocesso, chefeRevisao);
        verificarSituacao(codSubprocesso, REVISAO_CADASTRO_DISPONIBILIZADA);

        // 5. Aceitar Revisão (Gestor)
        autenticar(gestorRevisao, "ROLE_GESTOR");
        workflowService.aceitarRevisaoCadastro(codSubprocesso, "Ok", gestorRevisao);
        verificarSituacao(codSubprocesso, REVISAO_CADASTRO_DISPONIBILIZADA);

        // 6. Homologar Revisão (Admin) - Com Impactos (Simulado)
        autenticar(admin, "ROLE_ADMIN");
        workflowService.homologarRevisaoCadastro(codSubprocesso, "Homologado", admin);
        verificarSituacao(codSubprocesso, REVISAO_CADASTRO_HOMOLOGADA);

        // 7. Submeter Mapa Ajustado (Admin)
        autenticar(admin, "ROLE_ADMIN");
        SubmeterMapaAjustadoRequest ajusteReq = SubmeterMapaAjustadoRequest.builder()
                .dataLimiteEtapa2(LocalDateTime.now().plusDays(5))
                .build();
        workflowService.submeterMapaAjustado(codSubprocesso, ajusteReq, admin);
        verificarSituacao(codSubprocesso, REVISAO_MAPA_DISPONIBILIZADO);

        // 8. Validar Mapa (Chefe)
        autenticar(chefeRevisao, "ROLE_CHEFE");
        workflowService.validarMapa(codSubprocesso, chefeRevisao);
        verificarSituacao(codSubprocesso, REVISAO_MAPA_VALIDADO);

        // 9. Homologar Validação (Admin) (Pula gestor só pra variar, assumindo
        // hierarquia permite ou admin força?
        // Na verdade, homologarValidacao checa estado. Se estado é
        // REVISAO_MAPA_VALIDADO, admin pode homologar)
        autenticar(admin, "ROLE_ADMIN");
        workflowService.homologarValidacao(codSubprocesso, admin);
        verificarSituacao(codSubprocesso, REVISAO_MAPA_HOMOLOGADO);
    }

    @Test
    @DisplayName("Fluxo Revisão Sem Impactos")
    void fluxoRevisaoSemImpactos() {
        // Mock Sem Impactos
        when(impactoMapaService.verificarImpactos(any(), any()))
                .thenReturn(ImpactoMapaDto.semImpacto());

        // 1. Criar Processo
        autenticar(admin, "ROLE_ADMIN");
        Long codProcesso = processoFacade.criar(CriarProcessoRequest.builder()
                .descricao("Rev Sem Impacto")
                .tipo(TipoProcesso.REVISAO)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                .unidades(List.of(codUnidadeRevisao))
                .build()).getCodigo();

        // 2. Iniciar
        autenticar(admin, "ROLE_ADMIN");
        processoFacade.iniciarProcessoRevisao(codProcesso, List.of(codUnidadeRevisao));
        Long codSubprocesso = processoFacade.listarTodosSubprocessos(codProcesso).getFirst().getCodigo();
        Long codMapa = processoFacade.listarTodosSubprocessos(codProcesso).getFirst().getCodMapa();

        // 3. Chefe faz alteração
        autenticar(chefeRevisao, "ROLE_CHEFE");
        AtividadeResponse ativ = atividadeService.criar(
                CriarAtividadeRequest.builder().descricao("Ativ").mapaCodigo(codMapa).build());
        conhecimentoService.criar(ativ.codigo(),
                CriarConhecimentoRequest.builder().descricao("C").atividadeCodigo(ativ.codigo())
                        .build());

        em.flush();
        em.clear();

        // 4. Disponibilizar
        autenticar(chefeRevisao, "ROLE_CHEFE");
        workflowService.disponibilizarRevisao(codSubprocesso, chefeRevisao);

        // 5. Homologar (Admin) - Sem Impactos
        autenticar(admin, "ROLE_ADMIN");
        workflowService.homologarRevisaoCadastro(codSubprocesso, "Ok", admin);

        // Deve ir direto para MAPA_HOMOLOGADO (ou REVISAO_MAPA_HOMOLOGADO)
        verificarSituacao(codSubprocesso, REVISAO_MAPA_HOMOLOGADO);
    }

    private void verificarSituacao(Long codSubprocesso, SituacaoSubprocesso esperada) {
        Subprocesso sp = subprocessoRepo.findById(codSubprocesso).orElseThrow();
        assertThat(sp.getSituacao()).as("Situação incorreta para subprocesso " + codSubprocesso)
                .isEqualTo(esperada);
    }
}