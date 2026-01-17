package sgc.integracao;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.organizacao.model.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class PainelControllerRestAssuredTest extends BaseRestAssuredTest {

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    private Usuario usuario;
    private Unidade unidade;

    @BeforeEach
    void setupDados() {
        usuarioRepo.deleteAll();
        unidadeRepo.deleteAll();

        unidade = new Unidade();
        unidade.setSigla("PAINEL_TESTE");
        unidade.setNome("Unidade Painel");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade = unidadeRepo.save(unidade);

        usuario = new Usuario();
        usuario.setTituloEleitoral("55555555555");
        usuario.setNome("Usuario Painel");
        usuario.setUnidadeLotacao(unidade);
        usuario = usuarioRepo.save(usuario);
    }

    @Test
    @DisplayName("Deve listar processos no painel")
    void deveListarProcessos() {
        String token = gerarToken(usuario.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .queryParam("perfil", "ADMIN")
        .when()
            .get("/api/painel/processos")
        .then()
            .statusCode(200)
            .body("content", not(nullValue()));
    }

    @Test
    @DisplayName("Deve listar alertas no painel")
    void deveListarAlertas() {
        String token = gerarToken(usuario.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .queryParam("usuarioTitulo", usuario.getTituloEleitoral())
        .when()
            .get("/api/painel/alertas")
        .then()
            .statusCode(200)
            .body("content", not(nullValue()));
    }
}
