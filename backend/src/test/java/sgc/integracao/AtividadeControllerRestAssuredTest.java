package sgc.integracao;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.mapa.dto.CriarAtividadeRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.organizacao.model.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class AtividadeControllerRestAssuredTest extends BaseRestAssuredTest {

    @Autowired
    private AtividadeRepo atividadeRepo;

    @Autowired
    private MapaRepo mapaRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    private Usuario usuarioAdmin;
    private Unidade unidade;
    private Mapa mapa;
    private Atividade atividade;

    @BeforeEach
    void setupDados() {
        atividadeRepo.deleteAll();
        mapaRepo.deleteAll();
        usuarioRepo.deleteAll();
        unidadeRepo.deleteAll();

        unidade = new Unidade();
        unidade.setSigla("ADM_ATIV");
        unidade.setNome("Unidade Atividade");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade = unidadeRepo.save(unidade);

        usuarioAdmin = new Usuario();
        usuarioAdmin.setTituloEleitoral("33333333333");
        usuarioAdmin.setNome("Admin Atividade");
        usuarioAdmin.setUnidadeLotacao(unidade);
        usuarioAdmin = usuarioRepo.save(usuarioAdmin);

        mapa = new Mapa();
        mapa = mapaRepo.save(mapa);

        atividade = new Atividade(mapa, "Atividade 1");
        atividade = atividadeRepo.save(atividade);
    }

    @Test
    @DisplayName("Deve obter atividade por ID")
    void deveObterAtividade() {
        String token = gerarToken(usuarioAdmin.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/atividades/{id}", atividade.getCodigo())
        .then()
            .statusCode(200)
            .body("descricao", equalTo("Atividade 1"));
    }

    @Test
    @DisplayName("Deve criar atividade")
    void deveCriarAtividade() {
        String token = gerarToken(usuarioAdmin.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        CriarAtividadeRequest req = CriarAtividadeRequest.builder()
                .mapaCodigo(mapa.getCodigo())
                .descricao("Nova Atividade")
                .build();

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/atividades")
        .then()
            .statusCode(201)
            .body("atividade.descricao", equalTo("Nova Atividade"));
    }
}
