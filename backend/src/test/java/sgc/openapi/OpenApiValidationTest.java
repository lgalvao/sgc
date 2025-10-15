package sgc.openapi;

import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import sgc.integracao.mocks.TestSecurityConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Validação da Especificação OpenAPI")
@Import(TestSecurityConfig.class)
@WithMockUser
class OpenApiValidationTest {

    private static final String OpenAPI_URL = "/api-docs";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve gerar uma especificação OpenAPI V3 válida e sem erros")
    void deveGerarEspecificacaoOpenApiValida() throws Exception {
        // Arrange
        MvcResult mvcResult = mockMvc.perform(get(OpenAPI_URL)).andDo(print()).andReturn();
        String openApiSpec = mvcResult.getResponse().getContentAsString();
        int status = mvcResult.getResponse().getStatus();

        if (status != 200) {
            System.err.println("Falha ao buscar especificação OpenAPI. Status: " + status);
            System.err.println("Corpo da Resposta: " + openApiSpec);
        }

        assertThat(status).as("O status da requisição para a especificação OpenAPI deve ser 200 OK").isEqualTo(200);

        // Act
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        SwaggerParseResult parseResult = new OpenAPIV3Parser().readContents(openApiSpec, null, options);

        // Assert
        assertThat(parseResult.getMessages())
            .as("A especificação OpenAPI não deve conter erros de parsing")
            .isEmpty();

        assertThat(parseResult.getOpenAPI())
            .as("A especificação OpenAPI não deve ser nula")
            .isNotNull();

        assertThat(parseResult.getOpenAPI().getPaths())
            .as("A especificação OpenAPI deve conter paths definidos")
            .isNotEmpty();
    }
}