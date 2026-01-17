package sgc.integracao;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.configuracao.model.Parametro;
import sgc.configuracao.model.ParametroRepo;
import sgc.organizacao.model.*;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class ConfiguracaoControllerRestAssuredTest extends BaseRestAssuredTest {

    @Autowired
    private ParametroRepo parametroRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    private Usuario usuarioAdmin;
    private Unidade unidade;

    @BeforeEach
    void setupDados() {
        parametroRepo.deleteAll();
        usuarioRepo.deleteAll();
        unidadeRepo.deleteAll();

        unidade = new Unidade();
        unidade.setSigla("ADM_CONF");
        unidade.setNome("Unidade Admin");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade = unidadeRepo.save(unidade);

        usuarioAdmin = new Usuario();
        usuarioAdmin.setTituloEleitoral("77777777777");
        usuarioAdmin.setNome("Admin Config");
        usuarioAdmin.setMatricula("77777");
        usuarioAdmin.setEmail("admin@teste.com");
        usuarioAdmin.setUnidadeLotacao(unidade);
        usuarioAdmin = usuarioRepo.save(usuarioAdmin);

        Parametro p = new Parametro();
        p.setChave("CONF_TESTE");
        p.setValor("VALOR_TESTE");
        p.setDescricao("Configuracao de Teste");
        parametroRepo.save(p);
    }

    @Test
    @DisplayName("Deve listar configurações como ADMIN")
    void deveListarConfiguracoes() {
        String token = gerarToken(usuarioAdmin.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/configuracoes")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .body("[0].chave", equalTo("CONF_TESTE"));
    }

    @Test
    @DisplayName("Deve atualizar configurações como ADMIN")
    void deveAtualizarConfiguracoes() {
        String token = gerarToken(usuarioAdmin.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        Parametro p = new Parametro();
        p.setChave("CONF_NOVA");
        p.setValor("VALOR_NOVO");
        p.setDescricao("Nova Config");

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(List.of(p))
        .when()
            .post("/api/configuracoes")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0));
            // Note: The response depends on whether it returns all or just saved, but controller says "salvar" and returns list.
            // Assuming it returns the saved list or all.
    }

    @Test
    @DisplayName("Deve negar acesso a não ADMIN")
    void deveNegarAcessoNaoAdmin() {
        // Create a non-admin user
        Usuario usuarioComum = new Usuario();
        usuarioComum.setTituloEleitoral("66666666666");
        usuarioComum.setNome("User Comum");
        usuarioComum.setUnidadeLotacao(unidade);
        usuarioRepo.save(usuarioComum);

        String token = gerarToken(usuarioComum.getTituloEleitoral(), Perfil.SERVIDOR, unidade.getCodigo());

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/configuracoes")
        .then()
            .statusCode(403);
    }
}
