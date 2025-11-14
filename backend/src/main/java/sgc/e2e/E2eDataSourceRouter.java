package sgc.e2e;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import javax.sql.DataSource;

public class E2eDataSourceRouter extends AbstractRoutingDataSource {

    private final E2eTestDatabaseService e2eTestDatabaseService;
    private DataSource defaultDataSource; // To hold the default data source

    public E2eDataSourceRouter(E2eTestDatabaseService e2eTestDatabaseService) {
        this.e2eTestDatabaseService = e2eTestDatabaseService;
    }

    // Setter for the default data source
    public void setDefaultDataSource(DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }

    @Override
    protected DataSource determineTargetDataSource() {
        String testId = E2eTestContext.getCurrentTestId();
        if (testId != null) {
            return e2eTestDatabaseService.getOrCreateDataSource(testId);
        }
        // If no testId, return the default data source
        return defaultDataSource;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return null;
    }
}
