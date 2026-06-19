package sgc.organizacao.model;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        Usuario usuario = usuarioRepo.buscarPorTituloComUnidadeLotacao("1").orElseThrow();

        assertThat(usuario.getNome()).isEqualTo("Ana Paula Souza");
        assertThat(Hibernate.isInitialized(usuario.getUnidadeLotacao())).isTrue();
        assertThat(usuario.getUnidadeLotacao().getCodigo()).isEqualTo(10L);
    }

    @Test
    @DisplayName("deve buscar usuarios por titulos com unidade de lotacao")
    void deveBuscarUsuariosPorTitulosComUnidadeDeLotacao() {
        List<Usuario> usuarios = usuarioRepo.listarPorTitulosComUnidadeLotacao(List.of("1", "4", "17"));

        assertThat(usuarios)
                .extracting(Usuario::getTituloEleitoral)
                .containsExactlyInAnyOrder("1", "4", "17");
        assertThat(usuarios)
                .allSatisfy(usuario -> assertThat(Hibernate.isInitialized(usuario.getUnidadeLotacao())).isTrue());
    }

    @Test
    @DisplayName("deve listar consultas de usuarios com unidade de lotacao")
    void deveListarConsultasUsuariosComUnidadeDeLotacao() {
        List<UsuarioConsultaLeitura> usuarios = usuarioRepo.listarTodasConsultas();

        assertThat(usuarios)
                .extracting(UsuarioConsultaLeitura::tituloEleitoral)
                .contains("1");
        assertThat(usuarios)
                .filteredOn(usuario -> "1".equals(usuario.tituloEleitoral()))
                .first()
                .extracting(UsuarioConsultaLeitura::unidadeCodigo)
                .isEqualTo(10L);
    }
}
