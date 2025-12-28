package sgc.unidade.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.usuario.model.Usuario;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes da Entidade Unidade")
class UnidadeTest {

    @Test
    @DisplayName("Deve atualizar matrícula e título do titular quando usuário não for nulo")
    void deveAtualizarTitularQuandoUsuarioNaoNulo() {
        // Arrange
        Unidade unidade = new Unidade();
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123456789012");
        usuario.setMatricula("12345678");

        // Act
        unidade.setTitular(usuario);

        // Assert
        assertThat(unidade.getTituloTitular()).isEqualTo("123456789012");
        assertThat(unidade.getMatriculaTitular()).isEqualTo("12345678");
    }

    @Test
    @DisplayName("Não deve atualizar matrícula e título do titular quando usuário for nulo")
    void naoDeveAtualizarTitularQuandoUsuarioNulo() {
        // Arrange
        Unidade unidade = new Unidade();
        unidade.setTituloTitular("ORIGINAL");
        unidade.setMatriculaTitular("ORIGINAL");

        // Act
        unidade.setTitular(null);

        // Assert
        assertThat(unidade.getTituloTitular()).isEqualTo("ORIGINAL");
        assertThat(unidade.getMatriculaTitular()).isEqualTo("ORIGINAL");
    }
}
