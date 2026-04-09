package sgc.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import sgc.alerta.model.Alerta;
import sgc.organizacao.ContextoUsuarioAutenticado;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioRepo;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.painel.PainelFacade;
import sgc.subprocesso.dto.AnaliseHistoricoDto;
import sgc.subprocesso.dto.SubprocessoDetalheResponse;
import sgc.subprocesso.model.Analise;
import sgc.subprocesso.model.AnaliseRepo;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
@ActiveProfiles("hom")
@Tag("integration")
@DisplayName("Benchmark Oracle de Processo e Subprocesso")
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.session_factory.statement_inspector=sgc.integracao.mocks.ColetorSqlTeste",
        "spring.datasource.url=${DB_URL}",
        "spring.datasource.username=${DB_USERNAME}",
        "spring.datasource.password=${DB_PASSWORD}",
        "spring.datasource.driver-class-name=oracle.jdbc.OracleDriver",
        "spring.sql.init.mode=never"
})
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
class ProcessoSubprocessoPerformanceOracleIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(ProcessoSubprocessoPerformanceOracleIntegrationTest.class);
    private static final Path DIRETORIO_RELATORIOS = Path.of("etc", "benchmarks", "processo-subprocesso");

    @Autowired
    private DataSource dataSource;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private SubprocessoRepo subprocessoRepo;
    @Autowired
    private AnaliseRepo analiseRepo;
    @Autowired
    private PainelFacade painelFacade;
    @Autowired
    private sgc.subprocesso.service.SubprocessoConsultaService subprocessoConsultaService;
    @MockitoBean
    private JavaMailSender javaMailSender;

    @Test
    @DisplayName("Deve medir hotspots de processo e subprocesso no Oracle real")
    void deveMedirHotspotsDeProcessoESubprocessoNoOracleReal() throws Exception {
        assumeTrue(estaUsandoOracle(), "Benchmark real deve rodar somente quando a datasource atual aponta para Oracle.");

        AmostrasBenchmark amostras = carregarAmostras();
        MetricasExecucaoTeste medidor = new MetricasExecucaoTeste(entityManager, entityManagerFactory);
        Usuario usuarioAdmin = criarUsuarioAdmin(amostras.usuarioBase());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        usuarioAdmin,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

        List<MetricasExecucaoTeste.ResultadoMedicao> resultados = new ArrayList<>();
        resultados.add(medidor.medir(
                "painel.listarProcessos.gestor",
                () -> painelFacade.listarProcessos(
                        new ContextoUsuarioAutenticado(
                                amostras.usuarioBase().getTituloEleitoral(),
                                amostras.codigoUnidadeBase(),
                                amostras.perfilPainel()
                        ),
                        PageRequest.of(0, 20)),
                "VW_UNIDADE", "PROCESSO"
        ));
        resultados.add(medidor.medir(
                "painel.listarAlertas.gestor",
                () -> painelFacade.listarAlertas(
                        new ContextoUsuarioAutenticado(
                                amostras.usuarioBase().getTituloEleitoral(),
                                amostras.codigoUnidadeBase(),
                                amostras.perfilPainel()
                        ),
                        PageRequest.of(0, 20)),
                "ALERTA", "ALERTA_USUARIO"
        ));
        resultados.add(medidor.medir(
                "subprocesso.obterDetalhes",
                () -> subprocessoConsultaService.obterDetalhes(amostras.codigoSubprocessoDetalhe()),
                "SUBPROCESSO", "MOVIMENTACAO", "VW_UNIDADE"
        ));
        resultados.add(medidor.medir(
                "subprocesso.listarHistoricoValidacao",
                () -> subprocessoConsultaService.listarHistoricoValidacao(amostras.codigoSubprocessoHistorico()),
                "ANALISE", "VW_UNIDADE"
        ));

        validarResultados(resultados);
        BenchmarkRelatorio relatorio = new BenchmarkRelatorio(
                OffsetDateTime.now(ZoneOffset.UTC),
                carregarDescricaoBanco(),
                amostras,
                resultados.stream().map(ResultadoSerializado::from).toList()
        );

        Files.createDirectories(DIRETORIO_RELATORIOS);
        Path arquivo = DIRETORIO_RELATORIOS.resolve(
                "oracle-" + DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(OffsetDateTime.now()) + ".json"
        );
        ObjectMapper objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(arquivo.toFile(), relatorio);

        logger.info("\n{}", formatarRelatorio(relatorio, arquivo));
    }

    private boolean estaUsandoOracle() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String produto = connection.getMetaData().getDatabaseProductName();
            return produto != null && produto.toLowerCase(Locale.ROOT).contains("oracle");
        }
    }

    private String carregarDescricaoBanco() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return "%s | %s | %s".formatted(
                    connection.getMetaData().getDatabaseProductName(),
                    connection.getMetaData().getDatabaseProductVersion(),
                    connection.getMetaData().getURL()
            );
        }
    }

    private AmostrasBenchmark carregarAmostras() {
        Usuario usuarioBase = usuarioRepo.findAll(PageRequest.of(0, 1)).getContent().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nenhum usuario encontrado para benchmark Oracle."));

        Subprocesso subprocessoBase = subprocessoRepo.findAll(PageRequest.of(0, 20)).getContent().stream()
                .filter(subprocesso -> subprocesso.getUnidade() != null)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nenhum subprocesso encontrado para benchmark Oracle."));
        Long codigoSubprocessoDetalhe = subprocessoBase.getCodigo();

        Long codigoSubprocessoHistorico = analiseRepo.findAll(PageRequest.of(0, 1)).getContent().stream()
                .map(Analise::getSubprocesso)
                .map(Subprocesso::getCodigo)
                .findFirst()
                .orElse(codigoSubprocessoDetalhe);

        Long codigoUnidadeBase = subprocessoBase.getUnidade().getCodigo();
        Perfil perfilPainel = Perfil.GESTOR;

        return new AmostrasBenchmark(
                usuarioBase,
                codigoUnidadeBase,
                perfilPainel,
                codigoSubprocessoDetalhe,
                codigoSubprocessoHistorico
        );
    }

    private Usuario criarUsuarioAdmin(Usuario base) {
        base.setPerfilAtivo(Perfil.ADMIN);
        base.setUnidadeAtivaCodigo(base.getUnidadeLotacao().getCodigo());
        return base;
    }

    private void validarResultados(List<MetricasExecucaoTeste.ResultadoMedicao> resultados) {
        assertThat(resultados).hasSize(4);
        resultados.forEach(resultado -> {
            assertThat(resultado.resultado()).as(resultado.nome()).isNotNull();
            switch (resultado.resultado()) {
                case Page<?> pagina -> assertThat(pagina.getContent()).isNotNull();
                case List<?> lista -> assertThat(lista).isNotNull();
                case SubprocessoDetalheResponse detalhe -> assertThat(detalhe.subprocesso()).isNotNull();
                default -> {
                }
            }
        });
    }

    private String formatarRelatorio(BenchmarkRelatorio relatorio, Path arquivo) {
        StringBuilder builder = new StringBuilder();
        builder.append("Benchmark Oracle de processo/subprocesso\n")
                .append("Banco: ").append(relatorio.banco()).append('\n')
                .append("Arquivo: ").append(arquivo).append('\n');
        relatorio.resultados().forEach(resultado -> builder.append(String.format(
                Locale.ROOT,
                "- %s: %d ms | totalSqls=%d | views=%d | prepared=%d | trechos=%s%n",
                resultado.nome(),
                resultado.duracaoMs(),
                resultado.totalSqls(),
                resultado.sqlsViewsOrganizacionais(),
                resultado.preparedStatements(),
                resultado.contagensPorTrecho()
        )));
        return builder.toString();
    }

    private record AmostrasBenchmark(
            Usuario usuarioBase,
            Long codigoUnidadeBase,
            Perfil perfilPainel,
            Long codigoSubprocessoDetalhe,
            Long codigoSubprocessoHistorico
    ) {
    }

    private record ResultadoSerializado(
            String nome,
            long duracaoMs,
            long totalSqls,
            long sqlsViewsOrganizacionais,
            long preparedStatements,
            java.util.Map<String, Long> contagensPorTrecho
    ) {
        static ResultadoSerializado from(MetricasExecucaoTeste.ResultadoMedicao resultado) {
            return new ResultadoSerializado(
                    resultado.nome(),
                    resultado.duracaoMs(),
                    resultado.totalSqls(),
                    resultado.sqlsViewsOrganizacionais(),
                    resultado.preparedStatements(),
                    resultado.contagensPorTrecho()
            );
        }
    }

    private record BenchmarkRelatorio(
            OffsetDateTime executadoEm,
            String banco,
            AmostrasBenchmark amostras,
            List<ResultadoSerializado> resultados
    ) {
    }
}
