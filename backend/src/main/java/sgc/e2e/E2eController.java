package sgc.e2e;

import com.fasterxml.jackson.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.context.annotation.*;
import org.springframework.core.io.*;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.datasource.init.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.core.context.*;
import org.springframework.transaction.annotation.*;
import org.springframework.web.bind.annotation.*;
import sgc.comum.erros.*;
import sgc.mapa.model.*;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;
import sgc.processo.service.ProcessoService;
import sgc.subprocesso.model.*;

import javax.sql.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

@SuppressWarnings("JvmTaintAnalysis")
@RestController
@RequestMapping("/e2e")
@Profile("e2e")
@Slf4j
@RequiredArgsConstructor
public class E2eController {
    private static final String SQL_SUBPROCESSO_POR_PROCESSO = " sgc.subprocesso WHERE processo_codigo = ?)";
    private static final String TITULO_USUARIO_FIXTURE_ADMIN = "111111";

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final ProcessoService processoService;
    private final ProcessoRepo processoRepo;
    private final SubprocessoRepo subprocessoRepo;
    private final MapaRepo mapaRepo;
    private final UnidadeService unidadeService;
    private final UsuarioFacade usuarioFacade;
    private final ResourceLoader resourceLoader;


    @PostMapping("/reset-database")
    public void resetDatabase() {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) return;

