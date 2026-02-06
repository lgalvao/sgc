package sgc.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.dto.UnidadeDto;
import sgc.processo.dto.ProcessoDto;

import sgc.processo.service.ProcessoFacade;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import java.io.ByteArrayInputStream;
import javax.sql.DataSource;
import org.springframework.core.io.Resource;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cobertura Extra: E2eController")
class E2eControllerCoverageTest {

    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private NamedParameterJdbcTemplate namedJdbcTemplate;
    @Mock private ProcessoFacade processoFacade;
    @Mock private UnidadeFacade unidadeFacade;
    @Mock private ResourceLoader resourceLoader;

    @InjectMocks
    private E2eController controller;

    @Test
    @DisplayName("limparTabela: Deve tentar DELETE se TRUNCATE falhar")
    void limparTabela_TruncateFalha_TentaDelete() throws Exception {
        // ... (rest of the method remains same, but easier to just replace header section up to InjectMocks)
        Connection conn = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        
        lenient().when(stmt.execute(anyString())).thenReturn(true);
        doThrow(new SQLException("Erro H2")).when(stmt).execute(argThat(s -> s != null && s.contains("TRUNCATE")));

        DataSource ds = mock(DataSource.class);
        when(jdbcTemplate.getDataSource()).thenAnswer(i -> ds);
        when(ds.getConnection()).thenReturn(conn);
        when(conn.createStatement()).thenReturn(stmt);
        when(jdbcTemplate.queryForList(anyString(), eq(String.class))).thenReturn(List.of("TABELA_TESTE"));
        
        Resource resource = mock(Resource.class);
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("SELECT 1;".getBytes()));

        controller.resetDatabase();

        verify(stmt).execute(argThat(s -> s != null && s.contains("TRUNCATE")));
        verify(stmt).execute(contains("DELETE FROM sgc.TABELA_TESTE"));
    }

    @Test
    @DisplayName("criarProcessoFixture: Unidade não encontrada")
    void criarProcessoFixture_UnidadeNaoEncontrada() {
        var req = new E2eController.ProcessoFixtureRequest("Desc", "SIGLA", false, 30);
        when(unidadeFacade.buscarPorSigla("SIGLA")).thenThrow(new ErroEntidadeNaoEncontrada("Unidade", "SIGLA"));

        assertThatThrownBy(() -> controller.criarProcessoMapeamento(req))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }
    
    @Test
    @DisplayName("criarProcessoFixture: Falha ao iniciar processo devolve ErroValidacao")
    void criarProcessoFixture_FalhaIniciar() {
        var req = new E2eController.ProcessoFixtureRequest("Desc", "SIGLA", true, 30);
        
        UnidadeDto unidade = UnidadeDto.builder().codigo(10L).build();
        when(unidadeFacade.buscarPorSigla("SIGLA")).thenReturn(unidade);
        
        ProcessoDto dto = ProcessoDto.builder().codigo(100L).build();
        when(processoFacade.criar(any())).thenReturn(dto);
        
        when(processoFacade.iniciarProcessoMapeamento(100L, List.of(10L)))
            .thenReturn(List.of("Erro 1", "Erro 2"));

        assertThatThrownBy(() -> controller.criarProcessoMapeamento(req))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("Erro 1");
    }

    @Test
    @DisplayName("criarProcessoFixture: Falha ao recarregar processo")
    void criarProcessoFixture_FalhaRecarregar() {
        var req = new E2eController.ProcessoFixtureRequest("Desc", "SIGLA", true, 30);
        
        UnidadeDto unidade = UnidadeDto.builder().codigo(10L).build();
        when(unidadeFacade.buscarPorSigla("SIGLA")).thenReturn(unidade);
        
        ProcessoDto dto = ProcessoDto.builder().codigo(100L).build();
        when(processoFacade.criar(any())).thenReturn(dto);
        
        when(processoFacade.iniciarProcessoMapeamento(100L, List.of(10L)))
            .thenReturn(List.of()); // Sucesso
            
        when(processoFacade.obterPorId(100L)).thenReturn(java.util.Optional.empty()); // Falha ao recarregar

        assertThatThrownBy(() -> controller.criarProcessoMapeamento(req))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }
    
    // ... rest of comments
}
