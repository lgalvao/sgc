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

    @Autowired private SgrhService sgrhService;

    // Data from e2e/setup/seed.sql which seems to be the one actually loaded
    private static final String TITULO_ADMIN = "111111";
    private static final String EMAIL_ADMIN = "admin_sedoc_e_chefe_sedoc@tre-pe.jus.br";
    private static final String NOME_ADMIN = "ADMIN_SEDOC_E_CHEFE_SEDOC";
    private static final Long COD_UNIT_SEC1 = 2L;
    private static final String NOME_UNIT_SEC1 = "Secretaria 1";
    private static final String TITULO_JOHN_LENNON = "202020";

    @Test
    void testBuscarUsuarioPorTitulo() {
        Optional<UsuarioDto> result = sgrhService.buscarUsuarioPorTitulo(TITULO_ADMIN);

        assertTrue(result.isPresent());
        assertEquals(TITULO_ADMIN, result.get().getTitulo());
        assertEquals(NOME_ADMIN, result.get().getNome());
    }

    @Test
    void testBuscarUsuarioPorEmail() {
        Optional<UsuarioDto> result = sgrhService.buscarUsuarioPorEmail(EMAIL_ADMIN);

        assertTrue(result.isPresent());
        assertEquals(TITULO_ADMIN, result.get().getTitulo());
        assertEquals(EMAIL_ADMIN, result.get().getEmail());
    }

    @Test
    void testBuscarUsuariosAtivos() {
        List<UsuarioDto> result = sgrhService.buscarUsuariosAtivos();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        // At least user 111111, 202020, etc.
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
        // Unit 2 has children: 3, 4, 5, 9 (according to seed.sql)
        List<UnidadeDto> result = sgrhService.buscarSubunidades(COD_UNIT_SEC1);

        assertNotNull(result);
        // We expect at least these 4
        assertTrue(result.size() >= 4);
        for (UnidadeDto unidade : result) {
            assertEquals(COD_UNIT_SEC1, unidade.getCodigoPai());
        }
    }

    @Test
    void testConstruirArvoreHierarquica() {
        List<UnidadeDto> result = sgrhService.construirArvoreHierarquica();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        // Roots in seed.sql: 1 (SEDOC)
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
        // Unit 2 has User 202020 as Chefe
        Optional<ResponsavelDto> result = sgrhService.buscarResponsavelUnidade(2L);

        assertTrue(result.isPresent());
        assertEquals(2L, result.get().getUnidadeCodigo());
        assertEquals(TITULO_JOHN_LENNON, result.get().getTitularTitulo());
    }

    @Test
    void testBuscarUnidadesOndeEhResponsavel() {
        // User 202020 is Chefe of Unit 2
        List<Long> result = sgrhService.buscarUnidadesOndeEhResponsavel(TITULO_JOHN_LENNON);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains(2L));
    }

    @Test
    void testBuscarPerfisUsuario() {
        // User 202020 has CHEFE on 2
        List<PerfilDto> result = sgrhService.buscarPerfisUsuario(TITULO_JOHN_LENNON);

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
        // User 202020 has CHEFE on 2
        boolean result = sgrhService.usuarioTemPerfil(TITULO_JOHN_LENNON, "CHEFE", 2L);
        assertTrue(result);

        boolean result2 = sgrhService.usuarioTemPerfil(TITULO_JOHN_LENNON, "ADMIN", 2L);
        assertFalse(result2);

        // User 111111 has ADMIN on 1
        boolean result3 = sgrhService.usuarioTemPerfil(TITULO_ADMIN, "ADMIN", 1L);
        assertTrue(result3);
    }

    @Test
    void testBuscarUnidadesPorPerfil() {
        // User 111111 has ADMIN on 1
        List<Long> adminUnits = sgrhService.buscarUnidadesPorPerfil(TITULO_ADMIN, "ADMIN");
        assertTrue(adminUnits.contains(1L));
    }

    @Test
    void testBuscarResponsaveisUnidades() {
        // Unit 2 (User 202020 is CHEFE)
        // Unit 1 (User 111111 is CHEFE)
        List<Long> unidades = List.of(2L, 1L);
        Map<Long, ResponsavelDto> result = sgrhService.buscarResponsaveisUnidades(unidades);

        assertNotNull(result);
        assertTrue(result.containsKey(2L));
        assertTrue(result.containsKey(1L));

        assertEquals(TITULO_JOHN_LENNON, result.get(2L).getTitularTitulo());
        assertEquals(TITULO_ADMIN, result.get(1L).getTitularTitulo());
    }

    @Test
    void testBuscarUsuariosPorTitulos() {
        List<String> titulos = List.of(TITULO_JOHN_LENNON, TITULO_ADMIN);
        Map<String, UsuarioDto> result = sgrhService.buscarUsuariosPorTitulos(titulos);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(TITULO_JOHN_LENNON));
        assertTrue(result.containsKey(TITULO_ADMIN));
    }
}