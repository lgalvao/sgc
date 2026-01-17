package sgc.integracao;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.organizacao.model.*;
import sgc.processo.dto.CriarProcessoRequest;
import sgc.processo.model.*;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class ProcessoControllerRestAssuredTest extends BaseRestAssuredTest {

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    private Usuario usuarioAdmin;
    private Unidade unidade;
    private Processo processo;

    @BeforeEach
    void setupDados() {
        processoRepo.deleteAll();
        usuarioRepo.deleteAll();
        unidadeRepo.deleteAll();

        unidade = new Unidade();
        unidade.setSigla("ADM_PROC");
        unidade.setNome("Unidade Processo");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade = unidadeRepo.save(unidade);

        usuarioAdmin = new Usuario();
        usuarioAdmin.setTituloEleitoral("44444444444");
        usuarioAdmin.setNome("Admin Processo");
        usuarioAdmin.setUnidadeLotacao(unidade);
        usuarioAdmin = usuarioRepo.save(usuarioAdmin);

        processo = new Processo();
        processo.setDescricao("Processo Existente");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDataCriacao(LocalDateTime.now());
        processo = processoRepo.save(processo);
    }

    @Test
    @DisplayName("Deve listar processos ativos")
    void deveListarAtivos() {
        String token = gerarToken(usuarioAdmin.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/processos/ativos")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0));
    }

    @Test
    @DisplayName("Deve criar processo")
    void deveCriarProcesso() {
        String token = gerarToken(usuarioAdmin.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        CriarProcessoRequest req = CriarProcessoRequest.builder()
                .descricao("Novo Processo")
                .tipo(TipoProcesso.DIAGNOSTICO)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(30))
                .unidades(java.util.List.of(unidade.getCodigo()))
                .build();

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/processos")
        .then()
            .statusCode(201)
            .body("descricao", equalTo("Novo Processo"));
    }
}
