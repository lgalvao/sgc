package sgc.integracao;

import jakarta.persistence.*;
import org.hibernate.*;
import org.hibernate.stat.*;
import org.springframework.util.*;
import sgc.integracao.mocks.*;

import java.util.*;
import java.util.function.*;

import static org.assertj.core.api.Assertions.*;

public class MetricasExecucaoTeste {
    public static final String PROPRIEDADE_ORCAMENTO_TEMPO_ESTRITO = "sgc.performance.strict-time-budgets";
    private final EntityManager entityManager;
    private final EntityManagerFactory entityManagerFactory;

    public MetricasExecucaoTeste(EntityManager entityManager, EntityManagerFactory entityManagerFactory) {
        this.entityManager = entityManager;
        this.entityManagerFactory = entityManagerFactory;
    }

    public <T> ResultadoMedicao medir(String nome, Supplier<T> acao, String... trechosSql) {
        Statistics estatisticas = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        estatisticas.setStatisticsEnabled(true);

        entityManager.clear();
        estatisticas.clear();
        ColetorSqlTeste.limpar();

        StopWatch cronometro = new StopWatch(nome);
        cronometro.start();
        T resultado = acao.get();
        cronometro.stop();

        Map<String, Long> contagensPorTrecho = new LinkedHashMap<>();
        for (String trechoSql : trechosSql) {
            contagensPorTrecho.put(trechoSql, ColetorSqlTeste.contarSqlsContendo(trechoSql));
        }

        return new ResultadoMedicao(
                nome,
                resultado,
                cronometro.getTotalTimeMillis(),
                ColetorSqlTeste.contarSqls(),
                ColetorSqlTeste.contarSqlsViewsOrganizacionais(),
                estatisticas.getPrepareStatementCount(),
                contagensPorTrecho
        );
    }

    public record ResultadoMedicao(
            String nome,
            Object resultado,
            long duracaoMs,
            long totalSqls,
            long sqlsViewsOrganizacionais,
            long preparedStatements,
            Map<String, Long> contagensPorTrecho
    ) {
        public void validarTempoSeEstrito(long limiteMs) {
            if (Boolean.getBoolean(PROPRIEDADE_ORCAMENTO_TEMPO_ESTRITO)) {
                assertThat(duracaoMs)
                        .as("Tempo da medicao %s deveria ficar abaixo de %d ms".formatted(nome, limiteMs))
                        .isLessThanOrEqualTo(limiteMs);
            }
        }

        public String resumo() {
            return "%s: %d ms | totalSqls=%d | views=%d | prepared=%d | trechos=%s".formatted(
                    nome,
                    duracaoMs,
                    totalSqls,
                    sqlsViewsOrganizacionais,
                    preparedStatements,
                    contagensPorTrecho
            );
        }
    }
}
