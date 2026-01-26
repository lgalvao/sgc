package sgc.organizacao.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de métodos default de UnidadeRepo")
class UnidadeRepoDefaultTest {

    @Test
    @DisplayName("Deve buscar por titulo titular ativo")
    void deveBuscarPorTituloTitular() {
        // Create a mock that supports callRealMethod for default methods
        UnidadeRepo repo = mock(UnidadeRepo.class);

        // Setup callRealMethod for the default method
        doCallRealMethod().when(repo).findByTituloTitular(any());

        // Mock the dependency method
        String titulo = "Chefe";
        List<Unidade> expected = List.of(new Unidade());
        when(repo.findByTituloTitularAndSituacao(titulo, SituacaoUnidade.ATIVA)).thenReturn(expected);

        // Execute
        List<Unidade> result = repo.findByTituloTitular(titulo);

        // Verify
        assertThat(result).isSameAs(expected);
        verify(repo).findByTituloTitularAndSituacao(titulo, SituacaoUnidade.ATIVA);
    }
}
