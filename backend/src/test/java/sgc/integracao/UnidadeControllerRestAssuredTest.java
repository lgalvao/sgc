package sgc.integracao;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.organizacao.model.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class UnidadeControllerRestAssuredTest extends BaseRestAssuredTest {

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    private Unidade unidadeTeste;
    private Usuario usuarioTeste;

    @BeforeEach
    void setupDados() {
        if (unidadeRepo.findBySigla("TESTE").isPresent()) {
            unidadeTeste = unidadeRepo.findBySigla("TESTE").get();
        } else {
            unidadeTeste = new Unidade();
            unidadeTeste.setSigla("TESTE");
            unidadeTeste.setNome("Unidade de Teste");
            unidadeTeste.setTipo(TipoUnidade.OPERACIONAL);
            unidadeTeste = unidadeRepo.save(unidadeTeste);
        }

        String titulo = "12345678900";
        if (usuarioRepo.findById(titulo).isPresent()) {
            usuarioTeste = usuarioRepo.findById(titulo).get();
        } else {
            usuarioTeste = new Usuario();
            usuarioTeste.setTituloEleitoral(titulo);
            usuarioTeste.setNome("Usuario Teste");
            usuarioTeste.setMatricula("12345");
            usuarioTeste.setEmail("teste@teste.com");
            usuarioTeste.setUnidadeLotacao(unidadeTeste);
            usuarioTeste = usuarioRepo.save(usuarioTeste);
        }
    }

    @Test
    @DisplayName("Deve listar todas as unidades com sucesso")
    void deveListarTodasUnidades() {
        String token = gerarToken(usuarioTeste.getTituloEleitoral(), Perfil.ADMIN, unidadeTeste.getCodigo());

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/unidades")
        .then()
            .statusCode(200)
            .body("$", not(empty()));
    }

    @Test
    @DisplayName("Deve buscar unidade por código existente")
    void deveBuscarUnidadePorCodigoExistente() {
        String token = gerarToken(usuarioTeste.getTituloEleitoral(), Perfil.ADMIN, unidadeTeste.getCodigo());

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/unidades/{codigo}", unidadeTeste.getCodigo())
        .then()
            .statusCode(200)
            .body("codigo", equalTo(unidadeTeste.getCodigo().intValue()))
            .body("sigla", equalTo(unidadeTeste.getSigla()));
    }

    @Test
    @DisplayName("Deve retornar 404 para unidade inexistente")
    void deveRetornar404ParaUnidadeInexistente() {
        String token = gerarToken(usuarioTeste.getTituloEleitoral(), Perfil.ADMIN, unidadeTeste.getCodigo());
        long idInexistente = 9999999L;

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/unidades/{codigo}", idInexistente)
        .then()
            .statusCode(404);
    }
}
