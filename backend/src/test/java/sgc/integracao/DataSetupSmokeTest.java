package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.unidade.modelo.Unidade;
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

    @BeforeEach
    void setUp() {
        usuarioRepo.deleteAll();
        unidadeRepo.deleteAll();
    }

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

    @Test
    @DisplayName("Deve persistir e recuperar uma nova unidade")
    void shouldPersistAndRetrieveNewUnidade() {
        Unidade novaUnidade = new Unidade();
        novaUnidade.setCodigo(999L);
        novaUnidade.setNome("Unidade de Teste");
        novaUnidade.setSigla("UT");
        unidadeRepo.save(novaUnidade);

        Unidade unidadeSalva = unidadeRepo.findById(999L).orElseThrow();
        assertThat(unidadeSalva.getNome()).isEqualTo("Unidade de Teste");
    }

    @Test
    @DisplayName("Deve persistir e recuperar um novo usuário")
    void shouldPersistAndRetrieveNewUsuario() {
        Usuario novoUsuario = new Usuario();
        novoUsuario.setTituloEleitoral(999999999999L);
        novoUsuario.setNome("Usuario de Teste");
        novoUsuario.setEmail("teste@teste.com");
        usuarioRepo.save(novoUsuario);

        Usuario usuarioSalvo = usuarioRepo.findById(999999999999L).orElseThrow();
        assertThat(usuarioSalvo.getNome()).isEqualTo("Usuario de Teste");
    }
}
