package sgc.integracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import sgc.sgrh.UsuarioRepo;
import sgc.unidade.modelo.UnidadeRepo;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Smoke Test: Verificação do Setup de Dados")
public class DataSetupSmokeTest {

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Test
    @DisplayName("Deve carregar unidades do data.sql")
    void shouldLoadUnidadesFromDataSql() {
        assertThat(unidadeRepo.count()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Deve carregar usuários do data.sql")
    void shouldLoadUsuariosFromDataSql() {
        assertThat(usuarioRepo.count()).isGreaterThan(0);
    }
}
