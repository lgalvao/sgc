package sgc.e2e;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/e2e")
@Profile("e2e")
@RequiredArgsConstructor
public class E2eController {
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @PostMapping("/reset-database")
    public void resetDatabase() throws SQLException {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");

        try {
            List<String> tables = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'SGC'", 
                String.class
            );

            for (String table : tables) {
                jdbcTemplate.execute("TRUNCATE TABLE sgc." + table);
            }
        } finally {
            jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }

        File seedFile = new File("../e2e/setup/seed.sql");
        if (!seedFile.exists()) seedFile = new File("e2e/setup/seed.sql");
        if (!seedFile.exists()) throw new RuntimeException("Arquivo seed.sql não encontrado");

        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new FileSystemResource(seedFile));
        }
    }

    @PostMapping("/processo/{codigo}/limpar") 
    @Transactional
    public void limparProcessoComDependentes(@PathVariable Long codigo) {
        String sqlMapas = "SELECT mapa_codigo FROM sgc.subprocesso WHERE processo_codigo = ? AND mapa_codigo IS NOT NULL";
        List<Long> mapaIds = jdbcTemplate.queryForList(sqlMapas, Long.class, codigo);

        jdbcTemplate.update("DELETE FROM sgc.analise WHERE subprocesso_codigo IN (SELECT codigo FROM sgc.subprocesso WHERE processo_codigo = ?)", codigo);
        jdbcTemplate.update("DELETE FROM sgc.notificacao WHERE subprocesso_codigo IN (SELECT codigo FROM sgc.subprocesso WHERE processo_codigo = ?)", codigo);
        jdbcTemplate.update("DELETE FROM sgc.movimentacao WHERE subprocesso_codigo IN (SELECT codigo FROM sgc.subprocesso WHERE processo_codigo = ?)", codigo);
        jdbcTemplate.update("DELETE FROM sgc.subprocesso WHERE processo_codigo = ?", codigo);

        if (!mapaIds.isEmpty()) {
            String ids = mapaIds.toString().replace("[", "").replace("]", "");
            jdbcTemplate.update("DELETE FROM sgc.conhecimento WHERE atividade_codigo IN (SELECT codigo FROM sgc.atividade WHERE mapa_codigo IN (" + ids + "))");
            jdbcTemplate.update("DELETE FROM sgc.competencia_atividade WHERE atividade_codigo IN (SELECT codigo FROM sgc.atividade WHERE mapa_codigo IN (" + ids + "))");
            jdbcTemplate.update("DELETE FROM sgc.competencia_atividade WHERE competencia_codigo IN (SELECT codigo FROM sgc.competencia WHERE mapa_codigo IN (" + ids + "))");
            jdbcTemplate.update("DELETE FROM sgc.atividade WHERE mapa_codigo IN (" + ids + ")");
            jdbcTemplate.update("DELETE FROM sgc.competencia WHERE mapa_codigo IN (" + ids + ")");
            jdbcTemplate.update("DELETE FROM sgc.mapa WHERE codigo IN (" + ids + ")");
        }

        jdbcTemplate.update("DELETE FROM sgc.alerta_usuario WHERE alerta_codigo IN (SELECT codigo FROM sgc.alerta WHERE processo_codigo = ?)", codigo);
        jdbcTemplate.update("DELETE FROM sgc.alerta WHERE processo_codigo = ?", codigo);
        jdbcTemplate.update("DELETE FROM sgc.unidade_processo WHERE processo_codigo = ?", codigo);
        jdbcTemplate.update("DELETE FROM sgc.processo WHERE codigo = ?", codigo);
    }
}
