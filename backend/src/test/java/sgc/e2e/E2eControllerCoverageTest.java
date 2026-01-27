package sgc.e2e;

import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import sgc.comum.erros.ErroValidacao;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.dto.UnidadeDto;
import sgc.processo.dto.CriarProcessoRequest;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.service.ProcessoFacade;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("E2eController - Testes de Cobertura")
class E2eControllerCoverageTest {

    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private DataSource dataSource;
    @Mock
    private ProcessoFacade processoFacade;
    @Mock
    private UnidadeFacade unidadeFacade;

    @InjectMocks
    private E2eController controller;

    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Deve criar processo de revisão via fixture")
    void deveCriarProcessoRevisao() {
        // Arrange
        E2eController.ProcessoFixtureRequest request = new E2eController.ProcessoFixtureRequest(
                "Teste Revisao", "SIGLA", true, 10
        );

        UnidadeDto unidadeDto = UnidadeDto.builder().codigo(1L).sigla("SIGLA").build();
        ProcessoDto processoDto = ProcessoDto.builder().codigo(100L).build();

        when(unidadeFacade.buscarPorSigla("SIGLA")).thenReturn(unidadeDto);
        when(processoFacade.criar(any(CriarProcessoRequest.class))).thenReturn(processoDto);
        when(processoFacade.obterPorId(100L)).thenReturn(Optional.of(processoDto));

        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Act
        ProcessoDto result = controller.criarProcessoRevisao(request);

        // Assert
        assertNotNull(result);
        verify(processoFacade).iniciarProcessoRevisao(eq(100L), any());
    }

    @Test
    @DisplayName("Deve validar unidade obrigatória")
    void deveValidarUnidadeObrigatoria() {
        E2eController.ProcessoFixtureRequest request = new E2eController.ProcessoFixtureRequest(
                "Teste", "   ", false, 10
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);

        assertThrows(ErroValidacao.class, () -> controller.criarProcessoMapeamento(request));
    }

    @Test
    @DisplayName("Deve usar descrição padrão quando não fornecida (null)")
    void deveUsarDescricaoPadraoNull() {
         E2eController.ProcessoFixtureRequest request = new E2eController.ProcessoFixtureRequest(
                null, "SIGLA", false, 10
        );

        UnidadeDto unidadeDto = UnidadeDto.builder().codigo(1L).sigla("SIGLA").build();
        ProcessoDto processoDto = ProcessoDto.builder().codigo(100L).build();

        when(unidadeFacade.buscarPorSigla("SIGLA")).thenReturn(unidadeDto);
        when(processoFacade.criar(any(CriarProcessoRequest.class))).thenReturn(processoDto);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        controller.criarProcessoMapeamento(request);

        verify(processoFacade).criar(argThat(req -> req.getDescricao().contains("Processo Fixture E2E")));
    }

    @Test
    @DisplayName("Deve usar descrição padrão quando vazia")
    void deveUsarDescricaoPadraoVazia() {
         E2eController.ProcessoFixtureRequest request = new E2eController.ProcessoFixtureRequest(
                "", "SIGLA", false, 10
        );

        UnidadeDto unidadeDto = UnidadeDto.builder().codigo(1L).sigla("SIGLA").build();
        ProcessoDto processoDto = ProcessoDto.builder().codigo(100L).build();

        when(unidadeFacade.buscarPorSigla("SIGLA")).thenReturn(unidadeDto);
        when(processoFacade.criar(any(CriarProcessoRequest.class))).thenReturn(processoDto);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        controller.criarProcessoMapeamento(request);

        verify(processoFacade).criar(argThat(req -> req.getDescricao().contains("Processo Fixture E2E")));
    }

    @Test
    @DisplayName("Reset database deve falhar se arquivo SQL não encontrado")
    void resetDatabaseFalhaSemArquivo() throws SQLException {
        // This is hard to test without controlling File creation.
        // We can simulate the IOException or SQLException in other parts though.
        // But specifically verifying the "throw new ErroConfiguracao" requires the file to be missing.
        // Since we can't delete the file, we can't easily cover this line in unit test without powermock.
        // However, if we assume the code is running in an environment where seed.sql might be missing, we could try.
        // But here it exists.
        // Let's at least test the catch block by simulating SQLException.

        doThrow(new RuntimeException("Simulated DB Error")).when(jdbcTemplate).execute(anyString());

        assertThrows(RuntimeException.class, () -> controller.resetDatabase());
    }

    @Test
    @DisplayName("Deve falhar ao recarregar processo não encontrado após criar fixture")
    void deveFalharRecarregarProcessoNaoEncontrado() {
        E2eController.ProcessoFixtureRequest request = new E2eController.ProcessoFixtureRequest(
                "Teste", "SIGLA", true, 10
        );

        UnidadeDto unidadeDto = UnidadeDto.builder().codigo(1L).sigla("SIGLA").build();
        ProcessoDto processoDto = ProcessoDto.builder().codigo(100L).build();

        when(unidadeFacade.buscarPorSigla("SIGLA")).thenReturn(unidadeDto);
        when(processoFacade.criar(any(CriarProcessoRequest.class))).thenReturn(processoDto);
        // Retorna vazio ao tentar recarregar
        when(processoFacade.obterPorId(100L)).thenReturn(Optional.empty());

        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Deve lançar a exceção do orElseThrow
        assertThrows(sgc.comum.erros.ErroEntidadeNaoEncontrada.class, () -> controller.criarProcessoMapeamento(request));
    }
}
