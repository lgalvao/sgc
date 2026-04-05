package sgc.organizacao.model;

import org.hibernate.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.*;
import sgc.organizacao.dto.*;

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
    @DisplayName("deve buscar usuarios por prefixo do nome com limite")
    void deveBuscarUsuariosPorNome() {
        List<UsuarioPesquisaDto> porNome = usuarioRepo.pesquisarPorNome("Ana", PageRequest.of(0, 20));

        assertThat(porNome).extracting(UsuarioPesquisaDto::tituloEleitoral).contains("1");
    }

    @Test
    @DisplayName("deve buscar usuarios por prefixo do titulo eleitoral com limite")
    void deveBuscarUsuariosPorTituloEleitoral() {
        List<UsuarioPesquisaDto> porTitulo = usuarioRepo.pesquisarPorNome("17", PageRequest.of(0, 20));

        assertThat(porTitulo).extracting(UsuarioPesquisaDto::tituloEleitoral).contains("17");
    }
}
