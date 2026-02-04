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
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoFacade;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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
        Connection conn = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        
        // Configurar comportamento padrao para chamadas execute
        // Lenient porque sera chamado varias vezes com strings diferentes
        lenient().when(stmt.execute(anyString())).thenReturn(true);
        
        // Simular falha especificamente no TRUNCATE
        // Como o stub acima cobre qualquer string, precisamos garantir que ESE stub especifico ganhe
        // Mockito avalia em ordem reversa de declaracao? Nao, 'when' ... overrides.
        // Mas para doThrow/thenThrow com matcher, eh melhor usar doAnswer ou garantir a ordem.
        // Se usarmos doThrow(...).when(stmt).execute(contains("TRUNCATE")) DEPOIS do anyString, deve funcionar.
        doThrow(new SQLException("Erro H2")).when(stmt).execute(argThat(s -> s != null && s.contains("TRUNCATE")));

        // Mockar DataSource e Connection para resetDatabase
        javax.sql.DataSource ds = mock(javax.sql.DataSource.class);
        when(jdbcTemplate.getDataSource()).thenAnswer(i -> ds);
        when(ds.getConnection()).thenReturn(conn);
        when(conn.createStatement()).thenReturn(stmt);
        
        // Mockar lista de tabelas
        when(jdbcTemplate.queryForList(anyString(), eq(String.class))).thenReturn(List.of("TABELA_TESTE"));
        
        // Mockar resourceLoader para seed
        org.springframework.core.io.Resource resource = mock(org.springframework.core.io.Resource.class);
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        // when(resource.isReadable()).thenReturn(true); // Unnecessary
        when(resource.getInputStream()).thenReturn(new java.io.ByteArrayInputStream("SELECT 1;".getBytes()));

        controller.resetDatabase();

        // Verificar se chamou TRUNCATE (que falhou) e depois DELETE
        verify(stmt).execute(argThat(s -> s != null && s.contains("TRUNCATE")));
        verify(stmt).execute(contains("DELETE FROM sgc.TABELA_TESTE"));
    }

    @Test
    @DisplayName("criarProcessoFixture: Unidade não encontrada")
    void criarProcessoFixture_UnidadeNaoEncontrada() {
        var req = new E2eController.ProcessoFixtureRequest("Desc", "SIGLA", false, 30);
        when(unidadeFacade.buscarPorSigla("SIGLA")).thenReturn(null);

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
            
        when(processoFacade.obterPorId(100L)).thenReturn(Optional.empty()); // Falha ao recarregar

        assertThatThrownBy(() -> controller.criarProcessoMapeamento(req))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }
    
    // Para testar o ramo "else" do tipo de processo (linha 237), seria necessário passar um tipo diferente de MAPEAMENTO/REVISAO para criarProcessoFixture.
    // Mas os métodos públicos (criarProcessoMapeamento, criarProcessoRevisao) fixam o tipo.
    // O método privado aceita TipoProcesso.
    // Podemos testar criando um método público "fake" via reflexão ou apenas aceitar que essa branch é inalcançável via API pública (o que é bom).
    // Mas para cobertura 100%, precisaríamos invocar o privado.
    // No entanto, vou pular esse detalhe por enquanto.
}
