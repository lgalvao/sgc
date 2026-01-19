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
import sgc.subprocesso.model.*;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class SubprocessoCadastroControllerRestAssuredTest extends BaseRestAssuredTest {

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
    private MovimentacaoRepo movimentacaoRepo;

    @Autowired
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @Autowired
    private AnaliseRepo analiseRepo;

    @Autowired
    private AlertaRepo alertaRepo;

    @Autowired
    private AlertaUsuarioRepo alertaUsuarioRepo;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Usuario usuarioAdmin;
    private Unidade unidade;
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
        unidade.setSigla("ADM_SUB_CAD");
        unidade.setNome("Unidade Sub Cad");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade = unidadeRepo.save(unidade);

        usuarioAdmin = new Usuario();
        usuarioAdmin.setTituloEleitoral("12121212121");
        usuarioAdmin.setNome("Admin Sub Cad");
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

        Processo processo = new Processo();
        processo.setDescricao("Processo Sub Cad");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDataCriacao(LocalDateTime.now());
        processo = processoRepo.save(processo);

        subprocesso = new Subprocesso();
        subprocesso.setProcesso(processo);
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocesso = subprocessoRepo.save(subprocesso);

        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapa = mapaRepo.save(mapa);

        subprocesso.setMapa(mapa);
        subprocesso = subprocessoRepo.save(subprocesso);
    }

    @Test
    @DisplayName("Deve obter histórico de cadastro")
    void deveObterHistoricoCadastro() {
        String token = gerarToken(usuarioAdmin.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/subprocessos/{codigo}/historico-cadastro", subprocesso.getCodigo())
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("Deve obter cadastro")
    void deveObterCadastro() {
        String token = gerarToken(usuarioAdmin.getTituloEleitoral(), Perfil.ADMIN, unidade.getCodigo());

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/subprocessos/{codigo}/cadastro", subprocesso.getCodigo())
        .then()
            .statusCode(200)
            .body("subprocessoCodigo", equalTo(subprocesso.getCodigo().intValue()));
    }
}
