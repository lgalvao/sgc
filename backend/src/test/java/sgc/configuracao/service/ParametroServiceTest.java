package sgc.configuracao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroConfiguracao;
import sgc.configuracao.model.Parametro;
import sgc.configuracao.model.ParametroRepo;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParametroService")
class ParametroServiceTest {

    @InjectMocks
    private ParametroService parametroService;

    @Mock
    private ParametroRepo parametroRepo;

    @Test
    @DisplayName("Deve buscar todos os parâmetros")
    void buscarTodos_sucesso() {
        // Arrange
        Parametro p1 = new Parametro("CHAVE_1", "VALOR_1", "Desc 1");
        Parametro p2 = new Parametro("CHAVE_2", "VALOR_2", "Desc 2");
        when(parametroRepo.findAll()).thenReturn(List.of(p1, p2));

        // Act
        List<Parametro> resultado = parametroService.buscarTodos();

        // Assert
        assertThat(resultado).hasSize(2).contains(p1, p2);
        verify(parametroRepo).findAll();
    }

    @Test
    @DisplayName("Deve buscar parâmetro por chave com sucesso")
    void buscarPorChave_sucesso() {
        // Arrange
        String chave = "TESTE_CHAVE";
        Parametro parametro = new Parametro(chave, "VALOR", "Desc");
        when(parametroRepo.findByChave(chave)).thenReturn(Optional.of(parametro));

        // Act
        Parametro resultado = parametroService.buscarPorChave(chave);

        // Assert
        assertThat(resultado).isEqualTo(parametro);
        verify(parametroRepo).findByChave(chave);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar chave inexistente")
    void buscarPorChave_naoEncontrado() {
        // Arrange
        String chave = "INEXISTENTE";
        when(parametroRepo.findByChave(chave)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> parametroService.buscarPorChave(chave))
                .isInstanceOf(ErroConfiguracao.class)
                .hasMessageContaining(chave);
    }

    @Test
    @DisplayName("Deve salvar lista de parâmetros")
    void salvar_sucesso() {
        // Arrange
        Parametro p1 = new Parametro("CHAVE_1", "VALOR_1", "Desc 1");
        List<Parametro> lista = List.of(p1);
        when(parametroRepo.saveAll(lista)).thenReturn(lista);

        // Act
        List<Parametro> resultado = parametroService.salvar(lista);

        // Assert
        assertThat(resultado).isEqualTo(lista);
        verify(parametroRepo).saveAll(lista);
    }

    @Test
    @DisplayName("Deve atualizar valor de parâmetro existente")
    void atualizar_sucesso() {
        // Arrange
        String chave = "CHAVE_ATUALIZAR";
        String valorAntigo = "VALOR_ANTIGO";
        String novoValor = "VALOR_NOVO";
        Parametro parametro = new Parametro(chave, valorAntigo, "Desc");

        when(parametroRepo.findByChave(chave)).thenReturn(Optional.of(parametro));
        when(parametroRepo.save(any(Parametro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Parametro resultado = parametroService.atualizar(chave, novoValor);

        // Assert
        assertThat(resultado.getValor()).isEqualTo(novoValor);
        verify(parametroRepo).findByChave(chave);
        verify(parametroRepo).save(parametro);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar parâmetro inexistente")
    void atualizar_naoEncontrado() {
        // Arrange
        String chave = "INEXISTENTE";
        when(parametroRepo.findByChave(chave)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> parametroService.atualizar(chave, "novo"))
                .isInstanceOf(ErroConfiguracao.class);
        verify(parametroRepo, never()).save(any());
    }
}
