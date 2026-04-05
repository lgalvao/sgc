package sgc.e2e;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.*;
import org.springframework.core.io.*;
import org.springframework.core.io.Resource;
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
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;
import sgc.processo.service.*;
import sgc.subprocesso.model.*;

import javax.sql.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

@RestController
@RequestMapping("/e2e")
@Profile("e2e")
@ConditionalOnProperty(name = "aplicacao.ambiente-testes", havingValue = "true")
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
    private final CacheManager cacheManager;

    @PostConstruct
    public void validarAmbienteE2e() {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) {
            throw new ErroConfiguracao("DataSource indisponível para o perfil e2e");
        }

        try (Connection conn = dataSource.getConnection()) {
            String jdbcUrl = conn.getMetaData().getURL();
            String databaseProductName = conn.getMetaData().getDatabaseProductName();
            boolean h2 = "H2".equalsIgnoreCase(databaseProductName);
            boolean jdbcH2 = jdbcUrl != null && jdbcUrl.startsWith("jdbc:h2:");

            if (!h2 || !jdbcH2) {
                throw new ErroConfiguracao(
                        "Perfil e2e requer H2 em memória. Banco detectado: %s (%s)"
                                .formatted(databaseProductName, jdbcUrl));
            }
        } catch (SQLException e) {
            throw new ErroConfiguracao("Falha ao validar DataSource do perfil e2e: " + e.getMessage());
        }
    }


    @PostMapping("/reset-database")
    public void resetDatabase() {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) return;

        try (Connection conn = dataSource.getConnection()) {
            executeDatabaseReset(conn);
            limparCaches();
            log.info("Reset do banco de dados e caches concluído.");
        } catch (Exception e) {
            log.error("Erro crítico ao resetar banco de dados", e);
            throw new ErroConfiguracao("Falha no reset do banco: %s".formatted(e.getMessage()));
        }
    }

    private void limparCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
        log.info("Todos os caches de aplicação foram limpos.");
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

    private void limparTabela(Statement stmt, String table) {
        log.debug("Limpando tabela: sgc.{}", table);
        try {
            // nosemgrep: java.lang.security.audit.formatted-sql-string.formatted-sql-string
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

            String subquerySubprocessos = "(SELECT codigo FROM sgc.subprocesso WHERE processo_codigo = ?)";
            String subqueryMapas = "(SELECT codigo FROM sgc.mapa WHERE subprocesso_codigo IN " + subquerySubprocessos + ")";

            jdbcTemplate.update("DELETE FROM sgc.alerta_usuario WHERE alerta_codigo IN (SELECT codigo FROM sgc.alerta WHERE processo_codigo = ?)", codigo);
            jdbcTemplate.update("DELETE FROM sgc.alerta WHERE processo_codigo = ?", codigo);

            jdbcTemplate.update("DELETE FROM sgc.conhecimento WHERE atividade_codigo IN (SELECT codigo FROM sgc.atividade WHERE mapa_codigo IN " + subqueryMapas + ")", codigo);
            jdbcTemplate.update("DELETE FROM sgc.competencia_atividade WHERE atividade_codigo IN (SELECT codigo FROM sgc.atividade WHERE mapa_codigo IN " + subqueryMapas + ")", codigo);
            jdbcTemplate.update("DELETE FROM sgc.competencia_atividade WHERE competencia_codigo IN (SELECT codigo FROM sgc.competencia WHERE mapa_codigo IN " + subqueryMapas + ")", codigo);
            jdbcTemplate.update("DELETE FROM sgc.atividade WHERE mapa_codigo IN " + subqueryMapas, codigo);
            jdbcTemplate.update("DELETE FROM sgc.competencia WHERE mapa_codigo IN " + subqueryMapas, codigo);

            // Limpar referências de Mapa vigente antes de excluir o Mapa
            jdbcTemplate.update("DELETE FROM sgc.unidade_mapa WHERE mapa_vigente_codigo IN " + subqueryMapas, codigo);
            jdbcTemplate.update("DELETE FROM sgc.mapa WHERE subprocesso_codigo IN " + subquerySubprocessos, codigo);

            jdbcTemplate.update("DELETE FROM sgc.analise WHERE subprocesso_codigo IN " + subquerySubprocessos, codigo);
            jdbcTemplate.update("DELETE FROM sgc.notificacao WHERE subprocesso_codigo IN " + subquerySubprocessos, codigo);
            jdbcTemplate.update("DELETE FROM sgc.movimentacao WHERE subprocesso_codigo IN " + subquerySubprocessos, codigo);
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
     * Cria um processo de mapeamento via API para testes E2E.
     */
    @PostMapping("/fixtures/processo-mapeamento")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoMapeamento(@RequestBody ProcessoFixtureRequest request) {
        return executeAsAdmin(() -> criarProcessoFixture(request, TipoProcesso.MAPEAMENTO));
    }

    /**
     * Cria um processo de revisão via API para testes E2E.
     */
    @PostMapping("/fixtures/processo-revisao")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoRevisao(@RequestBody ProcessoFixtureRequest request) {
        return executeAsAdmin(() -> criarProcessoFixture(request, TipoProcesso.REVISAO));
    }

    /**
     * Cria um processo já finalizado e insere atividades/mapa forçadamente via SQL
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
        setSituacaoProcesso(codProcesso);

        // Tornar mapa vigente na unidade
        jdbcTemplate.update("DELETE FROM sgc.unidade_mapa WHERE unidade_codigo = ?", codUnidade);
        jdbcTemplate.update("INSERT INTO sgc.unidade_mapa (unidade_codigo, mapa_vigente_codigo) VALUES (?, ?)", codUnidade, codMapa);
        limparCaches();

        return processoService.buscarPorCodigo(codProcesso);
    }

    /**
     * Cria um processo de mapeamento já iniciado, com cadastro preenchido e disponibilizado
     */
    @PostMapping("/fixtures/processo-mapeamento-com-cadastro-disponibilizado")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoMapeamentoComCadastroDisponibilizado(@RequestBody ProcessoFixtureRequest request) {
        return criarProcessoMapeamentoComMapaNaSituacao(request, "MAPEAMENTO_CADASTRO_DISPONIBILIZADO");
    }

    /**
     * Cria um processo de mapeamento já iniciado, com mapa preenchido e disponibilizado,
     */
    @PostMapping("/fixtures/processo-mapeamento-com-mapa-disponibilizado")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoMapeamentoComMapaDisponibilizado(@RequestBody ProcessoFixtureRequest request) {
        return criarProcessoMapeamentoComMapaNaSituacao(request, "MAPEAMENTO_MAPA_DISPONIBILIZADO");
    }

    /**
     * Cria um processo de mapeamento já iniciado, com mapa preenchido e com sugestões registradas,
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
     */
    @PostMapping("/fixtures/processo-mapeamento-com-mapa-validado")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoMapeamentoComMapaValidado(@RequestBody ProcessoFixtureRequest request) {
        return criarProcessoMapeamentoComMapaNaSituacao(request, "MAPEAMENTO_MAPA_VALIDADO");
    }

    /**
     * Cria um processo de mapeamento já iniciado, com mapa preenchido e homologado,
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
     * Cria um processo de revisão já iniciado, com mapa preenchido e homologado
     */
    @SuppressWarnings("UnusedReturnValue")
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
    @SuppressWarnings("UnusedReturnValue")
    @PostMapping("/fixtures/processo-revisao-com-cadastro-homologado")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoRevisaoComCadastroHomologado(@RequestBody ProcessoFixtureRequest request) {
        return executeAsAdmin(() -> criarProcessoRevisaoCadastroHomologadoFixture(request));
    }

    /**
     * Cria um processo de revisão já iniciado, com cadastro disponibilizado pelo CHEFE,
     */
    @PostMapping("/fixtures/processo-revisao-com-cadastro-disponibilizado")
    @Transactional
    @JsonView(ProcessoViews.Publica.class)
    public Processo criarProcessoRevisaoComCadastroDisponibilizado(@RequestBody ProcessoFixtureRequest request) {
        return executeAsAdmin(() -> criarProcessoRevisaoCadastroDisponibilizadoFixture(request));
    }

    private Processo criarProcessoMapeamentoComMapaNaSituacao(ProcessoFixtureRequest request, String situacaoSubprocesso) {
        return criarProcessoNaSituacao(request, situacaoSubprocesso);
    }

    private Processo criarProcessoRevisaoCadastroDisponibilizadoFixture(ProcessoFixtureRequest request) {
        int diasLimite = request.diasLimite() != null ? request.diasLimite() : 30;
        Unidade unidade = unidadeService.buscarPorSigla(request.unidadeSigla());

        ProcessoFixtureRequest requestMapeamento = ProcessoFixtureRequest.mapaBase(request.unidadeSigla(), diasLimite);
        Processo processoMapeamento = criarProcessoFixture(requestMapeamento, TipoProcesso.MAPEAMENTO);
        Long codSubprocessoMapeamento = subprocessoRepo
                .findByProcessoCodigoAndUnidadeCodigo(processoMapeamento.getCodigo(), unidade.getCodigo())
                .map(Subprocesso::getCodigo)
                .orElseThrow();
        Long codMapaVigente = mapaRepo.buscarPorSubprocesso(codSubprocessoMapeamento)
                .map(Mapa::getCodigo)
                .orElseThrow();

        inserirAtividadeComConhecimento(codMapaVigente, "Atividade fixture 1", "Conhecimento fixture 1A");
        inserirAtividadeComConhecimento(codMapaVigente, "Atividade fixture 2", "Conhecimento fixture 2A");

        setSituacaoSubprocesso(codSubprocessoMapeamento, SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        setSituacaoProcesso(processoMapeamento.getCodigo());
        jdbcTemplate.update("DELETE FROM sgc.unidade_mapa WHERE unidade_codigo = ?", unidade.getCodigo());
        jdbcTemplate.update("INSERT INTO sgc.unidade_mapa (unidade_codigo, mapa_vigente_codigo) VALUES (?, ?)",
                unidade.getCodigo(), codMapaVigente);
        limparCaches();

        ProcessoFixtureRequest requestRevisao = ProcessoFixtureRequest.iniciado(
                request.descricao(),
                request.unidadeSigla(),
                diasLimite
        );
        Processo processoRevisao = criarProcessoFixture(requestRevisao, TipoProcesso.REVISAO);
        Long codSubprocessoRevisao = subprocessoRepo
                .findByProcessoCodigoAndUnidadeCodigo(processoRevisao.getCodigo(), unidade.getCodigo())
                .map(Subprocesso::getCodigo)
                .orElseThrow();

        setSituacaoSubprocesso(codSubprocessoRevisao, SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        registrarMovimentacaoFixture(codSubprocessoRevisao, unidade.getCodigo(),
                obterUnidadeSuperiorObrigatoria(unidade).getCodigo(),
                "Disponibilização da revisão do cadastro via fixture");

        return processoService.buscarPorCodigo(processoRevisao.getCodigo());
    }

    private Processo criarProcessoNaSituacao(ProcessoFixtureRequest request, String situacaoSubprocesso) {
        ProcessoFixtureRequest requestIniciado = ProcessoFixtureRequest.iniciado(
                request.descricao(),
                request.unidadeSigla(),
                request.diasLimite()
        );
        Processo processo = executeAsAdmin(() -> criarProcessoFixture(requestIniciado, TipoProcesso.MAPEAMENTO));

        Long codProcesso = processo.getCodigo();
        Unidade unidade = unidadeService.buscarPorSigla(request.unidadeSigla());
        Long codSuperior = obterUnidadeSuperiorObrigatoria(unidade).getCodigo();
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
        Long codAtividade = consultarCodigoGerado(
                "SELECT codigo FROM sgc.atividade WHERE mapa_codigo = ? AND descricao = ?",
                codMapa, "Atividade fixture" + sufixo);

        jdbcTemplate.update("INSERT INTO sgc.conhecimento (atividade_codigo, descricao) VALUES (?, ?)",
                codAtividade, "Conhecimento fixture" + sufixo);
        jdbcTemplate.update("INSERT INTO sgc.competencia (mapa_codigo, descricao) VALUES (?, ?)",
                codMapa, "Competência fixture" + sufixo);
        Long codCompetencia = consultarCodigoGerado(
                "SELECT codigo FROM sgc.competencia WHERE mapa_codigo = ? AND descricao = ?",
                codMapa, "Competência fixture" + sufixo);
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
        ProcessoFixtureRequest requestIniciado = ProcessoFixtureRequest.iniciado(
                request.descricao(),
                request.unidadeSigla(),
                request.diasLimite()
        );
        Processo processo = criarProcessoFixture(requestIniciado, TipoProcesso.MAPEAMENTO);

        Long codProcesso = processo.getCodigo();
        Unidade unidade = unidadeService.buscarPorSigla(request.unidadeSigla());
        Unidade admin = unidadeService.buscarAdmin();
        Long codSubprocesso = subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(codProcesso, unidade.getCodigo())
                .map(Subprocesso::getCodigo)
                .orElseThrow();
        Long codMapa = mapaRepo.buscarPorSubprocesso(codSubprocesso)
                .map(Mapa::getCodigo)
                .orElseThrow();

        inserirAtividadesFixtureMapaSemCompetencias(codMapa);
        setSituacaoSubprocesso(codSubprocesso, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        Long codAdmin = admin.getCodigo();
        registrarMovimentacaoFixture(codSubprocesso, codAdmin, codAdmin, "Cadastro homologado via fixture");

        return processoService.buscarPorCodigo(codProcesso);
    }

    private Processo criarProcessoRevisaoHomologadoFixture(ProcessoFixtureRequest request) {
        if (request.unidadeSigla().isBlank()) {
            throw new ErroValidacao("Unidade é obrigatória");
        }

        Unidade unidade = unidadeService.buscarPorSigla(request.unidadeSigla());
        Unidade admin = unidadeService.buscarAdmin();
        int diasLimite = request.diasLimite() != null ? request.diasLimite() : 30;
        LocalDateTime agora = LocalDateTime.now();

        Processo processo = Processo.builder()
                .descricao(descricaoFixture(request))
                .tipo(TipoProcesso.REVISAO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataCriacao(agora)
                .dataLimite(LocalDate.now().plusDays(diasLimite).atStartOfDay())
                .build();
        processo.adicionarParticipantes(Set.of(unidade));
        processo = processoRepo.saveAndFlush(processo);

        Subprocesso subprocesso = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .situacao(SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO)
                .dataLimiteEtapa1(agora.plusDays(diasLimite))
                .dataFimEtapa1(agora)
                .build();
        subprocesso = subprocessoRepo.saveAndFlush(subprocesso);

        Mapa mapa = Mapa.builder()
                .subprocesso(subprocesso)
                .dataHoraDisponibilizado(agora.minusHours(1))
                .dataHoraHomologado(agora)
                .build();
        mapa = mapaRepo.saveAndFlush(mapa);

        subprocesso.setMapa(mapa);
        subprocessoRepo.saveAndFlush(subprocesso);

        registrarMovimentacaoFixture(
                subprocesso.getCodigo(),
                admin.getCodigo(),
                admin.getCodigo(),
                "Mapa homologado via fixture"
        );

        return processoService.buscarPorCodigo(processo.getCodigo());
    }

    private Processo criarProcessoRevisaoCadastroHomologadoFixture(ProcessoFixtureRequest request) {
        if (request.unidadeSigla().isBlank()) {
            throw new ErroValidacao("Unidade é obrigatória");
        }

        int diasLimite = request.diasLimite() != null ? request.diasLimite() : 30;
        Unidade unidade = unidadeService.buscarPorSigla(request.unidadeSigla());
        Unidade admin = unidadeService.buscarAdmin();

        ProcessoFixtureRequest requestMapeamento = ProcessoFixtureRequest.mapaBase(request.unidadeSigla(), diasLimite);
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
        setSituacaoProcesso(codProcessoMapeamento);
        jdbcTemplate.update("DELETE FROM sgc.unidade_mapa WHERE unidade_codigo = ?", unidade.getCodigo());
        jdbcTemplate.update("INSERT INTO sgc.unidade_mapa (unidade_codigo, mapa_vigente_codigo) VALUES (?, ?)",
                unidade.getCodigo(), codMapaVigente);
        limparCaches();

        ProcessoFixtureRequest requestRevisao = ProcessoFixtureRequest.iniciado(
                request.descricao(),
                request.unidadeSigla(),
                diasLimite
        );
        Processo processoRevisao = criarProcessoFixture(requestRevisao, TipoProcesso.REVISAO);
        Long codProcessoRevisao = processoRevisao.getCodigo();
        Long codSubprocessoRevisao = subprocessoRepo.findByProcessoCodigoAndUnidadeCodigo(codProcessoRevisao, unidade.getCodigo())
                .map(Subprocesso::getCodigo)
                .orElseThrow();
        Long codMapaRevisao = mapaRepo.buscarPorSubprocesso(codSubprocessoRevisao)
                .map(Mapa::getCodigo)
                .orElseThrow();

        Long codAtividadeRevisao2 = consultarCodigoGerado(
                "SELECT codigo FROM sgc.atividade WHERE mapa_codigo = ? AND descricao = ?",
                codMapaRevisao, "Atividade fixture 2");
        jdbcTemplate.update("DELETE FROM sgc.conhecimento WHERE atividade_codigo = ?", codAtividadeRevisao2);
        jdbcTemplate.update("INSERT INTO sgc.conhecimento (atividade_codigo, descricao) VALUES (?, ?)",
                codAtividadeRevisao2, "Conhecimento fixture 2B");

        Long codAtividadeRemovida = consultarCodigoGerado(
                "SELECT codigo FROM sgc.atividade WHERE mapa_codigo = ? AND descricao = ?",
                codMapaRevisao, "Atividade fixture 3");
        jdbcTemplate.update("DELETE FROM sgc.conhecimento WHERE atividade_codigo = ?", codAtividadeRemovida);
        jdbcTemplate.update("DELETE FROM sgc.competencia_atividade WHERE atividade_codigo = ?", codAtividadeRemovida);
        jdbcTemplate.update("DELETE FROM sgc.atividade WHERE codigo = ?", codAtividadeRemovida);
        jdbcTemplate.update("DELETE FROM sgc.competencia WHERE mapa_codigo = ? AND descricao = ?",
                codMapaRevisao, "Competência fixture 3");

        inserirAtividadeComConhecimento(codMapaRevisao, "Atividade nova revisão fixture", "Conhecimento novo");
        setSituacaoSubprocesso(codSubprocessoRevisao, SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        Long codAdmin = admin.getCodigo();
        registrarMovimentacaoFixture(codSubprocessoRevisao, codAdmin, codAdmin, "Revisão homologada via fixture");

        return processoService.buscarPorCodigo(codProcessoRevisao);
    }

    private void inserirAtividadesFixtureMapaSemCompetencias(Long codMapa) {
        inserirAtividadeComConhecimento(codMapa, "Atividade fixture 1", "Conhecimento fixture 1A");
        inserirAtividadeComConhecimento(codMapa, "Atividade fixture 2", "Conhecimento fixture 2A");
        inserirAtividadeComConhecimento(codMapa, "Atividade fixture 3", "Conhecimento fixture 3A");
    }

    private Long inserirAtividadeComConhecimento(Long codMapa, String atividade, String conhecimento) {
        jdbcTemplate.update("INSERT INTO sgc.atividade (mapa_codigo, descricao) VALUES (?, ?)", codMapa, atividade);
        Long codAtividade = consultarCodigoGerado(
                "SELECT codigo FROM sgc.atividade WHERE mapa_codigo = ? AND descricao = ?",
                codMapa, atividade);
        jdbcTemplate.update("INSERT INTO sgc.conhecimento (atividade_codigo, descricao) VALUES (?, ?)", codAtividade, conhecimento);
        return codAtividade;
    }

    private void inserirCompetenciaComAtividade(Long codMapa, String competencia, Long codAtividade) {
        jdbcTemplate.update("INSERT INTO sgc.competencia (mapa_codigo, descricao) VALUES (?, ?)", codMapa, competencia);
        Long codCompetencia = consultarCodigoGerado(
                "SELECT codigo FROM sgc.competencia WHERE mapa_codigo = ? AND descricao = ?",
                codMapa, competencia);
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

    private String descricaoFixture(ProcessoFixtureRequest request) {
        String descReq = request.descricao();
        if (descReq != null && !descReq.isBlank()) {
            return descReq;
        }
        return "Processo fixture E2E " + TipoProcesso.REVISAO.name() + " " + System.currentTimeMillis();
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
            processoService.iniciar(processoCodigo, unidades, usuario);

            // Recarregar processo após iniciar
            processo = processoService.buscarPorCodigo(processoCodigo);
        }

        return processo;
    }

    private Long consultarCodigoGerado(String sql, Object... args) {
        Long codigo = jdbcTemplate.queryForObject(sql, Long.class, args);
        if (codigo == null) {
            throw new IllegalStateException("Fixture E2E não encontrou codigo gerado para SQL: " + sql);
        }
        return codigo;
    }

    private Unidade obterUnidadeSuperiorObrigatoria(Unidade unidade) {
        Unidade unidadeSuperior = unidade.getUnidadeSuperior();
        if (unidadeSuperior == null) {
            throw new IllegalStateException("Unidade %s sem unidade superior para fixture E2E".formatted(unidade.getSigla()));
        }
        return unidadeSuperior;
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

    private void setSituacaoProcesso(Long codProcesso) {
        Processo p = processoRepo.findById(codProcesso).orElseThrow();
        p.setSituacao(SituacaoProcesso.FINALIZADO);
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
        private static ProcessoFixtureRequest iniciado(@Nullable String descricao, String unidadeSigla, @Nullable Integer diasLimite) {
            return new ProcessoFixtureRequest(descricao, unidadeSigla, true, diasLimite);
        }

        private static ProcessoFixtureRequest mapaBase(String unidadeSigla, @Nullable Integer diasLimite) {
            return iniciado("Mapa base fixture " + System.currentTimeMillis(), unidadeSigla, diasLimite);
        }
    }
}
