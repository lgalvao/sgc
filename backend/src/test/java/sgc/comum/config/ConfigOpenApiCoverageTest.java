package sgc.comum.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("unit")
@DisplayName("ConfigOpenApi - Cobertura Adicional")
class ConfigOpenApiCoverageTest {

    @Test
    @DisplayName("customOpenAPI deve usar valores default quando configurações forem nulas")
    void deveUsarValoresDefault() {
        // Arrange
        ConfigAplicacao config = mock(ConfigAplicacao.class);
        ConfigAplicacao.OpenApi openapi = new ConfigAplicacao.OpenApi();
        // Valores nulos por padrão
        
        when(config.getOpenapi()).thenReturn(openapi);
        
        ConfigOpenApi configOpenApi = new ConfigOpenApi(config);

        // Act
        OpenAPI result = configOpenApi.customOpenAPI();

        // Assert
        assertThat(result.getInfo().getTitle()).isEqualTo("SGC API");
        assertThat(result.getInfo().getDescription()).isEqualTo("Sistema de Gestão de Competências");
        assertThat(result.getInfo().getVersion()).isEqualTo("v1");
    }
}
