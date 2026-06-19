package sgc.organizacao.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Tag("integration")
@DisplayName("UsuarioPerfilRepo - Testes de Repositório")
class UsuarioPerfilRepoTest {

    @Autowired
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @Test
    @DisplayName("deve buscar perfis por usuario")
    void deveBuscarPerfisPorUsuario() {
        assertThat(usuarioPerfilRepo.findByUsuarioTitulo("111111111111"))
                .extracting(UsuarioPerfil::getPerfil)
                .containsExactlyInAnyOrder(Perfil.ADMIN, Perfil.CHEFE);
    }
}
