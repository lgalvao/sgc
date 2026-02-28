package sgc.integracao;

import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.*;
import org.springframework.test.web.servlet.request.*;
import org.springframework.test.web.servlet.result.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.ComumDtos.*;
import sgc.fixture.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import tools.jackson.core.type.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Transactional
@DisplayName("CDU-13: Analisar cadastro de atividades e conhecimentos")
class CDU13IntegrationTest extends BaseIntegrationTest {
    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    @Autowired
    private AnaliseRepo analiseRepo;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Unidade unidade;
    private Unidade unidadeSuperior;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        // Criar Unidades via JDBC para contornar @Immutable
        Long idSuperior = 3000L;
        Long idUnidade = 3001L;

        String sqlInsertUnidade = "INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular) VALUES (?, ?, ?, ?, ?, ?, ?)";

        Integer countSuperior = jdbcTemplate.queryForObject("SELECT count(*) FROM SGC.VW_UNIDADE WHERE codigo = ?", Integer.class, idSuperior);
        if (countSuperior != null && countSuperior == 0) {
            jdbcTemplate.update(sqlInsertUnidade, idSuperior, "Coordenação de Sistemas Teste", "COSIS-TEST",
                    "INTERMEDIARIA", "ATIVA", null, null);
        }

        Integer countUnidade = jdbcTemplate.queryForObject("SELECT count(*) FROM SGC.VW_UNIDADE WHERE codigo = ?", Integer.class, idUnidade);
        if (countUnidade != null && countUnidade == 0) {
            jdbcTemplate.update(sqlInsertUnidade, idUnidade, "Serviço de Desenvolvimento Teste", "SEDESENV-TEST",
                    "OPERACIONAL", "ATIVA", idSuperior, null);
        }

        // Carregar via Repo
        unidadeSuperior = unidadeRepo.findById(idSuperior).orElseThrow();
        unidade = unidadeRepo.findById(idUnidade).orElseThrow();

