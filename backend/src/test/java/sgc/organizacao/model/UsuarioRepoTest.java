package sgc.organizacao.model;

import org.hibernate.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.transaction.annotation.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Tag("integration")
@DisplayName("UsuarioRepo - Testes de Repositório")
class UsuarioRepoTest {

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Test
    @DisplayName("deve buscar usuario por titulo com unidade de lotacao carregada")
    void deveBuscarUsuarioPorTituloComUnidadeDeLotacaoCarregada() {
        Usuario usuario = usuarioRepo.findByTituloComUnidadeLotacao("1").orElseThrow();

        assertThat(usuario.getNome()).isEqualTo("Ana Paula Souza");
        assertThat(Hibernate.isInitialized(usuario.getUnidadeLotacao())).isTrue();
        assertThat(usuario.getUnidadeLotacao().getCodigo()).isEqualTo(10L);
    }

    @Test
    @DisplayName("deve buscar usuarios por titulos com unidade de lotacao")
    void deveBuscarUsuariosPorTitulosComUnidadeDeLotacao() {
        List<Usuario> usuarios = usuarioRepo.findByTitulosComUnidadeLotacao(List.of("1", "4", "17"));

        assertThat(usuarios)
                .extracting(Usuario::getTituloEleitoral)
                .containsExactlyInAnyOrder("1", "4", "17");
        assertThat(usuarios)
                .allSatisfy(usuario -> assertThat(Hibernate.isInitialized(usuario.getUnidadeLotacao())).isTrue());
    }

    @Test
    @DisplayName("deve buscar usuarios por nome ou matricula")
    void deveBuscarUsuariosPorNomeOuMatricula() {
        List<Usuario> porNome = usuarioRepo.buscarPorNomeOuMatriculaComUnidadeLotacao("Ana Paula");
        List<Usuario> porMatricula = usuarioRepo.buscarPorNomeOuMatriculaComUnidadeLotacao("00000017");

        assertThat(porNome).extracting(Usuario::getTituloEleitoral).contains("1");
        assertThat(porMatricula).extracting(Usuario::getTituloEleitoral).containsExactly("17");
        assertThat(porMatricula.getFirst().getUnidadeLotacao().getCodigo()).isEqualTo(10L);
    }
}
