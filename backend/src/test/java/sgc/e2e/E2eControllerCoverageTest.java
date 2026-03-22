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
@DisplayName("E2eController - Cobertura adicional")
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
        verify(jdbcTemplate).getDataSource();
    }

    @Test
    @DisplayName("criarProcessoMapeamento deve falhar se unidadeSigla for vazia")
    void deveFalharSeUnidadeSiglaVazia() {
        E2eController.ProcessoFixtureRequest request = new E2eController.ProcessoFixtureRequest(
                "desc", "", false, 30);
        assertThatThrownBy(() -> controller.criarProcessoMapeamento(request))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage("Unidade é obrigatória");
    }

    @Test
    @DisplayName("criarProcessoRevisaoComMapaHomologado deve falhar se unidadeSigla for vazia")
    void criarProcessoRevisaoHomologadoDeveFalharSeUnidadeSiglaVazia() {
        E2eController.ProcessoFixtureRequest request = new E2eController.ProcessoFixtureRequest(
                "desc", "", false, 30);
        assertThatThrownBy(() -> controller.criarProcessoRevisaoComMapaHomologado(request))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage("Unidade é obrigatória");
    }

    @Test
    @DisplayName("criarProcessoRevisaoComCadastroHomologado deve falhar se unidadeSigla for vazia")
    void criarProcessoRevisaoCadastroHomologadoDeveFalharSeUnidadeSiglaVazia() {
        E2eController.ProcessoFixtureRequest request = new E2eController.ProcessoFixtureRequest(
                "desc", "   ", false, 30);
        assertThatThrownBy(() -> controller.criarProcessoRevisaoComCadastroHomologado(request))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage("Unidade é obrigatória");
    }
}