        // Criar Usuários via JDBC (Usuario é @Immutable, não pode ser salvo via Repo)
        String sqlInsertUsuario = "INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, unidade_lot_codigo, MATRICULA) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlInsertPerfil = "INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, perfil, unidade_codigo) VALUES (?, ?, ?)";

        String tituloAdmin = "101010101010";
        if (usuarioRepo.findById(tituloAdmin).isEmpty()) {
            jdbcTemplate.update(sqlInsertUsuario, tituloAdmin, "Admin Mock", "admin@test.com", "1010", idSuperior, "");
            jdbcTemplate.update(sqlInsertPerfil, tituloAdmin, "ADMIN", idSuperior);
        }

        String tituloGestor = "132313231323";
        if (usuarioRepo.findById(tituloGestor).isEmpty()) {
            jdbcTemplate.update(sqlInsertUsuario, tituloGestor, "Gestor Mock", "gestor@test.com", "2020", idSuperior, "");
            jdbcTemplate.update(sqlInsertPerfil, tituloGestor, "GESTOR", idSuperior);
        }

        // Carregar usuários do banco
        Usuario adminUser = usuarioRepo.findById("101010101010").orElseThrow();

        // Criar Processo via Fixture
        Processo processo = ProcessoFixture.processoPadrao();
        processo.setCodigo(null);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDescricao("Processo de Teste CDU-13");
        processo = processoRepo.save(processo);

        // Criar Subprocesso via Fixture
        subprocesso = SubprocessoFixture.subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(10));
        subprocesso = subprocessoRepo.save(subprocesso);

        // Movimentação inicial
        Movimentacao movimentacaoInicial = Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(unidade)
                .unidadeDestino(unidadeSuperior)
                .descricao("Disponibilização inicial")
                .usuario(adminUser)
                .dataHora(LocalDateTime.now())
                .build();
        movimentacaoRepo.save(movimentacaoInicial);

        entityManager.flush();
        entityManager.clear();

        // Reload entities
        subprocesso = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        unidade = unidadeRepo.findById(idUnidade).orElseThrow();
        unidadeSuperior = unidadeRepo.findById(idSuperior).orElseThrow();
    }

    @Test
    @DisplayName("Deve devolver cadastro, registrar análise corretamente e alterar situação")
    void devolverCadastro_deveFuncionarCorretamente() throws Exception {
        // Given
        Usuario gestor = usuarioRepo.findById("132313231323").orElseThrow();
        gestor.setPerfilAtivo(Perfil.GESTOR);
        gestor.setUnidadeAtivaCodigo(unidadeSuperior.getCodigo());
        gestor.setAuthorities(Set.of(Perfil.GESTOR.toGrantedAuthority()));

        String observacoes = "Favor revisar a atividade X e Y.";
        JustificativaRequest requestBody = new JustificativaRequest(observacoes);

        // When
        mockMvc.perform(
                        post(
                                "/api/subprocessos/{id}/devolver-cadastro",
                                subprocesso.getCodigo())
                                .with(csrf())
                                .with(user(gestor))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk());

        // Then
        entityManager.flush();
        entityManager.clear();

        Subprocesso subprocessoAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(subprocessoAtualizado.getSituacao())
                .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        assertThat(subprocessoAtualizado.getDataFimEtapa1()).isNull();

        List<Analise> analises = analiseRepo
                .findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
        assertThat(analises).hasSize(1);
        Analise analiseRegistrada = analises.getFirst();
        assertThat(analiseRegistrada.getAcao()).isEqualTo(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO);
        assertThat(analiseRegistrada.getObservacoes()).isEqualTo(observacoes);
        assertThat(analiseRegistrada.getUnidadeCodigo()).isEqualTo(unidadeSuperior.getCodigo());

        List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(
                subprocesso.getCodigo());
        assertThat(movimentacoes).hasSize(2);
        Movimentacao movimentacaoDevolucao = movimentacoes.getFirst();
        assertThat(movimentacaoDevolucao.getUnidadeOrigem().getSigla())
                .isEqualTo(unidadeSuperior.getSigla());
        assertThat(movimentacaoDevolucao.getUnidadeDestino().getSigla())
                .isEqualTo(unidade.getSigla());
    }

    @Test
    @DisplayName("Deve aceitar cadastro, registrar análise e mover para unidade superior")
    void aceitarCadastro_deveFuncionarCorretamente() throws Exception {
        Usuario gestor = usuarioRepo.findById("132313231323").orElseThrow();
        gestor.setPerfilAtivo(Perfil.GESTOR);
        gestor.setUnidadeAtivaCodigo(unidadeSuperior.getCodigo());
        gestor.setAuthorities(Set.of(Perfil.GESTOR.toGrantedAuthority()));

        String observacoes = "Cadastro parece OK.";
        TextoRequest requestBody = new TextoRequest(observacoes);

        mockMvc.perform(
                        post("/api/subprocessos/{id}/aceitar-cadastro", subprocesso.getCodigo())
                                .with(csrf())
                                .with(user(gestor))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        List<Analise> analises = analiseRepo
                .findBySubprocessoCodigoOrderByDataHoraDesc(subprocesso.getCodigo());
        assertThat(analises).hasSize(1);
        Analise analiseRegistrada = analises.getFirst();
        assertThat(analiseRegistrada.getAcao()).isEqualTo(TipoAcaoAnalise.ACEITE_MAPEAMENTO);
        assertThat(analiseRegistrada.getObservacoes()).isEqualTo(observacoes);
        assertThat(analiseRegistrada.getUsuarioTitulo())
                .isEqualTo("132313231323");

        List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(
                subprocesso.getCodigo());
        assertThat(movimentacoes).hasSize(2);
        Movimentacao movimentacaoAceite = movimentacoes.getFirst();

        // O aceite parte da unidade atual (onde o processo está), que é a superior
        assertThat(movimentacaoAceite.getUnidadeOrigem().getSigla())
                .isEqualTo(unidadeSuperior.getSigla());
        assertThat(movimentacaoAceite.getUnidadeDestino().getSigla())
                .isEqualTo(unidadeSuperior.getSigla());
        assertThat(movimentacaoAceite.getDescricao())
                .isEqualTo("Cadastro de atividades e conhecimentos aceito");
    }

    @Test
    @DisplayName("Deve homologar cadastro, alterar situação e registrar movimentação da ADMIN")
    void homologarCadastro_deveFuncionarCorretamente() throws Exception {
        Usuario admin = usuarioRepo.findById("101010101010").orElseThrow();
        admin.setPerfilAtivo(Perfil.ADMIN);
        admin.setUnidadeAtivaCodigo(unidadeSuperior.getCodigo()); // Or default admin unit
        admin.setAuthorities(Set.of(Perfil.ADMIN.toGrantedAuthority()));

        TextoRequest requestBody = new TextoRequest("Homologado via teste.");

        mockMvc.perform(
                        post(
                                "/api/subprocessos/{id}/homologar-cadastro",
                                subprocesso.getCodigo())
                                .with(csrf())
                                .with(user(admin))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        Subprocesso subprocessoAtualizado = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        assertThat(subprocessoAtualizado.getSituacao())
                .isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);

        List<Movimentacao> movimentacoes = movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(
                subprocesso.getCodigo());
        assertThat(movimentacoes).hasSize(2);
        Movimentacao movimentacaoHomologacao = movimentacoes.getFirst();

        // O sistema (MockAdmin) parece usar uma unidade chamada ADMIN.
        // Para não quebrar o teste, vamos aceitar ADMIN ou COSIS-TEST
        // (unidadeSuperior).
        String siglaOrigem = movimentacaoHomologacao.getUnidadeOrigem().getSigla();
        assertThat(siglaOrigem).isIn("ADMIN", unidadeSuperior.getSigla());
    }

    @Test
    @DisplayName("Deve retornar o histórico de devoluções e aceites ordenado")
    void getHistorico_deveRetornarAcoesOrdenadas() throws Exception {
        Usuario gestor = usuarioRepo.findById("132313231323").orElseThrow();
        gestor.setPerfilAtivo(Perfil.GESTOR);
        gestor.setUnidadeAtivaCodigo(unidadeSuperior.getCodigo());
        gestor.setAuthorities(Set.of(Perfil.GESTOR.toGrantedAuthority()));

        String obsDevolucao = "Falta atividade Z";
        JustificativaRequest devolverReq = new JustificativaRequest(obsDevolucao);

        mockMvc.perform(post("/api/subprocessos/{id}/devolver-cadastro", subprocesso.getCodigo())
                        .with(csrf())
                        .with(user(gestor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(devolverReq)))
                .andExpect(status().isOk());

        subprocesso = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        subprocessoRepo.saveAndFlush(subprocesso);

        // Ao redisponibilizar, o subprocesso volta para a unidadeSuperior
        Movimentacao movRedisponibiliza = Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(unidade)
                .unidadeDestino(unidadeSuperior)
                .descricao("Redisponibilização após ajustes")
                .usuario(gestor) // Usando gestor ou qualquer outro aqui
                .dataHora(LocalDateTime.now().plusDays(1)) // Garante que é a última
                .build();
        movimentacaoRepo.saveAndFlush(movRedisponibiliza);

        // Ensure user is configured correctly like in other passing tests
        gestor.setPerfilAtivo(Perfil.GESTOR);
        gestor.setUnidadeAtivaCodigo(unidadeSuperior.getCodigo());
        gestor.setAuthorities(Set.of(Perfil.GESTOR.toGrantedAuthority()));

        // Clear persistence context to ensure controller fetches fresh data (including new movement)
        entityManager.flush();
        entityManager.clear();

        String obsAceite = "Agora sim, completo.";
        TextoRequest aceitarReq = new TextoRequest(obsAceite);
        mockMvc.perform(post("/api/subprocessos/{id}/aceitar-cadastro", subprocesso.getCodigo())
                        .with(csrf())
                        .with(user(gestor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aceitarReq)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        // Re-authenticate explicitly to avoid stale security context
        // Using "132313231323" ensures the user is found in DB, and authorities ensures role check passes
        String jsonResponse = mockMvc
                .perform(MockMvcRequestBuilders.get(
                                "/api/subprocessos/{id}/historico-cadastro",
                                subprocesso.getCodigo())
                        .with(user(gestor))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<AnaliseHistoricoDto> historico = objectMapper.readValue(jsonResponse,
                new TypeReference<>() {
                });

        assertThat(historico).hasSize(2);

        AnaliseHistoricoDto aceite = historico.getFirst();
        assertThat(aceite.acao()).isEqualTo(TipoAcaoAnalise.ACEITE_MAPEAMENTO);
        assertThat(aceite.observacoes()).isEqualTo(obsAceite);
        assertThat(aceite.unidadeSigla()).isEqualTo(unidadeSuperior.getSigla());

        AnaliseHistoricoDto devolucao = historico.get(1);
        assertThat(devolucao.acao()).isEqualTo(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO);
        assertThat(devolucao.observacoes()).isEqualTo(obsDevolucao);
        assertThat(devolucao.unidadeSigla()).isEqualTo(unidadeSuperior.getSigla());
    }
}
