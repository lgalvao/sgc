package sgc.e2e;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.jdbc.core.*;
import org.springframework.test.context.*;
import org.springframework.transaction.annotation.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.service.*;

import java.sql.Timestamp;
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
                "SELECT usuario_titulo FROM sgc.VW_RESPONSABILIDADE WHERE unidade_codigo = ?",
                String.class,
                3L
        );
        String tipoResponsabilidade = jdbcTemplate.queryForObject(
                "SELECT tipo FROM sgc.VW_RESPONSABILIDADE WHERE unidade_codigo = ?",
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

    @Test
    @DisplayName("Deve ressincronizar views ao atualizar atribuição temporária")
    void deveRessincronizarViewsAoAtualizarAtribuicaoTemporaria() {
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

        Long codigoAtribuicao = jdbcTemplate.queryForObject(
                "SELECT codigo FROM sgc.atribuicao_temporaria WHERE unidade_codigo = ?",
                Long.class,
                3L
        );

        responsavelUnidadeService.atualizarAtribuicaoTemporaria(
                3L,
                codigoAtribuicao,
                new CriarAtribuicaoRequest(
                        "232323",
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(20),
                        "Atualizada"
                )
        );

        Timestamp dataFim = jdbcTemplate.queryForObject(
                "SELECT data_fim FROM sgc.VW_RESPONSABILIDADE WHERE unidade_codigo = ?",
                Timestamp.class,
                3L
        );

        assertThat(dataFim.toLocalDateTime()).isEqualTo(LocalDate.now().plusDays(20).atTime(23, 59, 59));
    }

    @Test
    @DisplayName("Deve restaurar titular e perfis ao remover atribuição temporária")
    void deveRestaurarTitularEPerfisAoRemoverAtribuicaoTemporaria() {
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

        Long codigoAtribuicao = jdbcTemplate.queryForObject(
                "SELECT codigo FROM sgc.atribuicao_temporaria WHERE unidade_codigo = ?",
                Long.class,
                3L
        );

        responsavelUnidadeService.removerAtribuicaoTemporaria(3L, codigoAtribuicao);

        String responsavelAtual = jdbcTemplate.queryForObject(
                "SELECT usuario_titulo FROM sgc.VW_RESPONSABILIDADE WHERE unidade_codigo = ?",
                String.class,
                3L
        );
        String tipoResponsabilidade = jdbcTemplate.queryForObject(
                "SELECT tipo FROM sgc.VW_RESPONSABILIDADE WHERE unidade_codigo = ?",
                String.class,
                3L
        );
        Integer perfisChefeTitular = jdbcTemplate.queryForObject(
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
        Integer perfisChefeTemporario = jdbcTemplate.queryForObject(
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

        assertThat(responsavelAtual).isEqualTo("555555");
        assertThat(tipoResponsabilidade).isEqualTo("TITULAR");
        assertThat(perfisChefeTitular).isEqualTo(1);
        assertThat(perfisChefeTemporario).isZero();
    }

    @Test
    @DisplayName("Deve sincronizar perfil GESTOR quando a unidade for intermediária")
    void deveSincronizarPerfilGestorQuandoAUnidadeForIntermediaria() {
        prepararMassaMinima();
        jdbcTemplate.update("UPDATE sgc.vw_unidade SET tipo = 'INTERMEDIARIA' WHERE codigo = ?", 3L);

        responsavelUnidadeService.criarAtribuicaoTemporaria(
                3L,
                new CriarAtribuicaoRequest(
                        "232323",
                        LocalDate.now(),
                        LocalDate.now().plusDays(10),
                        "Cobertura de férias"
                )
        );

        Integer perfisGestor = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM sgc.vw_usuario_perfil_unidade
                        WHERE usuario_titulo = ?
                          AND unidade_codigo = ?
                          AND perfil = 'GESTOR'
                        """,
                Integer.class,
                "232323",
                3L
        );
        Integer perfisChefe = jdbcTemplate.queryForObject(
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

        assertThat(perfisGestor).isEqualTo(1);
        assertThat(perfisChefe).isZero();
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException quando valor obrigatório vier nulo")
    void deveLancarIllegalStateExceptionQuandoValorObrigatorioVierNulo() throws Exception {
        JdbcTemplate jdbcTemplateMock = org.mockito.Mockito.mock(JdbcTemplate.class);
        org.mockito.Mockito.when(jdbcTemplateMock.queryForObject("SELECT valor", String.class, 3L)).thenReturn(null);
        AtribuicaoTemporariaViewsE2eAspect aspect = new AtribuicaoTemporariaViewsE2eAspect(jdbcTemplateMock);
        java.lang.reflect.Method metodo = AtribuicaoTemporariaViewsE2eAspect.class
                .getDeclaredMethod("buscarValorObrigatorio", String.class, Object[].class);
        metodo.setAccessible(true);

        assertThatThrownBy(() -> {
            try {
                metodo.invoke(aspect, "SELECT valor", (Object) new Object[]{3L});
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw e.getCause();
            }
        }).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Fixture E2E não encontrou valor obrigatório");
    }

    @Test
    @DisplayName("Não deve remover perfis antigos quando o título anterior for nulo")
    void naoDeveRemoverPerfisAntigosQuandoOTituloAnteriorForNulo() throws Exception {
        JdbcTemplate jdbcTemplateMock = org.mockito.Mockito.mock(JdbcTemplate.class);
        AtribuicaoTemporariaViewsE2eAspect aspect = new AtribuicaoTemporariaViewsE2eAspect(jdbcTemplateMock);
        java.lang.reflect.Method metodo = AtribuicaoTemporariaViewsE2eAspect.class
                .getDeclaredMethod("removerPerfisDeResponsabilidadeAnteriores", String.class, String.class, Long.class);
        metodo.setAccessible(true);

        metodo.invoke(aspect, null, "232323", 3L);

        org.mockito.Mockito.verifyNoInteractions(jdbcTemplateMock);
    }

    @Test
    @DisplayName("Não deve remover perfis do responsável anterior quando ele já for o titular")
    void naoDeveRemoverPerfisDoResponsavelAnteriorQuandoEleJaForOTitular() throws Exception {
        JdbcTemplate jdbcTemplateMock = org.mockito.Mockito.mock(JdbcTemplate.class);
        org.mockito.Mockito.when(jdbcTemplateMock.queryForObject("SELECT titulo_titular FROM sgc.vw_unidade WHERE codigo = ?", String.class, 3L))
                .thenReturn("555555");
        org.mockito.Mockito.when(jdbcTemplateMock.queryForObject("SELECT matricula_titular FROM sgc.vw_unidade WHERE codigo = ?", String.class, 3L))
                .thenReturn("00555555");
        org.mockito.Mockito.when(jdbcTemplateMock.queryForObject("SELECT tipo FROM sgc.vw_unidade WHERE codigo = ?", String.class, 3L))
                .thenReturn("OPERACIONAL");
        AtribuicaoTemporariaViewsE2eAspect aspect = new AtribuicaoTemporariaViewsE2eAspect(jdbcTemplateMock);
        java.lang.reflect.Method metodo = AtribuicaoTemporariaViewsE2eAspect.class
                .getDeclaredMethod("restaurarResponsabilidadeTitular", Long.class, String.class);
        metodo.setAccessible(true);

        metodo.invoke(aspect, 3L, "555555");

        org.mockito.Mockito.verify(jdbcTemplateMock, org.mockito.Mockito.times(1)).update(
                org.mockito.ArgumentMatchers.contains("DELETE FROM sgc.vw_usuario_perfil_unidade"),
                org.mockito.ArgumentMatchers.eq("555555"),
                org.mockito.ArgumentMatchers.eq(3L)
        );
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
                MERGE INTO sgc.VW_RESPONSABILIDADE
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
