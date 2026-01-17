package sgc.integracao;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import sgc.alerta.model.AlertaRepo;
import sgc.organizacao.model.*;
import sgc.seguranca.login.dto.AutenticarRequest;
import sgc.seguranca.login.dto.EntrarRequest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestPropertySource(properties = "aplicacao.ambiente-testes=true")
class LoginControllerRestAssuredTest extends BaseRestAssuredTest {

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @Autowired
    private AlertaRepo alertaRepo;

    private Usuario usuario;
    private Unidade unidade;

    @BeforeEach
    void setupDados() {
        // Reuse or create Unidade
        if (unidadeRepo.findBySigla("TESTE_LOGIN").isPresent()) {
            unidade = unidadeRepo.findBySigla("TESTE_LOGIN").get();
        } else {
            unidade = new Unidade();
            unidade.setSigla("TESTE_LOGIN");
            unidade.setNome("Unidade Login");
            unidade.setTipo(TipoUnidade.OPERACIONAL);
            unidade = unidadeRepo.save(unidade);
        }

        // Reuse or create Usuario
        String titulo = "11111111111";
        if (usuarioRepo.findById(titulo).isPresent()) {
            usuario = usuarioRepo.findById(titulo).get();
        } else {
            usuario = new Usuario();
            usuario.setTituloEleitoral(titulo);
            usuario.setNome("Usuario Login");
            usuario.setUnidadeLotacao(unidade);
            usuario = usuarioRepo.save(usuario);
        }

        // Ensure Profile exists
        UsuarioPerfilId id = new UsuarioPerfilId(titulo, unidade.getCodigo(), Perfil.ADMIN);
        if (!usuarioPerfilRepo.existsById(id)) {
            UsuarioPerfil atribuicao = UsuarioPerfil.builder()
                    .usuario(usuario)
                    .unidade(unidade)
                    .perfil(Perfil.ADMIN)
                    .usuarioTitulo(usuario.getTituloEleitoral())
                    .unidadeCodigo(unidade.getCodigo())
                    .build();
            usuarioPerfilRepo.save(atribuicao);
        }
    }

    @Test
    @DisplayName("Deve autenticar com sucesso")
    void deveAutenticar() {
        AutenticarRequest req = new AutenticarRequest("11111111111", "senha_qualquer");

        given()
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/usuarios/autenticar")
        .then()
            .statusCode(200)
            .body(is("true"));
    }

    @Test
    @DisplayName("Deve realizar login (entrar)")
    void deveEntrar() {
        // First authenticate (internal cache)
        AutenticarRequest authReq = new AutenticarRequest("11111111111", "senha_qualquer");
        given().contentType(ContentType.JSON).body(authReq).post("/api/usuarios/autenticar");

        EntrarRequest req = new EntrarRequest("11111111111", "ADMIN", unidade.getCodigo());

        given()
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/usuarios/entrar")
        .then()
            .statusCode(200)
            .body("token", not(emptyOrNullString()))
            .body("nome", equalTo("Usuario Login"));
    }
}
