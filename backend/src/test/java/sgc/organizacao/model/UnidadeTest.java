package sgc.organizacao.model;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

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
        unidade.setTituloTitular(usuario.getTituloEleitoral());
        unidade.setMatriculaTitular(usuario.getMatricula());

        // Assert
        assertThat(unidade.getTituloTitular()).isEqualTo("123456789012");
        assertThat(unidade.getMatriculaTitular()).isEqualTo("12345678");
    }

    @Test
    @DisplayName("Deve instanciar via builder")
    void deveInstanciarViaBuilder() {
        Unidade u = Unidade.builder().nome("Nome").sigla("SIGLA").build();
        assertThat(u.getNome()).isEqualTo("Nome");
        assertThat(u.getSigla()).isEqualTo("SIGLA");
    }
}
