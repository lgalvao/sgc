package sgc.integracao.mocks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.thymeleaf.TemplateEngine;

@TestConfiguration
public class TestThymeleafConfig {
    @Bean
    public TemplateEngine templateEngine() {
        TemplateEngine mockTemplateEngine = mock(TemplateEngine.class);
        when(mockTemplateEngine.process(anyString(), any()))
                .thenReturn("Email de teste gerado pelo mock.");
        return mockTemplateEngine;
    }
}
