package sgc.organizacao.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de métodos default de UnidadeRepo")
class UnidadeRepoDefaultTest {
    @Test
    @DisplayName("Deve buscar por titulo titular ativo")
    void deveBuscarPorTituloTitular() {
        UnidadeRepo repo = mock(UnidadeRepo.class);
        doCallRealMethod().when(repo).findByTituloTitular(any());
        String titulo = "Chefe";
        List<Unidade> expected = List.of(new Unidade());
        when(repo.findByTituloTitularAndSituacao(titulo, SituacaoUnidade.ATIVA)).thenReturn(expected);
        List<Unidade> result = repo.findByTituloTitular(titulo);
        assertThat(result).isSameAs(expected);
        verify(repo).findByTituloTitularAndSituacao(titulo, SituacaoUnidade.ATIVA);
    }
}
