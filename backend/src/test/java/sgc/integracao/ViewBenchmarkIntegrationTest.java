package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.test.context.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@Tag("benchmark")
@DisplayName("Benchmark de Performance: Views Originais vs Otimizadas")
class ViewBenchmarkIntegrationTest extends BaseIntegrationTest {

    private JdbcTemplate oracleJdbcTemplate;

    @BeforeEach
    void setupOracleConnection() {
        // Conexão direta via .env.hom conforme sugerido pelo usuário
        org.springframework.jdbc.datasource.DriverManagerDataSource dataSource = new org.springframework.jdbc.datasource.DriverManagerDataSource();
        dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        dataSource.setUrl("jdbc:oracle:thin:@desenvolvimentobd:1521:admdes2");
        dataSource.setUsername("sgc");
        dataSource.setPassword("dzX7_gpL4AcRV2iAxwVNynFLEiTEnh");
        
        this.oracleJdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final int WARMUP_ITERATIONS = 3;
    private static final int MEASURE_ITERATIONS = 20;

    @Test
    @DisplayName("Benchmark: VW_VINCULACAO_UNIDADE")
    void benchmarkVinculacaoUnidade() {
        validarConsistenciaDados("VW_VINCULACAO_UNIDADE", "VW_VINCULACAO_UNIDADE_2");
        executarBenchmark(
                "VW_VINCULACAO_UNIDADE",
                "SELECT * FROM VW_VINCULACAO_UNIDADE",
                "SELECT * FROM VW_VINCULACAO_UNIDADE_2"
        );
    }

    @Test
    @DisplayName("Benchmark: VW_ZONA_RESP_CENTRAL")
    void benchmarkZonaRespCentral() {
        validarConsistenciaDados("VW_ZONA_RESP_CENTRAL", "VW_ZONA_RESP_CENTRAL_2");
        executarBenchmark(
                "VW_ZONA_RESP_CENTRAL",
                "SELECT * FROM VW_ZONA_RESP_CENTRAL",
                "SELECT * FROM VW_ZONA_RESP_CENTRAL_2"
        );
    }

    @Test
    @DisplayName("Benchmark: VW_UNIDADE")
    void benchmarkUnidade() {
        validarConsistenciaDados("VW_UNIDADE", "VW_UNIDADE_2");
        executarBenchmark(
                "VW_UNIDADE",
                "SELECT * FROM VW_UNIDADE",
                "SELECT * FROM VW_UNIDADE_2"
        );
    }

    @Test
    @DisplayName("Benchmark: VW_USUARIO")
    void benchmarkUsuario() {
        validarConsistenciaDados("VW_USUARIO", "VW_USUARIO_2");
        executarBenchmark(
                "VW_USUARIO",
                "SELECT * FROM VW_USUARIO",
                "SELECT * FROM VW_USUARIO_2"
        );
    }

    @Test
    @DisplayName("Benchmark: VW_RESPONSABILIDADE")
    void benchmarkResponsabilidade() {
        validarConsistenciaDados("VW_RESPONSABILIDADE", "VW_RESPONSABILIDADE_2");
        executarBenchmark(
                "VW_RESPONSABILIDADE",
                "SELECT * FROM VW_RESPONSABILIDADE",
                "SELECT * FROM VW_RESPONSABILIDADE_2"
        );
    }

    @Test
    @DisplayName("Benchmark: VW_USUARIO_PERFIL_UNIDADE")
    void benchmarkUsuarioPerfilUnidade() {
        validarConsistenciaDados("VW_USUARIO_PERFIL_UNIDADE", "VW_USUARIO_PERFIL_UNIDADE_2");
        executarBenchmark(
                "VW_USUARIO_PERFIL_UNIDADE",
                "SELECT * FROM VW_USUARIO_PERFIL_UNIDADE",
                "SELECT * FROM VW_USUARIO_PERFIL_UNIDADE_2"
        );
    }

    private void validarConsistenciaDados(String viewOriginal, String viewOtimizada) {
        System.out.println("SANITY CHECK: Comparando dados de " + viewOriginal + " e " + viewOtimizada + "...");
        
        List<Map<String, Object>> dadosOriginal = oracleJdbcTemplate.queryForList("SELECT * FROM " + viewOriginal);
        List<Map<String, Object>> dadosOtimizado = oracleJdbcTemplate.queryForList("SELECT * FROM " + viewOtimizada);

        assertThat(dadosOtimizado.size())
                .as("Quantidade de registros divergente para " + viewOtimizada)
                .isEqualTo(dadosOriginal.size());

        // Comparação de conteúdo (ignorando ordem caso não haja ORDER BY fixo na view)
        // Usamos containsExactlyInAnyOrderElementsOf para garantir que todos os campos e valores batem
        assertThat(dadosOtimizado)
                .as("Conteúdo dos dados diverge para " + viewOtimizada)
                .containsExactlyInAnyOrderElementsOf(dadosOriginal);
        
        System.out.println("SANITY CHECK OK: Dados idênticos.");
    }

    private void executarBenchmark(String nomeView, String sqlOriginal, String sqlOtimizado) {
        System.out.println("\n---------------------------------------------------------");
        System.out.println("BENCHMARK: " + nomeView);
        System.out.println("---------------------------------------------------------");

        // Warm-up
        System.out.print("Warm-up (5 iterações)... ");
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            oracleJdbcTemplate.queryForList(sqlOriginal);
            oracleJdbcTemplate.queryForList(sqlOtimizado);
        }
        System.out.println("OK.");

        // Medição Original
        System.out.print("Medindo Original (" + MEASURE_ITERATIONS + " iterações)... ");
        long inicioOrig = System.currentTimeMillis();
        for (int i = 0; i < MEASURE_ITERATIONS; i++) {
            oracleJdbcTemplate.queryForList(sqlOriginal);
        }
        long fimOrig = System.currentTimeMillis();
        long tempoTotalOrig = fimOrig - inicioOrig;
        double mediaOrig = (double) tempoTotalOrig / MEASURE_ITERATIONS;
        System.out.println(tempoTotalOrig + "ms");

        // Medição Otimizada
        System.out.print("Medindo Otimizada (" + MEASURE_ITERATIONS + " iterações)... ");
        long inicioOtim = System.currentTimeMillis();
        for (int i = 0; i < MEASURE_ITERATIONS; i++) {
            oracleJdbcTemplate.queryForList(sqlOtimizado);
        }
        long fimOtim = System.currentTimeMillis();
        long tempoTotalOtim = fimOtim - inicioOtim;
        double mediaOtim = (double) tempoTotalOtim / MEASURE_ITERATIONS;
        System.out.println(tempoTotalOtim + "ms");

        // Resultados
        double ganhoPercentual = 0;
        if (tempoTotalOrig > 0) {
            ganhoPercentual = ((double) (tempoTotalOrig - tempoTotalOtim) / tempoTotalOrig) * 100;
        }

        System.out.println("\nRESULTADO:");
        System.out.printf("  Média Original:  %.2f ms%n", mediaOrig);
        System.out.printf("  Média Otimizada: %.2f ms%n", mediaOtim);
        System.out.printf("  Ganho:           %.2f%%%n", ganhoPercentual);
        
        if (tempoTotalOtim < tempoTotalOrig) {
            System.out.println("  STATUS: OTIMIZAÇÃO BEM SUCEDIDA!");
        } else if (tempoTotalOtim == tempoTotalOrig) {
            System.out.println("  STATUS: SEM ALTERAÇÃO SIGNIFICATIVA.");
        } else {
            System.out.println("  STATUS: ALERTA - VERSÃO OTIMIZADA MAIS LENTA!");
        }
        System.out.println("---------------------------------------------------------");

        // Asserção
        assertThat(tempoTotalOtim).as("Tempo da versão otimizada (" + tempoTotalOtim + "ms) " +
                "não deve ser significativamente superior à original (" + tempoTotalOrig + "ms)")
                .isLessThanOrEqualTo((long)(tempoTotalOrig * 1.1));
    }
}

