package sgc.organizacao.model;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.transaction.annotation.*;

import static org.assertj.core.api.Assertions.*;

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
