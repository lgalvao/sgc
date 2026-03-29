package sgc.organizacao.model;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.transaction.annotation.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Tag("integration")
@DisplayName("AdministradorRepo - Testes de Repositório")
class AdministradorRepoTest {

    @Autowired
    private AdministradorRepo administradorRepo;

    @Test
    @DisplayName("deve listar administradores existentes e persistir novo administrador")
    void deveListarAdministradoresExistentesEPersistirNovoAdministrador() {
        assertThat(administradorRepo.findAll())
                .extracting(Administrador::getUsuarioTitulo)
                .contains("6", "111111111111", "999999999999");

        administradorRepo.save(Administrador.builder().usuarioTitulo("1").build());

        assertThat(administradorRepo.findById("1")).isPresent();
    }
}
