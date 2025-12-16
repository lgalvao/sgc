package sgc.sgrh;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.sgrh.dto.PerfilDto;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.sgrh.service.SgrhService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SgrhServiceTest {

    // Data from backend/src/test/resources/data.sql
    private static final String TITULO_ADMIN = "111111111111";
    private static final String EMAIL_ADMIN = "admin.teste@tre-pe.jus.br";
    private static final String NOME_ADMIN = "Admin Teste";

    private static final Long COD_UNIT_SEC1 = 2L;
    private static final String NOME_UNIT_SEC1 = "Secretaria de Informática e Comunicações";

    private static final String TITULO_CHEFE_UNIT2 = "777"; // Chefe STIC Teste

    @Autowired
    private SgrhService sgrhService;

    @Test
    void testBuscarUsuarioPorTitulo() {
        Optional<UsuarioDto> result = sgrhService.buscarUsuarioPorTitulo(TITULO_ADMIN);

        assertTrue(result.isPresent());
        assertEquals(TITULO_ADMIN, result.get().getTituloEleitoral());
        assertEquals(NOME_ADMIN, result.get().getNome());
    }

    @Test
    void testBuscarUsuarioPorEmail() {
        Optional<UsuarioDto> result = sgrhService.buscarUsuarioPorEmail(EMAIL_ADMIN);

        assertTrue(result.isPresent());
        assertEquals(TITULO_ADMIN, result.get().getTituloEleitoral());
        assertEquals(EMAIL_ADMIN, result.get().getEmail());
    }

    @Test
    void testBuscarUsuariosAtivos() {
        List<UsuarioDto> result = sgrhService.buscarUsuariosAtivos();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.size() >= 2);
    }

    @Test
    void testBuscarUnidadePorCodigo() {
        Optional<UnidadeDto> result = sgrhService.buscarUnidadePorCodigo(COD_UNIT_SEC1);

        assertTrue(result.isPresent());
        assertEquals(COD_UNIT_SEC1, result.get().getCodigo());
        assertEquals(NOME_UNIT_SEC1, result.get().getNome());
    }

    @Test
    void testBuscarUnidadesAtivas() {
        List<UnidadeDto> result = sgrhService.buscarUnidadesAtivas();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.size() > 5);
    }

    @Test
    void testBuscarSubunidades() {
        // Unit 2 has children
        List<UnidadeDto> result = sgrhService.buscarSubunidades(COD_UNIT_SEC1);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (UnidadeDto unidade : result) {
            assertEquals(COD_UNIT_SEC1, unidade.getCodigoPai());
        }
    }

    @Test
    void testConstruirArvoreHierarquica() {
        List<UnidadeDto> result = sgrhService.construirArvoreHierarquica();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        // Roots in data.sql: 1 (TRE)
        assertTrue(result.stream().anyMatch(u -> u.getCodigo().equals(1L)));

        // Ensure roots have no parent
        for (UnidadeDto unidade : result) {
            if (unidade.getCodigo().equals(1L)) {
                assertNull(unidade.getCodigoPai());
            }
        }
    }

    @Test
    void testBuscarResponsavelUnidade() {
        // Unit 2 has User 777 as Chefe
        Optional<ResponsavelDto> result = sgrhService.buscarResponsavelUnidade(2L);

        assertTrue(result.isPresent());
        assertEquals(2L, result.get().getUnidadeCodigo());
        assertEquals(TITULO_CHEFE_UNIT2, result.get().getTitularTitulo());
    }

    @Test
    void testBuscarUnidadesOndeEhResponsavel() {
        // User 777 is Chefe of Unit 2
        List<Long> result = sgrhService.buscarUnidadesOndeEhResponsavel(TITULO_CHEFE_UNIT2);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains(2L));
    }

    @Test
    void testBuscarPerfisUsuario() {
        List<PerfilDto> result = sgrhService.buscarPerfisUsuario(TITULO_CHEFE_UNIT2);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(
                result.stream()
                        .anyMatch(
                                p ->
                                        p.getPerfil().equals("CHEFE")
                                                && p.getUnidadeCodigo().equals(2L)));
    }

    @Test
    void testUsuarioTemPerfil() {
        // User 777 has CHEFE on 2
        boolean result = sgrhService.usuarioTemPerfil(TITULO_CHEFE_UNIT2, "CHEFE", 2L);
        assertTrue(result);

        // User 111111111111 has ADMIN on 100
        boolean result3 = sgrhService.usuarioTemPerfil(TITULO_ADMIN, "ADMIN", 100L);
        assertTrue(result3);
    }

    @Test
    void testBuscarUnidadesPorPerfil() {
        // User 111111111111 has ADMIN on 100
        List<Long> adminUnits = sgrhService.buscarUnidadesPorPerfil(TITULO_ADMIN, "ADMIN");
        assertTrue(adminUnits.contains(100L));
    }

    @Test
    void testBuscarResponsaveisUnidades() {
        // Unit 2 (Chefe 777)
        // Unit 100 (Chefe 7) -> No, user 7 has no profile in data.sql (only titular reference in unidade)
        // Wait, does Unit 100 have a profile CHEFE?
        // INSERT INTO SGC.VW_UNIDADE ... VALUES ('100', 'ADMIN-UNIT', ... '7');
        // User 7 'Zeca Silva'.
        // Profiles:
        // No explicit profile for 7.
        // User 111111111111 has ADMIN on 100.
        // So search for CHEFE on 100 will likely return nothing unless 111111111111 is CHEFE?
        // 111111111111 is ADMIN.

        // Let's check who has CHEFE on 100.
        // Nobody in data.sql.
        // Unit 9 has CHEFE 333333333333.

        List<Long> unidades = List.of(2L, 9L);
        Map<Long, ResponsavelDto> result = sgrhService.buscarResponsaveisUnidades(unidades);

        assertNotNull(result);
        assertTrue(result.containsKey(2L));
        assertTrue(result.containsKey(9L));

        assertEquals(TITULO_CHEFE_UNIT2, result.get(2L).getTitularTitulo());
        assertEquals("333333333333", result.get(9L).getTitularTitulo());
    }

    @Test
    void testBuscarUsuariosPorTitulos() {
        List<String> titulos = List.of(TITULO_CHEFE_UNIT2, TITULO_ADMIN);
        Map<String, UsuarioDto> result = sgrhService.buscarUsuariosPorTitulos(titulos);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(TITULO_CHEFE_UNIT2));
        assertTrue(result.containsKey(TITULO_ADMIN));
    }
}
