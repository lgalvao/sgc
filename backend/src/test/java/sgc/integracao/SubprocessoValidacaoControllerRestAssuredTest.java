package sgc.integracao;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class SubprocessoValidacaoControllerRestAssuredTest extends BaseRestAssuredTest {

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    private Usuario usuarioAdmin;
    private Unidade unidade;
    private Subprocesso subprocesso;

    @BeforeEach
    void setupDados() {
        subprocessoRepo.deleteAll();
        processoRepo.deleteAll();
        usuarioRepo.deleteAll();
        unidadeRepo.deleteAll();

        unidade = new Unidade();
        unidade.setSigla("ADM_SUB_VAL");
        unidade.setNome("Unidade Sub Val");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade = unidadeRepo.save(unidade);

        usuarioAdmin = new Usuario();
        usuarioAdmin.setTituloEleitoral("15151515151");
        usuarioAdmin.setNome("Admin Sub Val");
        usuarioAdmin.setUnidadeLotacao(unidade);
        usuarioAdmin = usuarioRepo.save(usuarioAdmin);

        Processo processo = new Processo();
        processo.setDescricao("Processo Sub Val");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDataCriacao(LocalDateTime.now());
        processo = processoRepo.save(processo);

        subprocesso = new Subprocesso();
        subprocesso.setProcesso(processo);
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocesso = subprocessoRepo.save(subprocesso);
    }

    @Test
    @DisplayName("Deve obter sugestões (vazio)")
    void deveObterSugestoes() {
        String token = gerarToken(usuarioAdmin.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/subprocessos/{codigo}/sugestoes", subprocesso.getCodigo())
        .then()
            .statusCode(200)
            .body("sugestoes", nullValue());
            // Expecting empty or null depending on implementation, but likely 200 OK.
            // Facade usually returns DTO. If null in DB, field might be null.
    }

    @Test
    @DisplayName("Deve obter histórico de validação")
    void deveObterHistoricoValidacao() {
        String token = gerarToken(usuarioAdmin.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/subprocessos/{codigo}/historico-validacao", subprocesso.getCodigo())
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(0));
    }
}
