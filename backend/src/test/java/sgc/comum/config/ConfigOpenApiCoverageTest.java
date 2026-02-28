package sgc.comum.config;

import io.swagger.v3.oas.models.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ConfigOpenApi - Cobertura Adicional")
class ConfigOpenApiCoverageTest {

    @Test
    @DisplayName("customOpenAPI deve usar valores default quando configurações forem nulas")
    void deveUsarValoresDefault() {
        // Arrange
        ConfigAplicacao config = mock(ConfigAplicacao.class);
        ConfigAplicacao.OpenApi openapi = new ConfigAplicacao.OpenApi();

        when(config.getOpenapi()).thenReturn(openapi);

        ConfigOpenApi configOpenApi = new ConfigOpenApi(config);

        // Act
        OpenAPI result = configOpenApi.customOpenAPI();

        // Assert
        assertThat(result.getInfo().getTitle()).isEqualTo("SGC API");
        assertThat(result.getInfo().getDescription()).isEqualTo("Sistema de Gestão de Competências");
        assertThat(result.getInfo().getVersion()).isEqualTo("v1");
    }

    @Test
    @DisplayName("customOpenAPI deve usar valores configurados quando não forem nulos")
    void deveUsarValoresConfigurados() {
        // Arrange
        ConfigAplicacao config = mock(ConfigAplicacao.class);
        ConfigAplicacao.OpenApi openapi = new ConfigAplicacao.OpenApi();
        openapi.setTitle("Meu Titulo");
        openapi.setDescription("Minha Descricao");
        openapi.setVersion("v2");

        when(config.getOpenapi()).thenReturn(openapi);

        ConfigOpenApi configOpenApi = new ConfigOpenApi(config);

        // Act
        OpenAPI result = configOpenApi.customOpenAPI();

        // Assert
        assertThat(result.getInfo().getTitle()).isEqualTo("Meu Titulo");
        assertThat(result.getInfo().getDescription()).isEqualTo("Minha Descricao");
        assertThat(result.getInfo().getVersion()).isEqualTo("v2");
    }
}
