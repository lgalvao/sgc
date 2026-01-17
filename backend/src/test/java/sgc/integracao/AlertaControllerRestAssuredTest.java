package sgc.integracao;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.organizacao.model.*;

import java.time.LocalDateTime;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class AlertaControllerRestAssuredTest extends BaseRestAssuredTest {

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    private Usuario usuario;
    private Unidade unidade;
    private Alerta alerta;

    @BeforeEach
    void setupDados() {
        alertaRepo.deleteAll();
        usuarioRepo.deleteAll();
        unidadeRepo.deleteAll();

        unidade = new Unidade();
        unidade.setSigla("TESTE_ALERTA");
        unidade.setNome("Unidade de Teste Alerta");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade = unidadeRepo.save(unidade);

        usuario = new Usuario();
        usuario.setTituloEleitoral("99999999999");
        usuario.setNome("Usuario Alerta");
        usuario.setMatricula("99999");
        usuario.setEmail("alerta@teste.com");
        usuario.setUnidadeLotacao(unidade);
        usuario = usuarioRepo.save(usuario);

        alerta = Alerta.builder()
                .usuarioDestino(usuario)
                .unidadeDestino(unidade)
                .descricao("Alerta de Teste")
                .dataHora(LocalDateTime.now())
                .build();
        alerta = alertaRepo.save(alerta);
    }

    @Test
    @DisplayName("Deve listar alertas do usuário")
    void deveListarAlertas() {
        String token = gerarToken(usuario.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/alertas")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .body("[0].descricao", equalTo("Alerta de Teste"));
    }

    @Test
    @DisplayName("Deve marcar alertas como lidos")
    void deveMarcarComoLidos() {
        String token = gerarToken(usuario.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(List.of(alerta.getCodigo()))
        .when()
            .post("/api/alertas/marcar-como-lidos")
        .then()
            .statusCode(200)
            .body("message", containsString("marcados como lidos"));
    }
}
