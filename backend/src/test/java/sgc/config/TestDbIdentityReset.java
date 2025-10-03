package sgc.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Test helper that resets identity columns to a high value in the "test" profile.
 * This avoids primary key collisions between SQL fixtures that insert explicit ids
 * and Hibernate-generated ids during tests.
 * <p>
 * Implemented without Lombok so it compiles on test classpath.
 */
@Component
@Profile("test")
public class TestDbIdentityReset implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    public TestDbIdentityReset(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        String[] tables = {
                "PROCESSO", "UNIDADE", "MAPA", "ATIVIDADE",
                "COMPETENCIA", "CONHECIMENTO", "SUBPROCESSO",
                "UNIDADE_PROCESSO", "MOVIMENTACAO", "ALERTA"
        };
        for (String table : tables) {
        try {
            // H2 accepts ALTER TABLE ... ALTER COLUMN ... RESTART WITH n for identity columns
            jdbcTemplate.execute("ALTER TABLE " + table + " ALTER COLUMN codigo RESTART WITH 20000");
        } catch (Exception ignored) {
            // ignore tables/columns that don't exist in a given schema
        }
        }
    }
}