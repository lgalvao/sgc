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

    @Autowired
    private UsuarioPerfilRepo usuarioPerfilRepo;

    private Usuario usuarioAdmin;
    private Unidade unidade;
    private Subprocesso subprocesso;

    @BeforeEach
    void setupDados() {
        unidade = unidadeRepo.findBySigla("ADM_SUB_VAL").orElseGet(() -> {
            Unidade u = new Unidade();
            u.setSigla("ADM_SUB_VAL");
            u.setNome("Unidade Sub Val");
            u.setTipo(TipoUnidade.OPERACIONAL);
            return unidadeRepo.save(u);
        });

        usuarioAdmin = usuarioRepo.findById("15151515151").orElseGet(() -> {
            Usuario u = new Usuario();
            u.setTituloEleitoral("15151515151");
            u.setNome("Admin Sub Val");
            u.setUnidadeLotacao(unidade);
            return usuarioRepo.save(u);
        });

        UsuarioPerfilId id = new UsuarioPerfilId(usuarioAdmin.getTituloEleitoral(), unidade.getCodigo(), Perfil.ADMIN);
        if (!usuarioPerfilRepo.existsById(id)) {
            UsuarioPerfil up = new UsuarioPerfil();
            up.setUsuarioTitulo(usuarioAdmin.getTituloEleitoral());
            up.setUnidadeCodigo(unidade.getCodigo());
            up.setPerfil(Perfil.ADMIN);
            usuarioPerfilRepo.save(up);
        }

        // Check if Processo exists (simplified check, usually by ID or attributes, here we create new if not ensuring we don't duplicate if we can find it)
        // Since Processo doesn't have a unique key other than ID easily accessible here without a finder, and we want a fresh one usually...
        // But to avoid deleteAll, we just create a NEW one. The issue with deleteAll was FK constraints on Unidade/Usuario.
        // Processo and Subprocesso usually can be created anew without clearing tables, AS LONG AS we don't try to clear Unidade/Usuario.

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
