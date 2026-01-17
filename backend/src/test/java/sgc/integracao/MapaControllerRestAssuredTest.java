package sgc.integracao;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.mapa.dto.MapaDto;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.organizacao.model.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class MapaControllerRestAssuredTest extends BaseRestAssuredTest {

    @Autowired
    private MapaRepo mapaRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    private Usuario usuarioAdmin;
    private Unidade unidade;
    private Mapa mapa;

    @BeforeEach
    void setupDados() {
        mapaRepo.deleteAll();
        usuarioRepo.deleteAll();
        unidadeRepo.deleteAll();

        unidade = new Unidade();
        unidade.setSigla("ADM_MAPA");
        unidade.setNome("Unidade Mapa");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade = unidadeRepo.save(unidade);

        usuarioAdmin = new Usuario();
        usuarioAdmin.setTituloEleitoral("22222222222");
        usuarioAdmin.setNome("Admin Mapa");
        usuarioAdmin.setUnidadeLotacao(unidade);
        usuarioAdmin = usuarioRepo.save(usuarioAdmin);

        mapa = new Mapa();
        mapa.setObservacoesDisponibilizacao("Obs Teste");
        mapa = mapaRepo.save(mapa);
    }

    @Test
    @DisplayName("Deve listar mapas")
    void deveListarMapas() {
        String token = gerarToken(usuarioAdmin.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/mapas")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .body("[0].observacoesDisponibilizacao", equalTo("Obs Teste"));
    }

    @Test
    @DisplayName("Deve criar mapa")
    void deveCriarMapa() {
        String token = gerarToken(usuarioAdmin.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        MapaDto dto = new MapaDto();
        dto = MapaDto.builder().observacoesDisponibilizacao("Nova Obs").build();

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(dto)
        .when()
            .post("/api/mapas")
        .then()
            .statusCode(201)
            .body("observacoesDisponibilizacao", equalTo("Nova Obs"));
    }
}
