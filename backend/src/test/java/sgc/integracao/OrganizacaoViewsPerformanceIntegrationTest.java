package sgc.integracao;

import com.fasterxml.jackson.databind.*;
import jakarta.persistence.*;
import org.hibernate.*;
import org.hibernate.stat.*;
import org.junit.jupiter.api.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.env.*;
import org.springframework.data.domain.*;
import org.springframework.util.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;

import javax.sql.*;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.function.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

@Tag("integration")
class OrganizacaoViewsPerformanceIntegrationTest extends BaseIntegrationTest {
    private static final int AQUECIMENTOS = 1;
    private static final int REPETICOES_MEDIDAS = 3;
    private static final int TAMANHO_AMOSTRA = 10;
    private static final Path DIRETORIO_RELATORIOS = Path.of("etc", "benchmarks", "views");
    private static final Logger logger = LoggerFactory.getLogger(OrganizacaoViewsPerformanceIntegrationTest.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private Environment environment;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @Autowired
    private ResponsabilidadeRepo responsabilidadeRepo;

    @Autowired
    private UnidadeHierarquiaService unidadeHierarquiaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ResponsavelUnidadeService responsavelUnidadeService;

    @BeforeEach
    void garantirExecucaoSomenteNoOracle() throws SQLException {
        assumeTrue(estaUsandoOracle(), "Teste de performance das views deve rodar somente contra Oracle.");
    }

    @Test
    @DisplayName("Deve medir consultas mais comuns das views organizacionais no Oracle")
    void deveMedirConsultasMaisComunsDasViewsOrganizacionaisNoOracle() {
        AmostrasConsulta amostras = carregarAmostras();
        BancoInfo bancoInfo = carregarBancoInfo();

        List<ResultadoMedicao> resultados = List.of(
                medir("hierarquia.buscarArvoreHierarquica", unidadeHierarquiaService::buscarArvoreHierarquica),
                medir("hierarquia.buscarMapaHierarquia", unidadeHierarquiaService::buscarMapaHierarquia),
                medir("usuario.buscarPorTitulo", () -> usuarioService.buscarOpt(amostras.tituloUsuario())),
                medir("usuario.buscarPorUnidadeLotacao", () -> usuarioService.buscarPorUnidadeLotacao(amostras.codigoUnidadeLotacao())),
                medir("usuario.pesquisarPorNome", () -> usuarioService.pesquisarPorNome(amostras.termoBuscaUsuario())),
                medir("usuario.buscarAutorizacoesPerfil", () -> usuarioService.buscarAutorizacoesPerfil(amostras.tituloUsuarioComPerfil())),
                medir("responsavel.buscarAtualPorSigla", () -> responsavelUnidadeService.buscarResponsavelAtual(amostras.siglaUnidadeComResponsavel())),
                medir("responsavel.buscarEmLote", () -> responsavelUnidadeService.buscarResponsaveisUnidades(amostras.codigosUnidadesComResponsavel()))
        );

        assertThat(resultados)
                .anySatisfy(resultado -> assertThat(resultado.status()).isEqualTo(StatusMedicao.SUCESSO));

        RelatorioExecucao relatorio = new RelatorioExecucao(
                OffsetDateTime.now(),
                descricaoExecucao(),
                List.of(environment.getActiveProfiles()),
                bancoInfo,
                amostras,
                resultados
        );

        Path arquivoRelatorio = escreverRelatorio(relatorio);
        logger.info("\n{}", formatarRelatorio(relatorio, arquivoRelatorio));
    }

    private boolean estaUsandoOracle() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String produto = connection.getMetaData().getDatabaseProductName();
            return produto != null && produto.toLowerCase(Locale.ROOT).contains("oracle");
        }
    }

    private AmostrasConsulta carregarAmostras() {
        Usuario usuarioBase = usuarioRepo.findAll(PageRequest.of(0, 1)).getContent().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nenhum usuario encontrado para benchmark."));

        String tituloUsuario = usuarioBase.getTituloEleitoral();
        Long codigoUnidadeLotacao = usuarioBase.getUnidadeLotacao().getCodigo();

        String termoBusca = extrairTermoBusca(usuarioBase);

        String tituloUsuarioComPerfil = usuarioPerfilRepo.findAll(PageRequest.of(0, 1)).getContent().stream()
                .map(UsuarioPerfil::getUsuarioTitulo)
                .findFirst()
                .orElse(tituloUsuario);

        List<Responsabilidade> responsabilidades = responsabilidadeRepo.findAll(PageRequest.of(0, TAMANHO_AMOSTRA)).getContent();
        assertThat(responsabilidades)
                .as("A base Oracle precisa ter ao menos uma responsabilidade para o benchmark")
                .isNotEmpty();

        Responsabilidade responsabilidadeBase = responsabilidades.getFirst();
        String siglaUnidadeComResponsavel = unidadeRepo.findById(responsabilidadeBase.getUnidadeCodigo())
                .map(Unidade::getSigla)
                .orElseThrow(() -> new IllegalStateException("Nao foi possivel localizar unidade para benchmark."));

        List<Long> codigosUnidadesComResponsavel = responsabilidades.stream()
                .map(Responsabilidade::getUnidadeCodigo)
                .distinct()
                .toList();

        return new AmostrasConsulta(
                tituloUsuario,
                codigoUnidadeLotacao,
                termoBusca,
                tituloUsuarioComPerfil,
                siglaUnidadeComResponsavel,
                codigosUnidadesComResponsavel
        );
    }

    private BancoInfo carregarBancoInfo() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            return new BancoInfo(
                    metaData.getDatabaseProductName(),
                    metaData.getDatabaseProductVersion(),
                    metaData.getURL(),
                    metaData.getUserName()
            );
        } catch (SQLException e) {
            throw new IllegalStateException("Falha ao obter metadados do banco para benchmark.", e);
        }
    }

    private String extrairTermoBusca(Usuario usuario) {
        String nome = usuario.getNome().trim();
        if (!nome.isBlank()) {
            String primeiroToken = nome.split("\\s+")[0];
            if (primeiroToken.length() >= 3) {
                return primeiroToken.substring(0, 3);
            }
            return primeiroToken;
        }

        String matricula = usuario.getMatricula().trim();
        if (matricula.length() >= 3) {
            return matricula.substring(0, 3);
        }
        if (!matricula.isBlank()) {
            return matricula;
        }

        throw new IllegalStateException("Nao foi possivel extrair termo de busca a partir do usuario base.");
    }

    private ResultadoMedicao medir(String nome, Supplier<Object> consulta) {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);

        List<Long> temposMs = new ArrayList<>();
        List<Long> queries = new ArrayList<>();
        long tamanhoResultado = -1;

        try {
            for (int indice = 0; indice < AQUECIMENTOS + REPETICOES_MEDIDAS; indice++) {
                entityManager.clear();
                statistics.clear();

                StopWatch stopWatch = new StopWatch(nome);
                stopWatch.start();
                Object resultado = consulta.get();
                stopWatch.stop();

                validarResultado(nome, resultado);
                long duracaoMs = stopWatch.getTotalTimeMillis();
                long totalQueries = statistics.getPrepareStatementCount();
                tamanhoResultado = estimarTamanho(resultado);

                if (indice >= AQUECIMENTOS) {
                    temposMs.add(duracaoMs);
                    queries.add(totalQueries);
                }
            }

            return new ResultadoMedicao(
                    nome,
                    StatusMedicao.SUCESSO,
                    null,
                    temposMs,
                    media(temposMs),
                    maximo(temposMs),
                    media(queries),
                    tamanhoResultado
            );
        } catch (RuntimeException e) {
            logger.warn("Falha ao medir passo {}: {}", nome, e.getMessage());
            return new ResultadoMedicao(
                    nome,
                    StatusMedicao.FALHA,
                    resumirErro(e),
                    List.of(),
                    -1D,
                    -1L,
                    -1D,
                    -1L
            );
        }
    }

    private void validarResultado(String nome, Object resultado) {
        assertThat(resultado)
                .as("A consulta %s nao deveria retornar nulo", nome)
                .isNotNull();

        switch (resultado) {
            case Optional<?> optional -> assertThat(optional)
                    .as("A consulta %s deveria retornar Optional preenchido", nome)
                    .isPresent();
            case Collection<?> collection -> assertThat(collection)
                    .as("A consulta %s deveria retornar colecao nao vazia", nome)
                    .isNotEmpty();
            case Map<?, ?> map -> assertThat(map)
                    .as("A consulta %s deveria retornar mapa nao vazio", nome)
                    .isNotEmpty();
            default -> {
            }
        }
    }

    private long estimarTamanho(Object resultado) {
        if (resultado instanceof Optional<?> optional) {
            return optional.isPresent() ? 1 : 0;
        }
        if (resultado instanceof Collection<?> collection) {
            return collection.size();
        }
        if (resultado instanceof Map<?, ?> map) {
            return map.size();
        }
        return 1;
    }

    private double media(List<Long> valores) {
        return valores.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0D);
    }

    private long maximo(List<Long> valores) {
        return valores.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);
    }

    private String formatarRelatorio(RelatorioExecucao relatorio, Path arquivoRelatorio) {
        StringBuilder builder = new StringBuilder();
        builder.append("Benchmark simples das views organizacionais").append(System.lineSeparator());
        builder.append("Arquivo: ").append(arquivoRelatorio)
                .append(System.lineSeparator());
        builder.append("Descricao: ").append(relatorio.descricaoExecucao())
                .append(System.lineSeparator());
        builder.append("Perfis: ").append(String.join(",", relatorio.perfisAtivos()))
                .append(System.lineSeparator());
        builder.append("Banco: ").append(relatorio.banco().produto())
                .append(" | usuario=").append(relatorio.banco().usuario())
                .append(System.lineSeparator());
        builder.append("Aquecimentos: ").append(AQUECIMENTOS)
                .append(" | Repeticoes medidas: ").append(REPETICOES_MEDIDAS)
                .append(System.lineSeparator());
        builder.append("Amostras: tituloUsuario=").append(relatorio.amostras().tituloUsuario())
                .append(", unidadeLotacao=").append(relatorio.amostras().codigoUnidadeLotacao())
                .append(", unidadeComResponsavel=").append(relatorio.amostras().siglaUnidadeComResponsavel())
                .append(", loteResponsabilidades=").append(relatorio.amostras().codigosUnidadesComResponsavel().size())
                .append(System.lineSeparator());

        for (ResultadoMedicao resultado : relatorio.resultados()) {
            builder.append("- ").append(resultado.nome())
                    .append(" | status=").append(resultado.status());

            if (resultado.status() == StatusMedicao.SUCESSO) {
                builder.append(" | tempoMedioMs=").append(String.format(Locale.ROOT, "%.2f", resultado.tempoMedioMs()))
                        .append(" | tempoMaximoMs=").append(resultado.tempoMaximoMs())
                        .append(" | queriesMedias=").append(String.format(Locale.ROOT, "%.2f", resultado.queriesMedias()))
                        .append(" | tamanhoResultado=").append(resultado.tamanhoResultado());
            } else {
                builder.append(" | erro=").append(resultado.erro());
            }

            builder
                    .append(System.lineSeparator());
        }

        return builder.toString();
    }

    private String resumirErro(RuntimeException e) {
        Throwable causaRaiz = e;
        while (causaRaiz.getCause() != null) {
            causaRaiz = causaRaiz.getCause();
        }
        return causaRaiz.getClass().getSimpleName() + ": " + Optional.ofNullable(causaRaiz.getMessage()).orElse("sem mensagem");
    }

    private Path escreverRelatorio(RelatorioExecucao relatorio) {
        try {
            Files.createDirectories(DIRETORIO_RELATORIOS);
            String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
            String descricao = sanitizarParaArquivo(relatorio.descricaoExecucao());
            Path arquivo = DIRETORIO_RELATORIOS.resolve(timestamp + "-" + descricao + ".json");

            ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(arquivo.toFile(), relatorio);
            return arquivo;
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao escrever relatorio de benchmark.", e);
        }
    }

    private String descricaoExecucao() {
        String descricao = System.getProperty("sgc.perf.descricao");
        if (descricao == null || descricao.isBlank()) {
            descricao = System.getenv("SGC_PERF_DESCRICAO");
        }
        if (descricao == null || descricao.isBlank()) {
            return "sem-descricao";
        }
        return descricao.trim();
    }

    private String sanitizarParaArquivo(String valor) {
        String normalizado = valor.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");
        return normalizado.isBlank() ? "sem-descricao" : normalizado;
    }

    private record AmostrasConsulta(
            String tituloUsuario,
            Long codigoUnidadeLotacao,
            String termoBuscaUsuario,
            String tituloUsuarioComPerfil,
            String siglaUnidadeComResponsavel,
            List<Long> codigosUnidadesComResponsavel
    ) {
    }

    private record ResultadoMedicao(
            String nome,
            StatusMedicao status,
            String erro,
            List<Long> temposMs,
            double tempoMedioMs,
            long tempoMaximoMs,
            double queriesMedias,
            long tamanhoResultado
    ) {
    }

    private enum StatusMedicao {
        SUCESSO,
        FALHA
    }

    private record BancoInfo(
            String produto,
            String versao,
            String url,
            String usuario
    ) {
    }

    private record RelatorioExecucao(
            OffsetDateTime dataHoraExecucao,
            String descricaoExecucao,
            List<String> perfisAtivos,
            BancoInfo banco,
            AmostrasConsulta amostras,
            List<ResultadoMedicao> resultados
    ) {
    }

}
