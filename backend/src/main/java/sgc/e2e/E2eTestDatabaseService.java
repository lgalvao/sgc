package sgc.e2e;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
@Service
@RequiredArgsConstructor
public class E2eTestDatabaseService {
    public void executeSqlScripts(Connection connection, Resource... scripts) {
        for (Resource script : scripts) ScriptUtils.executeSqlScript(connection, script);
    }

    public void setReferentialIntegrity(Connection conn, boolean enable) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET REFERENTIAL_INTEGRITY " + (enable ? "TRUE" : "FALSE"));
        }
    }

    public void reloadDatabaseScripts(Connection conn, ResourceLoader resourceLoader) throws Exception {
        setReferentialIntegrity(conn, false);
        Resource dataResource = resourceLoader.getResource("classpath:" + "/data-minimal.sql");
        executeSqlScripts(conn, dataResource);
        setReferentialIntegrity(conn, true);
    }
}