        try (Connection conn = dataSource.getConnection()) {
            executeDatabaseReset(conn);
            log.info("Reset do banco de dados concluído.");
        } catch (Exception e) {
            log.error("Erro crítico ao resetar banco de dados", e);
            throw new ErroConfiguracao("Falha no reset do banco: %s".formatted(e.getMessage()));
        }
    }

    private void executeDatabaseReset(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            log.debug("Desabilitando integridade referencial");
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");

            List<String> tables = jdbcTemplate.queryForList(
                    "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_SCHEMA) = 'SGC'",
                    String.class).stream().flatMap(Stream::ofNullable).toList();

            log.debug("Limpando {} tabelas no schema SGC", tables.size());
            for (String table : tables) {
                limparTabela(stmt, table);
            }

            Resource seedResource = getSeedResource();
            log.debug("Executando script de seed: {}", seedResource.getFilename());
            ScriptUtils.executeSqlScript(conn, seedResource);

            log.debug("Reabilitando integridade referencial");
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
    }

    private void limparTabela(Statement stmt, String table) throws SQLException {
        log.debug("Limpando tabela: sgc.{}", table);
        try {
            stmt.execute("DELETE FROM sgc." + table);
        } catch (Exception e) {
            log.warn("Erro ao limpar tabela {}: {}", table, e.getMessage());
        }
    }

    private Resource getSeedResource() {
        Resource seedResource = resourceLoader.getResource("file:../e2e/setup/seed.sql");
        if (!seedResource.exists()) {
            seedResource = resourceLoader.getResource("file:e2e/setup/seed.sql");
        }
        if (!seedResource.exists()) {
            throw new ErroConfiguracao("Arquivo seed.sql não encontrado");
        }
        return seedResource;
    }

    @PostMapping("/processo/{codigo}/limpar-completo")
    @Transactional
    public void limparProcessoCompleto(@PathVariable Long codigo) {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) return;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");

            String subquerySubprocessos = "(SELECT codigo FROM sgc.subprocesso WHERE processo_codigo = " + codigo + ")";
            String subqueryMapas = "(SELECT codigo FROM sgc.mapa WHERE subprocesso_codigo IN " + subquerySubprocessos + ")";

            jdbcTemplate.update("DELETE FROM sgc.alerta_usuario WHERE alerta_codigo IN (SELECT codigo FROM sgc.alerta WHERE processo_codigo = ?)", codigo);
            jdbcTemplate.update("DELETE FROM sgc.alerta WHERE processo_codigo = ?", codigo);

            jdbcTemplate.update("DELETE FROM sgc.conhecimento WHERE atividade_codigo IN (SELECT codigo FROM sgc.atividade WHERE mapa_codigo IN " + subqueryMapas + ")");
            jdbcTemplate.update("DELETE FROM sgc.competencia_atividade WHERE atividade_codigo IN (SELECT codigo FROM sgc.atividade WHERE mapa_codigo IN " + subqueryMapas + ")");
            jdbcTemplate.update("DELETE FROM sgc.competencia_atividade WHERE competencia_codigo IN (SELECT codigo FROM sgc.competencia WHERE mapa_codigo IN " + subqueryMapas + ")");
            jdbcTemplate.update("DELETE FROM sgc.atividade WHERE mapa_codigo IN " + subqueryMapas);
            jdbcTemplate.update("DELETE FROM sgc.competencia WHERE mapa_codigo IN " + subqueryMapas);

            // Limpar referências de Mapa vigente antes de excluir o Mapa
            jdbcTemplate.update("DELETE FROM sgc.unidade_mapa WHERE mapa_vigente_codigo IN " + subqueryMapas);
            jdbcTemplate.update("DELETE FROM sgc.mapa WHERE subprocesso_codigo IN " + subquerySubprocessos);

            jdbcTemplate.update("DELETE FROM sgc.analise WHERE subprocesso_codigo IN " + subquerySubprocessos);
            jdbcTemplate.update("DELETE FROM sgc.notificacao WHERE subprocesso_codigo IN " + subquerySubprocessos);
            jdbcTemplate.update("DELETE FROM sgc.movimentacao WHERE subprocesso_codigo IN " + subquerySubprocessos);
            jdbcTemplate.update("DELETE FROM sgc.subprocesso WHERE processo_codigo = ?", codigo);

            jdbcTemplate.update("DELETE FROM sgc.unidade_processo WHERE processo_codigo = ?", codigo);
            jdbcTemplate.update("DELETE FROM sgc.processo WHERE codigo = ?", codigo);

            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
            log.info("Limpeza robusta do processo {} concluída.", codigo);
        } catch (SQLException e) {
            log.error("Erro na limpeza robusta do processo {}", codigo, e);
            throw new RuntimeException("Falha na limpeza do processo: " + e.getMessage());
        }
    }

    @PostMapping("/processo/{codigo}/limpar")
    @Transactional
    public void limparProcessoComDependentes(@PathVariable Long codigo) {
        String sqlMapas =
                "SELECT codigo FROM sgc.mapa WHERE subprocesso_codigo IN (SELECT codigo FROM" + SQL_SUBPROCESSO_POR_PROCESSO;
        List<Long> codigosMapas = jdbcTemplate.queryForList(sqlMapas, Long.class, codigo).stream()
                .flatMap(Stream::ofNullable)
                .toList();

        jdbcTemplate.update(
                "DELETE FROM sgc.analise WHERE subprocesso_codigo IN (SELECT codigo FROM"
                        + SQL_SUBPROCESSO_POR_PROCESSO,
                codigo);
        jdbcTemplate.update(
                "DELETE FROM sgc.notificacao WHERE subprocesso_codigo IN (SELECT codigo FROM"
                        + SQL_SUBPROCESSO_POR_PROCESSO,
                codigo);
        jdbcTemplate.update(
                "DELETE FROM sgc.movimentacao WHERE subprocesso_codigo IN (SELECT codigo FROM"
                        + SQL_SUBPROCESSO_POR_PROCESSO,
                codigo);


        if (!codigosMapas.isEmpty()) {
            Map<String, Object> params = Map.of("ids", codigosMapas);

            namedJdbcTemplate.update(
                    "DELETE FROM sgc.conhecimento WHERE atividade_codigo IN (SELECT codigo FROM"
                            + " sgc.atividade WHERE mapa_codigo IN (:ids))", params);
            namedJdbcTemplate.update(
                    "DELETE FROM sgc.competencia_atividade WHERE atividade_codigo IN (SELECT codigo"
                            + " FROM sgc.atividade WHERE mapa_codigo IN (:ids))", params);
            namedJdbcTemplate.update(
                    "DELETE FROM sgc.competencia_atividade WHERE competencia_codigo IN (SELECT"
                            + " codigo FROM sgc.competencia WHERE mapa_codigo IN (:ids))", params);
            namedJdbcTemplate.update("DELETE FROM sgc.atividade WHERE mapa_codigo IN (:ids)", params);
            namedJdbcTemplate.update("DELETE FROM sgc.competencia WHERE mapa_codigo IN (:ids)", params);
            namedJdbcTemplate.update("DELETE FROM sgc.mapa WHERE codigo IN (:ids)", params);
        }
        jdbcTemplate.update("""
                DELETE FROM sgc.alerta_usuario
                WHERE alerta_codigo IN (SELECT codigo FROM sgc.alerta WHERE processo_codigo = ?)""", codigo);
        jdbcTemplate.update("DELETE FROM sgc.alerta WHERE processo_codigo = ?", codigo);
        jdbcTemplate.update("DELETE FROM sgc.subprocesso WHERE processo_codigo = ?", codigo);
        jdbcTemplate.update("DELETE FROM sgc.unidade_processo WHERE processo_codigo = ?", codigo);
        jdbcTemplate.update("DELETE FROM sgc.processo WHERE codigo = ?", codigo);
    }

    /**
     * Cria um processo de mapeamento via API para testes E2E. Mais rápido que criar via UI.
     */
    @PostMapping("/fixtures/processo-mapeamento")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoMapeamento(@RequestBody ProcessoFixtureRequest request) {
        return executeAsAdmin(() -> criarProcessoFixture(request, TipoProcesso.MAPEAMENTO));
    }

    /**
     * Cria um processo de revisão via API para testes E2E. Mais rápido que criar via UI.
     */
    @PostMapping("/fixtures/processo-revisao")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoRevisao(@RequestBody ProcessoFixtureRequest request) {
        return executeAsAdmin(() -> criarProcessoFixture(request, TipoProcesso.REVISAO));
    }

    /**
     * Cria um processo já finalizado e insere atividades/mapa forçadamente via SQL
     * para pular o workflow e testar a importação de forma ultra-rápida.
     */
    @PostMapping("/fixtures/processo-finalizado-com-atividades")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoFinalizadoComAtividades(@RequestBody ProcessoFixtureRequest request) {
        Processo processo = executeAsAdmin(() -> criarProcessoFixture(request, TipoProcesso.MAPEAMENTO));
        Long codProcesso = processo.getCodigo();
        
        Unidade unidade = unidadeService.buscarPorSigla(request.unidadeSigla());
        Long codUnidade = unidade.getCodigo();

        Long codSubprocesso = subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(codProcesso, codUnidade)
                .map(Subprocesso::getCodigo)
                .orElseThrow();
        
        Long codMapa = mapaRepo.buscarPorSubprocesso(codSubprocesso)
                .map(Mapa::getCodigo)
                .orElseThrow();
        
        // Atividades com conhecimentos associados
        jdbcTemplate.update("INSERT INTO sgc.atividade (mapa_codigo, descricao) VALUES (?, ?)", codMapa, "Atividade origem A - " + codProcesso);
        jdbcTemplate.update("INSERT INTO sgc.atividade (mapa_codigo, descricao) VALUES (?, ?)", codMapa, "Atividade origem B - " + codProcesso);

        jdbcTemplate.update("INSERT INTO sgc.conhecimento (atividade_codigo, descricao) SELECT codigo, ? FROM sgc.atividade WHERE mapa_codigo = ? AND descricao = ?",
                "Conhecimento A - " + codProcesso, codMapa, "Atividade origem A - " + codProcesso);
        jdbcTemplate.update("INSERT INTO sgc.conhecimento (atividade_codigo, descricao) SELECT codigo, ? FROM sgc.atividade WHERE mapa_codigo = ? AND descricao = ?",
                "Conhecimento B - " + codProcesso, codMapa, "Atividade origem B - " + codProcesso);

        // Atualizar status para simular finalização
        setSituacaoSubprocesso(codSubprocesso, SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        setSituacaoProcesso(codProcesso, SituacaoProcesso.FINALIZADO);

        // Tornar mapa vigente na unidade
        jdbcTemplate.update("DELETE FROM sgc.unidade_mapa WHERE unidade_codigo = ?", codUnidade);
        jdbcTemplate.update("INSERT INTO sgc.unidade_mapa (unidade_codigo, mapa_vigente_codigo) VALUES (?, ?)", codUnidade, codMapa);

        return processoService.buscarPorCodigo(codProcesso);
    }

    /**
     * Cria um processo de mapeamento já iniciado, com cadastro preenchido e disponibilizado,
     * para acelerar cenários E2E que começam na análise do cadastro.
     */
    @PostMapping("/fixtures/processo-mapeamento-com-cadastro-disponibilizado")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoMapeamentoComCadastroDisponibilizado(@RequestBody ProcessoFixtureRequest request) {
        return criarProcessoMapeamentoComMapaNaSituacao(request, "MAPEAMENTO_CADASTRO_DISPONIBILIZADO");
    }

    /**
     * Cria um processo de mapeamento já iniciado, com mapa preenchido e disponibilizado,
     * para acelerar cenários E2E que começam na validação do mapa.
     */
    @PostMapping("/fixtures/processo-mapeamento-com-mapa-disponibilizado")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoMapeamentoComMapaDisponibilizado(@RequestBody ProcessoFixtureRequest request) {
        return criarProcessoMapeamentoComMapaNaSituacao(request, "MAPEAMENTO_MAPA_DISPONIBILIZADO");
    }

    /**
     * Cria um processo de mapeamento já iniciado, com mapa preenchido e com sugestões registradas,
     * para acelerar cenários E2E que começam na visualização de sugestões pelo GESTOR/ADMIN (CDU-20).
     */
    @PostMapping("/fixtures/processo-mapeamento-com-mapa-com-sugestoes")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoMapeamentoComMapaComSugestoes(@RequestBody ProcessoFixtureRequest request) {
        Processo processo = criarProcessoMapeamentoComMapaNaSituacao(request, "MAPEAMENTO_MAPA_COM_SUGESTOES");
        Unidade unidade = unidadeService.buscarPorSigla(request.unidadeSigla());
        Long codSubprocesso = subprocessoRepo
                .findByProcessoCodigoAndUnidadeCodigo(processo.getCodigo(), unidade.getCodigo())
                .map(Subprocesso::getCodigo)
                .orElseThrow();
        Mapa mapa = mapaRepo.buscarPorSubprocesso(codSubprocesso).orElseThrow();
        mapa.setSugestoes("Sugestão de ajuste na competência via fixture E2E");
        mapaRepo.save(mapa);
        return processoService.buscarPorCodigo(processo.getCodigo());
    }

    /**
     * Cria um processo de mapeamento já iniciado, com mapa preenchido e validado,
     * para acelerar cenários E2E que começam no aceite final do mapa.
     */
    @PostMapping("/fixtures/processo-mapeamento-com-mapa-validado")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoMapeamentoComMapaValidado(@RequestBody ProcessoFixtureRequest request) {
        return criarProcessoMapeamentoComMapaNaSituacao(request, "MAPEAMENTO_MAPA_VALIDADO");
    }

    /**
     * Cria um processo de mapeamento já iniciado, com mapa preenchido e homologado,
     * para acelerar cenários E2E que começam na finalização do processo.
     */
    @PostMapping("/fixtures/processo-mapeamento-com-mapa-homologado")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoMapeamentoComMapaHomologado(@RequestBody ProcessoFixtureRequest request) {
        return criarProcessoMapeamentoComMapaNaSituacao(request, "MAPEAMENTO_MAPA_HOMOLOGADO");
    }

    /**
     * Cria um processo de mapeamento já iniciado, com cadastro homologado pelo admin
     * e atividades suficientes para iniciar a montagem do mapa pela UI.
     */
    @PostMapping("/fixtures/processo-mapeamento-com-cadastro-homologado")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoMapeamentoComCadastroHomologado(@RequestBody ProcessoFixtureRequest request) {
        return executeAsAdmin(() -> criarProcessoMapeamentoCadastroHomologadoFixture(request));
    }

    /**
     * Cria um processo de revisão já iniciado, com mapa preenchido e homologado,
     * para acelerar cenários E2E que começam após o encerramento da revisão.
     */
    @PostMapping("/fixtures/processo-revisao-com-mapa-homologado")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoRevisaoComMapaHomologado(@RequestBody ProcessoFixtureRequest request) {
        return executeAsAdmin(() -> criarProcessoRevisaoHomologadoFixture(request));
    }

    /**
     * Cria um processo de revisão já iniciado, com cadastro homologado pelo admin
     * e mapa vigente prévio suficiente para exibir impactos na tela de ajuste.
     */
    @PostMapping("/fixtures/processo-revisao-com-cadastro-homologado")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoRevisaoComCadastroHomologado(@RequestBody ProcessoFixtureRequest request) {
        return executeAsAdmin(() -> criarProcessoRevisaoCadastroHomologadoFixture(request));
    }

    private Processo criarProcessoMapeamentoComMapaNaSituacao(ProcessoFixtureRequest request, String situacaoSubprocesso) {
        return criarProcessoNaSituacao(request, TipoProcesso.MAPEAMENTO, situacaoSubprocesso);
    }

    private Processo criarProcessoNaSituacao(ProcessoFixtureRequest request, TipoProcesso tipo, String situacaoSubprocesso) {
        ProcessoFixtureRequest requestIniciado = new ProcessoFixtureRequest(
                request.descricao(), request.unidadeSigla(), true, request.diasLimite());
        Processo processo = executeAsAdmin(() -> criarProcessoFixture(requestIniciado, tipo));

        Long codProcesso = processo.getCodigo();
        Unidade unidade = unidadeService.buscarPorSigla(request.unidadeSigla());
        Long codSuperior = unidade.getUnidadeSuperior().getCodigo();
        Long codUnidade = unidade.getCodigo();
        Long codSubprocesso = subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(codProcesso, codUnidade)
                .map(Subprocesso::getCodigo)
                .orElseThrow();
        Long codMapa = mapaRepo.buscarPorSubprocesso(codSubprocesso)
                .map(Mapa::getCodigo)
                .orElseThrow();

        String sufixo = " - " + codProcesso;
        jdbcTemplate.update("INSERT INTO sgc.atividade (mapa_codigo, descricao) VALUES (?, ?)",
                codMapa, "Atividade fixture" + sufixo);
        Long codAtividade = jdbcTemplate.queryForObject(
                "SELECT codigo FROM sgc.atividade WHERE mapa_codigo = ? AND descricao = ?",
                Long.class, codMapa, "Atividade fixture" + sufixo);

        jdbcTemplate.update("INSERT INTO sgc.conhecimento (atividade_codigo, descricao) VALUES (?, ?)",
                codAtividade, "Conhecimento fixture" + sufixo);
        jdbcTemplate.update("INSERT INTO sgc.competencia (mapa_codigo, descricao) VALUES (?, ?)",
                codMapa, "Competência fixture" + sufixo);
        Long codCompetencia = jdbcTemplate.queryForObject(
                "SELECT codigo FROM sgc.competencia WHERE mapa_codigo = ? AND descricao = ?",
                Long.class, codMapa, "Competência fixture" + sufixo);
        jdbcTemplate.update("INSERT INTO sgc.competencia_atividade (atividade_codigo, competencia_codigo) VALUES (?, ?)",
                codAtividade, codCompetencia);
        
        setSituacaoSubprocesso(codSubprocesso, SituacaoSubprocesso.valueOf(situacaoSubprocesso));

        // Definir localização baseada na situação para que os botões de ação apareçam para o ator correto:
        Long codDestino = codSuperior;
        if (situacaoSubprocesso.equals("MAPEAMENTO_MAPA_DISPONIBILIZADO") || situacaoSubprocesso.endsWith("_HOMOLOGADO")) {
            codDestino = codUnidade;
        }

        jdbcTemplate.update("INSERT INTO sgc.movimentacao (subprocesso_codigo, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo, data_hora, descricao) " +
                "VALUES (?, ?, ?, '111111', ?, ?)", codSubprocesso, codUnidade, codDestino, LocalDateTime.now(), "Movimentação automática via fixture");

        return processoService.buscarPorCodigo(codProcesso);
    }

    private Processo criarProcessoMapeamentoCadastroHomologadoFixture(ProcessoFixtureRequest request) {
        ProcessoFixtureRequest requestIniciado = new ProcessoFixtureRequest(
                request.descricao(), request.unidadeSigla(), true, request.diasLimite());
        Processo processo = criarProcessoFixture(requestIniciado, TipoProcesso.MAPEAMENTO);

        Long codProcesso = processo.getCodigo();
        Unidade unidade = unidadeService.buscarPorSigla(request.unidadeSigla());
        Unidade admin = unidadeService.buscarPorSigla("ADMIN");
        Long codSubprocesso = subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(codProcesso, unidade.getCodigo())
                .map(Subprocesso::getCodigo)
                .orElseThrow();
        Long codMapa = mapaRepo.buscarPorSubprocesso(codSubprocesso)
                .map(Mapa::getCodigo)
                .orElseThrow();

        inserirAtividadesFixtureMapaSemCompetencias(codMapa);
        setSituacaoSubprocesso(codSubprocesso, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        registrarMovimentacaoFixture(codSubprocesso, admin.getCodigo(), admin.getCodigo(), "Cadastro homologado via fixture");

        return processoService.buscarPorCodigo(codProcesso);
    }

    private Processo criarProcessoRevisaoHomologadoFixture(ProcessoFixtureRequest request) {
        if (request.unidadeSigla().isBlank()) {
            throw new ErroValidacao("Unidade é obrigatória");
        }

        Unidade unidade = unidadeService.buscarPorSigla(request.unidadeSigla());
        int diasLimite = request.diasLimite() != null ? request.diasLimite() : 30;
        LocalDateTime agora = LocalDateTime.now();

        Processo processo = new Processo();
        processo.setDescricao(descricaoFixture(request, TipoProcesso.REVISAO));
        processo.setTipo(TipoProcesso.REVISAO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDataCriacao(agora);
        processo.setDataLimite(LocalDate.now().plusDays(diasLimite).atStartOfDay());
        processo.adicionarParticipantes(Set.of(unidade));
        processo = processoRepo.saveAndFlush(processo);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setProcesso(processo);
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO);
        subprocesso.setDataLimiteEtapa1(agora.plusDays(diasLimite));
        subprocesso.setDataFimEtapa1(agora);
        subprocesso = subprocessoRepo.saveAndFlush(subprocesso);

        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapa.setDataHoraDisponibilizado(agora.minusHours(1));
        mapa.setDataHoraHomologado(agora);
        mapa = mapaRepo.saveAndFlush(mapa);

        subprocesso.setMapa(mapa);
        subprocessoRepo.saveAndFlush(subprocesso);

        return processoService.buscarPorCodigo(processo.getCodigo());
    }

    private Processo criarProcessoRevisaoCadastroHomologadoFixture(ProcessoFixtureRequest request) {
        if (request.unidadeSigla().isBlank()) {
            throw new ErroValidacao("Unidade é obrigatória");
        }

        int diasLimite = request.diasLimite() != null ? request.diasLimite() : 30;
        Unidade unidade = unidadeService.buscarPorSigla(request.unidadeSigla());
        Unidade admin = unidadeService.buscarPorSigla("ADMIN");

        ProcessoFixtureRequest requestMapeamento = new ProcessoFixtureRequest(
                "Mapa base fixture " + System.currentTimeMillis(), request.unidadeSigla(), true, diasLimite);
        Processo processoMapeamento = criarProcessoFixture(requestMapeamento, TipoProcesso.MAPEAMENTO);
        Long codProcessoMapeamento = processoMapeamento.getCodigo();
        Long codSubprocessoMapeamento = subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(codProcessoMapeamento, unidade.getCodigo())
                .map(Subprocesso::getCodigo)
                .orElseThrow();
        Long codMapaVigente = mapaRepo.buscarPorSubprocesso(codSubprocessoMapeamento)
                .map(Mapa::getCodigo)
                .orElseThrow();

        Long codAtividadeBase1 = inserirAtividadeComConhecimento(codMapaVigente, "Atividade fixture 1", "Conhecimento fixture 1A");
        Long codAtividadeBase2 = inserirAtividadeComConhecimento(codMapaVigente, "Atividade fixture 2", "Conhecimento fixture 2A");
        Long codAtividadeBase3 = inserirAtividadeComConhecimento(codMapaVigente, "Atividade fixture 3", "Conhecimento fixture 3A");

        inserirCompetenciaComAtividade(codMapaVigente, "Competência fixture 1", codAtividadeBase1);
        inserirCompetenciaComAtividade(codMapaVigente, "Competência fixture 2", codAtividadeBase2);
        inserirCompetenciaComAtividade(codMapaVigente, "Competência fixture 3", codAtividadeBase3);

        setSituacaoSubprocesso(codSubprocessoMapeamento, SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        setSituacaoProcesso(codProcessoMapeamento, SituacaoProcesso.FINALIZADO);
        jdbcTemplate.update("DELETE FROM sgc.unidade_mapa WHERE unidade_codigo = ?", unidade.getCodigo());
        jdbcTemplate.update("INSERT INTO sgc.unidade_mapa (unidade_codigo, mapa_vigente_codigo) VALUES (?, ?)",
                unidade.getCodigo(), codMapaVigente);

        ProcessoFixtureRequest requestRevisao = new ProcessoFixtureRequest(
                request.descricao(), request.unidadeSigla(), true, diasLimite);
        Processo processoRevisao = criarProcessoFixture(requestRevisao, TipoProcesso.REVISAO);
        Long codProcessoRevisao = processoRevisao.getCodigo();
        Long codSubprocessoRevisao = subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(codProcessoRevisao, unidade.getCodigo())
                .map(Subprocesso::getCodigo)
                .orElseThrow();
        Long codMapaRevisao = mapaRepo.buscarPorSubprocesso(codSubprocessoRevisao)
                .map(Mapa::getCodigo)
                .orElseThrow();

        Long codAtividadeRevisao2 = jdbcTemplate.queryForObject(
                "SELECT codigo FROM sgc.atividade WHERE mapa_codigo = ? AND descricao = ?",
                Long.class, codMapaRevisao, "Atividade fixture 2");
        jdbcTemplate.update("DELETE FROM sgc.conhecimento WHERE atividade_codigo = ?", codAtividadeRevisao2);
        jdbcTemplate.update("INSERT INTO sgc.conhecimento (atividade_codigo, descricao) VALUES (?, ?)",
                codAtividadeRevisao2, "Conhecimento fixture 2B");

        Long codAtividadeRemovida = jdbcTemplate.queryForObject(
                "SELECT codigo FROM sgc.atividade WHERE mapa_codigo = ? AND descricao = ?",
                Long.class, codMapaRevisao, "Atividade fixture 3");
        jdbcTemplate.update("DELETE FROM sgc.conhecimento WHERE atividade_codigo = ?", codAtividadeRemovida);
        jdbcTemplate.update("DELETE FROM sgc.competencia_atividade WHERE atividade_codigo = ?", codAtividadeRemovida);
        jdbcTemplate.update("DELETE FROM sgc.atividade WHERE codigo = ?", codAtividadeRemovida);
        jdbcTemplate.update("DELETE FROM sgc.competencia WHERE mapa_codigo = ? AND descricao = ?",
                codMapaRevisao, "Competência fixture 3");

        inserirAtividadeComConhecimento(codMapaRevisao, "Atividade nova revisão fixture", "Conhecimento novo");
        setSituacaoSubprocesso(codSubprocessoRevisao, SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        registrarMovimentacaoFixture(codSubprocessoRevisao, admin.getCodigo(), admin.getCodigo(), "Revisão homologada via fixture");

        return processoService.buscarPorCodigo(codProcessoRevisao);
    }

    private void inserirAtividadesFixtureMapaSemCompetencias(Long codMapa) {
        inserirAtividadeComConhecimento(codMapa, "Atividade fixture 1", "Conhecimento fixture 1A");
        inserirAtividadeComConhecimento(codMapa, "Atividade fixture 2", "Conhecimento fixture 2A");
        inserirAtividadeComConhecimento(codMapa, "Atividade fixture 3", "Conhecimento fixture 3A");
    }

    private Long inserirAtividadeComConhecimento(Long codMapa, String atividade, String conhecimento) {
        jdbcTemplate.update("INSERT INTO sgc.atividade (mapa_codigo, descricao) VALUES (?, ?)", codMapa, atividade);
        Long codAtividade = jdbcTemplate.queryForObject(
                "SELECT codigo FROM sgc.atividade WHERE mapa_codigo = ? AND descricao = ?",
                Long.class, codMapa, atividade);
        jdbcTemplate.update("INSERT INTO sgc.conhecimento (atividade_codigo, descricao) VALUES (?, ?)", codAtividade, conhecimento);
        return codAtividade;
    }

    private void inserirCompetenciaComAtividade(Long codMapa, String competencia, Long codAtividade) {
        jdbcTemplate.update("INSERT INTO sgc.competencia (mapa_codigo, descricao) VALUES (?, ?)", codMapa, competencia);
        Long codCompetencia = jdbcTemplate.queryForObject(
                "SELECT codigo FROM sgc.competencia WHERE mapa_codigo = ? AND descricao = ?",
                Long.class, codMapa, competencia);
        jdbcTemplate.update(
                "INSERT INTO sgc.competencia_atividade (atividade_codigo, competencia_codigo) VALUES (?, ?)",
                codAtividade, codCompetencia);
    }

    private void registrarMovimentacaoFixture(Long codSubprocesso, Long codOrigem, Long codDestino, String descricao) {
        jdbcTemplate.update(
                "INSERT INTO sgc.movimentacao (subprocesso_codigo, unidade_origem_codigo, unidade_destino_codigo, usuario_titulo, data_hora, descricao) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                codSubprocesso, codOrigem, codDestino, TITULO_USUARIO_FIXTURE_ADMIN, LocalDateTime.now(), descricao);
    }

    private String descricaoFixture(ProcessoFixtureRequest request, TipoProcesso tipo) {
        String descReq = request.descricao();
        if (descReq != null && !descReq.isBlank()) {
            return descReq;
        }
        return "Processo fixture E2E " + tipo.name() + " " + System.currentTimeMillis();
    }

    /**
     * Executa uma operação com contexto de segurança ADMIN.
     */
    private <T> T executeAsAdmin(Supplier<T> operation) {
        var auth = new UsernamePasswordAuthenticationToken(
                TITULO_USUARIO_FIXTURE_ADMIN,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        var previousAuth = SecurityContextHolder.getContext().getAuthentication();
        try {
            SecurityContextHolder.getContext().setAuthentication(auth);
            return operation.get();
        } finally {
            SecurityContextHolder.getContext().setAuthentication(previousAuth);
        }
    }

    /**
     * Método auxiliar para criar processos fixtures.
     */
    private Processo criarProcessoFixture(ProcessoFixtureRequest request, TipoProcesso tipo) {
        if (request.unidadeSigla().isBlank()) {
            throw new ErroValidacao("Unidade é obrigatória");
        }

        Unidade unidade = unidadeService.buscarPorSigla(request.unidadeSigla());

        int diasLimite = request.diasLimite() != null ? request.diasLimite() : 30;
        LocalDateTime dataLimite = LocalDate.now().plusDays(diasLimite).atStartOfDay();

        String descricao;
        String descReq = request.descricao();
        if (descReq != null && !descReq.isBlank()) {
            descricao = descReq;
        } else {
            descricao = "Processo fixture E2E " + tipo.name() + " " + System.currentTimeMillis();
        }

        CriarProcessoRequest criarReq = CriarProcessoRequest.builder()
                .descricao(descricao)
                .tipo(tipo)
                .dataLimiteEtapa1(dataLimite)
                .unidades(List.of(unidade.getCodigo()))
                .build();

        Processo processo = processoService.criar(criarReq);

        if (Boolean.TRUE.equals(request.iniciar())) {
            List<Long> unidades = List.of(unidade.getCodigo());
            Long processoCodigo = processo.getCodigo();
            Usuario usuario = obterUsuarioParaIniciacao();
            List<String> erros = processoService.iniciar(processoCodigo, unidades, usuario);

            if (!erros.isEmpty()) {
                throw new ErroValidacao("Falha ao iniciar processo fixture: " + String.join("; ", erros));
            }

            // Recarregar processo após iniciar
            processo = processoService.buscarPorCodigo(processoCodigo);
        }

        return processo;
    }

    private Usuario obterUsuarioParaIniciacao() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof Usuario u) {
            return u;
        }

        String login = (auth != null && auth.getPrincipal() instanceof String s) 
                ? s : TITULO_USUARIO_FIXTURE_ADMIN;
        
        return usuarioFacade.buscarPorLogin(login);
    }

    private void setSituacaoProcesso(Long codProcesso, SituacaoProcesso situacao) {
        Processo p = processoRepo.findById(codProcesso).orElseThrow();
        p.setSituacao(situacao);
        processoRepo.saveAndFlush(p);
    }

    private void setSituacaoSubprocesso(Long codSubprocesso, SituacaoSubprocesso situacao) {
        Subprocesso s = subprocessoRepo.findById(codSubprocesso).orElseThrow();
        s.setSituacaoForcada(situacao);
        subprocessoRepo.saveAndFlush(s);
    }

    /**
     * DTO para requisição de criação de processo fixture.
     */
    public record ProcessoFixtureRequest(
            @Nullable String descricao, String unidadeSigla, @Nullable Boolean iniciar, @Nullable Integer diasLimite) {
    }
}
