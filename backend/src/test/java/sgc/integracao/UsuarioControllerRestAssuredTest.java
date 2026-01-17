package sgc.integracao;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.organizacao.model.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class UsuarioControllerRestAssuredTest extends BaseRestAssuredTest {

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private AdministradorRepo administradorRepo;

    private Usuario usuarioAdmin;
    private Unidade unidade;

    @BeforeEach
    void setupDados() {
        administradorRepo.deleteAll();
        usuarioRepo.deleteAll();
        unidadeRepo.deleteAll();

        unidade = new Unidade();
        unidade.setSigla("ADM_USR");
        unidade.setNome("Unidade Usuario");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade = unidadeRepo.save(unidade);

        usuarioAdmin = new Usuario();
        usuarioAdmin.setTituloEleitoral("99999999999");
        usuarioAdmin.setNome("Admin Usuario");
        usuarioAdmin.setUnidadeLotacao(unidade);
        usuarioAdmin = usuarioRepo.save(usuarioAdmin);
    }

    @Test
    @DisplayName("Deve buscar usuário por título")
    void deveBuscarUsuario() {
        String token = gerarToken(usuarioAdmin.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/usuarios/{titulo}", usuarioAdmin.getTituloEleitoral())
        .then()
            .statusCode(200)
            .body("tituloEleitoral", equalTo(usuarioAdmin.getTituloEleitoral()));
    }

    @Test
    @DisplayName("Deve listar administradores")
    void deveListarAdministradores() {
        String token = gerarToken(usuarioAdmin.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/usuarios/administradores")
        .then()
            .statusCode(200);
    }
}
