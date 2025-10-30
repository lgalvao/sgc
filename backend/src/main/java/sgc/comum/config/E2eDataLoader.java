package sgc.comum.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

/**
 * Carrega dados de teste para o perfil e2e.
 * Esta classe executa o data.sql após a inicialização do Hibernate.
 */
@Configuration
@Profile("disabled-e2e-dataloader")
public class E2eDataLoader {

    @Bean
    CommandLineRunner loadTestData(DataSource dataSource) {
        return args -> {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("data.sql"));
            populator.setSeparator(";");
            populator.execute(dataSource);
            System.out.println("✅ Data.sql loaded successfully for e2e profile");
        };
    }
}
