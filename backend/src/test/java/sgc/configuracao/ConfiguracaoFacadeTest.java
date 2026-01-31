package sgc.configuracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroConfiguracao;
import sgc.configuracao.dto.ParametroRequest;
import sgc.configuracao.dto.ParametroResponse;
import sgc.configuracao.mapper.ParametroMapper;
import sgc.configuracao.model.Parametro;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("ConfiguracaoFacade")
class ConfiguracaoFacadeTest {
    @InjectMocks
    private ConfiguracaoFacade configuracaoFacade;

    @Mock
    private ConfiguracaoService configuracaoService;

    @Mock
    private ParametroMapper parametroMapper;

    @Test
    @DisplayName("Deve buscar todos os parâmetros")
    void buscarTodos_sucesso() {
        // Arrange
        Parametro p1 = Parametro.builder().chave("CHAVE_1").valor("VALOR_1").descricao("Desc 1").build();
        Parametro p2 = Parametro.builder().chave("CHAVE_2").valor("VALOR_2").descricao("Desc 2").build();
        when(configuracaoService.buscarTodos()).thenReturn(List.of(p1, p2));
        when(parametroMapper.toResponse(any())).thenReturn(mock(ParametroResponse.class));

        // Act
        List<ParametroResponse> resultado = configuracaoFacade.buscarTodos();

        // Assert
        assertThat(resultado).hasSize(2);
        verify(configuracaoService).buscarTodos();
    }

    @Test
    @DisplayName("Deve buscar parâmetro por chave com sucesso")
    void buscarPorChave_sucesso() {
        // Arrange
        String chave = "TESTE_CHAVE";
        Parametro parametro = Parametro.builder().chave(chave).valor("VALOR").descricao("Desc").build();
        when(configuracaoService.buscarPorChave(chave)).thenReturn(parametro);

        // Act
        Parametro resultado = configuracaoFacade.buscarPorChave(chave);

        // Assert
        assertThat(resultado).isEqualTo(parametro);
        verify(configuracaoService).buscarPorChave(chave);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar chave inexistente")
    void buscarPorChave_naoEncontrado() {
        // Arrange
        String chave = "INEXISTENTE";
        when(configuracaoService.buscarPorChave(chave)).thenThrow(new ErroConfiguracao(
                "Parâmetro '%s' não encontrado. Configure o parâmetro no banco de dados.".formatted(chave)));

        // Act & Assert
        assertThatThrownBy(() -> configuracaoFacade.buscarPorChave(chave))
                .isInstanceOf(ErroConfiguracao.class)
                .hasMessageContaining(chave);
    }

    @Test
    @DisplayName("Deve salvar lista de parâmetros")
    void salvar_sucesso() {
        // Arrange
        ParametroRequest req = new ParametroRequest(1L, "CHAVE_1", "VALOR_1", "Desc 1");
        Parametro p1 = Parametro.builder().chave("CHAVE_1").valor("VALOR_1").descricao("Desc 1").build();
        List<ParametroRequest> lista = List.of(req);
        when(configuracaoService.salvar(any())).thenReturn(List.of(p1));
        when(parametroMapper.toResponse(any())).thenReturn(mock(ParametroResponse.class));

        // Act
        List<ParametroResponse> resultado = configuracaoFacade.salvar(lista);

        // Assert
        assertThat(resultado).hasSize(1);
        verify(configuracaoService).salvar(any());
    }

    @Test
    @DisplayName("Deve atualizar valor de parâmetro existente")
    void atualizar_sucesso() {
        // Arrange
        String chave = "CHAVE_ATUALIZAR";
        String novoValor = "VALOR_NOVO";
        Parametro parametroAtualizado = Parametro.builder().chave(chave).valor(novoValor).descricao("Desc").build();

        when(configuracaoService.atualizar(chave, novoValor)).thenReturn(parametroAtualizado);

        // Act
        Parametro resultado = configuracaoFacade.atualizar(chave, novoValor);

        // Assert
        assertThat(resultado.getValor()).isEqualTo(novoValor);
        verify(configuracaoService).atualizar(chave, novoValor);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar parâmetro inexistente")
    void atualizar_naoEncontrado() {
        // Arrange
        String chave = "INEXISTENTE";
        when(configuracaoService.atualizar(chave, "novo"))
                .thenThrow(new ErroConfiguracao(
                        "Parâmetro '%s' não encontrado. Configure o parâmetro no banco de dados.".formatted(chave)));

        // Act & Assert
        assertThatThrownBy(() -> configuracaoFacade.atualizar(chave, "novo"))
                .isInstanceOf(ErroConfiguracao.class);
    }
}
