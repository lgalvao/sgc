package sgc.integracao;

import io.restassured.http.ContentType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.AlertaUsuarioRepo;
import sgc.analise.model.AnaliseRepo;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.CriarSubprocessoRequest;
import sgc.subprocesso.model.*;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class SubprocessoCrudControllerRestAssuredTest extends BaseRestAssuredTest {

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private MapaRepo mapaRepo;

    @Autowired
    private AtividadeRepo atividadeRepo;

    @Autowired
    private CompetenciaRepo competenciaRepo;

    @Autowired
    private ConhecimentoRepo conhecimentoRepo;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    @Autowired
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @Autowired
    private AnaliseRepo analiseRepo;

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private AlertaUsuarioRepo alertaUsuarioRepo;

    private Usuario usuarioAdmin;
    private Unidade unidade;
    private Processo processo;
    private Subprocesso subprocesso;

    @BeforeEach
    void setupDados() {
        transactionTemplate.execute(status -> {
            entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();

            conhecimentoRepo.deleteAllInBatch();
            entityManager.createNativeQuery("TRUNCATE TABLE sgc.competencia_atividade").executeUpdate();
            atividadeRepo.deleteAllInBatch();
            competenciaRepo.deleteAllInBatch();
            mapaRepo.deleteAllInBatch();
            analiseRepo.deleteAllInBatch();
            movimentacaoRepo.deleteAllInBatch();
            subprocessoRepo.deleteAllInBatch();
            processoRepo.deleteAllInBatch();
            alertaUsuarioRepo.deleteAllInBatch();
            alertaRepo.deleteAllInBatch();
            usuarioPerfilRepo.deleteAllInBatch();
            usuarioRepo.deleteAllInBatch();
            unidadeRepo.deleteAllInBatch();

            entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
            return null;
        });

        unidade = new Unidade();
        unidade.setSigla("ADM_SUB_CRUD");
        unidade.setNome("Unidade Sub Crud");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade = unidadeRepo.save(unidade);

        usuarioAdmin = new Usuario();
        usuarioAdmin.setTituloEleitoral("13131313131");
        usuarioAdmin.setNome("Admin Sub Crud");
        usuarioAdmin.setUnidadeLotacao(unidade);
        usuarioAdmin = usuarioRepo.save(usuarioAdmin);

        transactionTemplate.execute(status -> {
            entityManager.createNativeQuery("INSERT INTO sgc.vw_usuario_perfil_unidade (usuario_titulo, unidade_codigo, perfil) VALUES (?, ?, ?)")
                    .setParameter(1, usuarioAdmin.getTituloEleitoral())
                    .setParameter(2, unidade.getCodigo())
                    .setParameter(3, "ADMIN")
                    .executeUpdate();
            return null;
        });

        processo = new Processo();
        processo.setDescricao("Processo Sub Crud");
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
    @DisplayName("Deve obter detalhes do subprocesso")
    void deveObterDetalhes() {
        String token = gerarToken(usuarioAdmin.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/subprocessos/{codigo}", subprocesso.getCodigo())
        .then()
            .statusCode(200)
            .body("codigo", equalTo(subprocesso.getCodigo().intValue()));
    }

    @Test
    @DisplayName("Deve criar subprocesso")
    void deveCriarSubprocesso() {
        String token = gerarToken(usuarioAdmin.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        // Create another unit for the new subprocess
        Unidade unidade2 = new Unidade();
        unidade2.setSigla("UNIT_SUB_NEW");
        unidade2.setNome("Unit New Sub");
        unidade2.setTipo(TipoUnidade.OPERACIONAL);
        unidade2 = unidadeRepo.save(unidade2);

        CriarSubprocessoRequest req = CriarSubprocessoRequest.builder()
                .codProcesso(processo.getCodigo())
                .codUnidade(unidade2.getCodigo())
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                .build();

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/api/subprocessos")
        .then()
            .statusCode(201)
            .body("unidade.sigla", equalTo("UNIT_SUB_NEW"));
    }
}
