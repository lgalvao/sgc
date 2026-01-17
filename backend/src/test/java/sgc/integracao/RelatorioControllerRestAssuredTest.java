package sgc.integracao;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.organizacao.model.*;
import sgc.processo.model.*;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;

class RelatorioControllerRestAssuredTest extends BaseRestAssuredTest {

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
        unidade.setSigla("ADM_REL");
        unidade.setNome("Unidade Relatorio");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade = unidadeRepo.save(unidade);

        usuarioAdmin = new Usuario();
        usuarioAdmin.setTituloEleitoral("66666666666");
        usuarioAdmin.setNome("Admin Relatorio");
        usuarioAdmin.setUnidadeLotacao(unidade);
        usuarioAdmin = usuarioRepo.save(usuarioAdmin);

        processo = new Processo();
        processo.setDescricao("Processo Relatorio");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDataCriacao(LocalDateTime.now());
        processo = processoRepo.save(processo);
    }

    @Test
    @DisplayName("Deve gerar relatório de andamento")
    void deveGerarRelatorioAndamento() {
        String token = gerarToken(usuarioAdmin.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/relatorios/andamento/{id}", processo.getCodigo())
        .then()
            .statusCode(200)
            .contentType("application/pdf");
    }

    @Test
    @DisplayName("Deve gerar relatório de mapas")
    void deveGerarRelatorioMapas() {
        String token = gerarToken(usuarioAdmin.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/relatorios/mapas/{id}", processo.getCodigo())
        .then()
            .statusCode(200)
            .contentType("application/pdf");
    }
}
