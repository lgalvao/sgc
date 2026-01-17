package sgc.integracao;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.analise.model.*;
import sgc.analise.dto.CriarAnaliseRequest;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class AnaliseControllerRestAssuredTest extends BaseRestAssuredTest {

    @Autowired
    private AnaliseRepo analiseRepo;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    private Usuario usuario;
    private Unidade unidade;
    private Subprocesso subprocesso;

    @BeforeEach
    void setupDados() {
        analiseRepo.deleteAll();
        subprocessoRepo.deleteAll();
        processoRepo.deleteAll();
        usuarioRepo.deleteAll();
        unidadeRepo.deleteAll();

        unidade = new Unidade();
        unidade.setSigla("TESTE_ANALISE");
        unidade.setNome("Unidade de Teste Analise");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade = unidadeRepo.save(unidade);

        usuario = new Usuario();
        usuario.setTituloEleitoral("88888888888");
        usuario.setNome("Usuario Analise");
        usuario.setMatricula("88888");
        usuario.setEmail("analise@teste.com");
        usuario.setUnidadeLotacao(unidade);
        usuario = usuarioRepo.save(usuario);

        Processo processo = new Processo();
        processo.setDescricao("Processo Teste Analise");
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
    @DisplayName("Deve listar análises de cadastro (vazio inicialmente)")
    void deveListarAnalisesCadastro() {
        String token = gerarToken(usuario.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/subprocessos/{codSubprocesso}/analises-cadastro", subprocesso.getCodigo())
        .then()
            .statusCode(200)
            .body("size()", equalTo(0));
    }

    @Test
    @DisplayName("Deve criar uma análise de cadastro")
    void deveCriarAnaliseCadastro() {
        String token = gerarToken(usuario.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        CriarAnaliseRequest request = new CriarAnaliseRequest(
                "Observacao Teste",
                unidade.getSigla(),
                usuario.getTituloEleitoral(),
                "Motivo Teste"
        );

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/subprocessos/{codSubprocesso}/analises-cadastro", subprocesso.getCodigo())
        .then()
            .statusCode(201)
            .body("observacoes", equalTo("Observacao Teste"))
            .body("tipo", equalTo("CADASTRO"));
    }
}
