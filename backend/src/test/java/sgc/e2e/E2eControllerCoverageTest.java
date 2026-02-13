package sgc.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import sgc.comum.erros.ErroValidacao;
import sgc.organizacao.UnidadeFacade;
import sgc.processo.service.ProcessoFacade;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("E2eController - Cobertura Adicional")
class E2eControllerCoverageTest {

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private NamedParameterJdbcTemplate namedJdbcTemplate;
    @Mock
    private ProcessoFacade processoFacade;
    @Mock
    private UnidadeFacade unidadeFacade;
    @Mock
    private ResourceLoader resourceLoader;
    @Mock
    private DataSource dataSource;

    @InjectMocks
    private E2eController controller;

    @Test
    @DisplayName("resetDatabase deve retornar imediatamente se dataSource for nulo")
    void deveRetornarSeDataSourceForNulo() {
        // Arrange
        when(jdbcTemplate.getDataSource()).thenReturn(null);

        // Act
        controller.resetDatabase();

        // Assert - Não deve lançar exceção nem fazer nada (cobertura da linha 62)
        verify(jdbcTemplate).getDataSource();
    }

    @Test
    @DisplayName("criarProcessoMapeamento deve falhar se unidadeSigla for vazia")
    void deveFalharSeUnidadeSiglaVazia() {
        // Arrange
        E2eController.ProcessoFixtureRequest request = new E2eController.ProcessoFixtureRequest(
                "desc", "", false, 30);

        // Act & Assert (cobertura das linhas 206 e 207)
        assertThatThrownBy(() -> controller.criarProcessoMapeamento(request))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage("Unidade é obrigatória");
    }
}
