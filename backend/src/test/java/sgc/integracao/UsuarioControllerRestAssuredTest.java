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

    @Autowired
    private UsuarioPerfilRepo usuarioPerfilRepo;

    private Usuario usuarioAdmin;
    private Unidade unidade;

    @BeforeEach
    void setupDados() {
        unidade = unidadeRepo.findBySigla("ADM_USR").orElseGet(() -> {
            Unidade u = new Unidade();
            u.setSigla("ADM_USR");
            u.setNome("Unidade Usuario");
            u.setTipo(TipoUnidade.OPERACIONAL);
            return unidadeRepo.save(u);
        });

        usuarioAdmin = usuarioRepo.findById("99999999999").orElseGet(() -> {
            Usuario u = new Usuario();
            u.setTituloEleitoral("99999999999");
            u.setNome("Admin Usuario");
            u.setUnidadeLotacao(unidade);
            return usuarioRepo.save(u);
        });

        UsuarioPerfilId id = new UsuarioPerfilId(usuarioAdmin.getTituloEleitoral(), unidade.getCodigo(), Perfil.ADMIN);
        if (!usuarioPerfilRepo.existsById(id)) {
            UsuarioPerfil up = new UsuarioPerfil();
            up.setUsuarioTitulo(usuarioAdmin.getTituloEleitoral());
            up.setUnidadeCodigo(unidade.getCodigo());
            up.setPerfil(Perfil.ADMIN);
            usuarioPerfilRepo.save(up);
        }
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
