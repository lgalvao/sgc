package sgc.e2e;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.jdbc.core.*;
import sgc.comum.erros.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("E2eController - Cobertura Adicional")
class E2eControllerCoverageTest {
    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private E2eController controller;

    @Test
    @DisplayName("resetDatabase deve retornar imediatamente se dataSource for nulo")
    void deveRetornarSeDataSourceForNulo() {

        when(jdbcTemplate.getDataSource()).thenReturn(null);


        controller.resetDatabase();

        // Assert - Não deve lançar exceção nem fazer nada (cobertura da linha 62)
        verify(jdbcTemplate).getDataSource();
    }

    @Test
    @DisplayName("criarProcessoMapeamento deve falhar se unidadeSigla for vazia")
    void deveFalharSeUnidadeSiglaVazia() {

        E2eController.ProcessoFixtureRequest request = new E2eController.ProcessoFixtureRequest(
                "desc", "", false, 30);

        // Act & Assert (cobertura das linhas 206 e 207)
        assertThatThrownBy(() -> controller.criarProcessoMapeamento(request))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage("Unidade é obrigatória");
    }
}
