package sgc.e2e;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.jdbc.core.*;
import org.springframework.test.context.*;
import org.springframework.transaction.annotation.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.service.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

@Tag("integration")
@SpringBootTest
@Transactional
@ActiveProfiles("e2e")
@DisplayName("Sincronização E2E de views de atribuição temporária")
class AtribuicaoTemporariaViewsE2eAspectIntegrationTest {
    @Autowired
    private ResponsavelUnidadeService responsavelUnidadeService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Deve simular VW_RESPONSABILIDADE e VW_USUARIO_PERFIL_UNIDADE ao criar atribuição temporária")
    void deveSimularViewsAoCriarAtribuicaoTemporaria() {
        prepararMassaMinima();

        responsavelUnidadeService.criarAtribuicaoTemporaria(
                3L,
                new CriarAtribuicaoRequest(
                        "232323",
                        LocalDate.now(),
                        LocalDate.now().plusDays(10),
                        "Cobertura de férias"
                )
        );

        String responsavelAtual = jdbcTemplate.queryForObject(
                "SELECT usuario_titulo FROM sgc.vw_responsabilidade WHERE unidade_codigo = ?",
                String.class,
                3L
        );
        String tipoResponsabilidade = jdbcTemplate.queryForObject(
                "SELECT tipo FROM sgc.vw_responsabilidade WHERE unidade_codigo = ?",
                String.class,
                3L
        );
        Integer perfisChefeNovoResponsavel = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM sgc.vw_usuario_perfil_unidade
                WHERE usuario_titulo = ?
                  AND unidade_codigo = ?
                  AND perfil = 'CHEFE'
                """,
                Integer.class,
                "232323",
                3L
        );
        Integer perfisChefeTitularAnterior = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM sgc.vw_usuario_perfil_unidade
                WHERE usuario_titulo = ?
                  AND unidade_codigo = ?
                  AND perfil = 'CHEFE'
                """,
                Integer.class,
                "555555",
                3L
        );

        assertThat(responsavelAtual).isEqualTo("232323");
        assertThat(tipoResponsabilidade).isEqualTo("ATRIBUICAO_TEMPORARIA");
        assertThat(perfisChefeNovoResponsavel).isEqualTo(1);
        assertThat(perfisChefeTitularAnterior).isZero();
    }

    private void prepararMassaMinima() {
        jdbcTemplate.update("""
                MERGE INTO sgc.vw_unidade
                (codigo, nome, sigla, tipo, situacao, unidade_superior_codigo, titulo_titular, matricula_titular, data_inicio_titularidade)
                KEY(codigo)
                VALUES (3, 'Assessoria 11', 'ASSESSORIA_11', 'OPERACIONAL', 'ATIVA', 2, '555555', '00555555', CURRENT_TIMESTAMP)
                """);

        jdbcTemplate.update("""
                MERGE INTO sgc.vw_usuario
                (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
                KEY(titulo)
                VALUES ('555555', '00555555', 'David Bowie', 'david.bowie@tre-pe.jus.br', '2003', 3, 3)
                """);

        jdbcTemplate.update("""
                MERGE INTO sgc.vw_usuario
                (titulo, matricula, nome, email, ramal, unidade_lot_codigo, unidade_comp_codigo)
                KEY(titulo)
                VALUES ('232323', '00232323', 'Bon Jovi', 'bon.jovi@tre-pe.jus.br', '2023', 3, 3)
                """);

        jdbcTemplate.update("""
                MERGE INTO sgc.vw_responsabilidade
                (unidade_codigo, usuario_titulo, usuario_matricula, tipo, data_inicio, data_fim)
                KEY(unidade_codigo)
                VALUES (3, '555555', '00555555', 'TITULAR', CURRENT_TIMESTAMP, NULL)
                """);

        jdbcTemplate.update("""
                MERGE INTO sgc.vw_usuario_perfil_unidade
                (usuario_titulo, unidade_codigo, perfil)
                KEY(usuario_titulo, unidade_codigo, perfil)
                VALUES ('555555', 3, 'CHEFE')
                """);

        jdbcTemplate.update("""
                DELETE FROM sgc.vw_usuario_perfil_unidade
                WHERE usuario_titulo = '232323'
                  AND unidade_codigo = 3
                  AND perfil IN ('CHEFE', 'GESTOR')
                """);
    }
}
