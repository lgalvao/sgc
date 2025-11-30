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
    
    @Autowired
    private SgrhService sgrhService;

    private static final String TITULO_JOAO = "1";
    private static final String EMAIL_JOAO = "ana.souza@tre-pe.jus.br";
    private static final Long COD_UNIT_STIC = 2L;

    @Test
    void testBuscarUsuarioPorTitulo() {
        Optional<UsuarioDto> result = sgrhService.buscarUsuarioPorTitulo(TITULO_JOAO);

        assertTrue(result.isPresent());
        assertEquals(TITULO_JOAO, result.get().getTitulo());
        assertEquals("Ana Paula Souza", result.get().getNome());
    }

    @Test
    void testBuscarUsuarioPorEmail() {
        Optional<UsuarioDto> result = sgrhService.buscarUsuarioPorEmail(EMAIL_JOAO);

        assertTrue(result.isPresent());
        assertEquals(TITULO_JOAO, result.get().getTitulo());
        assertEquals(EMAIL_JOAO, result.get().getEmail());
    }

    @Test
    void testBuscarUsuariosAtivos() {
        List<UsuarioDto> result = sgrhService.buscarUsuariosAtivos();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.size() > 5); 
    }

    @Test
    void testBuscarUnidadePorCodigo() {
        Optional<UnidadeDto> result = sgrhService.buscarUnidadePorCodigo(COD_UNIT_STIC);

        assertTrue(result.isPresent());
        assertEquals(COD_UNIT_STIC, result.get().getCodigo());
        assertEquals("Secretaria de Informática e Comunicações", result.get().getNome());
    }

    @Test
    void testBuscarUnidadesAtivas() {
        List<UnidadeDto> result = sgrhService.buscarUnidadesAtivas();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.size() > 10);
    }

    @Test
    void testBuscarSubunidades() {
        // STIC (2) has children: COAD(3), COSIS(6), COSINF(7), COJUR(14), SEDOC(15) + test units (900-904)
        List<UnidadeDto> result = sgrhService.buscarSubunidades(COD_UNIT_STIC);

        assertNotNull(result);
        assertTrue(result.size() >= 5);
        for (UnidadeDto unidade : result) {
            assertEquals(COD_UNIT_STIC, unidade.getCodigoPai());
        }
    }

    @Test
    void testConstruirArvoreHierarquica() {
        List<UnidadeDto> result = sgrhService.construirArvoreHierarquica();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        // Roots in data.sql: 1 (TRE), 2 (STIC), 100 (ADMIN), 200 (SGP)
        assertTrue(result.stream().anyMatch(u -> u.getCodigo().equals(1L)));
        assertTrue(result.stream().anyMatch(u -> u.getCodigo().equals(2L)));
        
        for (UnidadeDto unidade : result) {
            assertNull(unidade.getCodigoPai());
        }
    }

    @Test
    void testBuscarResponsavelUnidade() {
        // Unit 2 has User 777 as Chefe
        Optional<ResponsavelDto> result = sgrhService.buscarResponsavelUnidade(2L);

        assertTrue(result.isPresent());
        assertEquals(2L, result.get().getUnidadeCodigo());
        assertEquals("777", result.get().getTitularTitulo()); 
    }

    @Test
    void testBuscarUnidadesOndeEhResponsavel() {
        // User 777 is Chefe of Unit 2
        List<Long> result = sgrhService.buscarUnidadesOndeEhResponsavel("777");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains(2L));
    }

    @Test
    void testBuscarPerfisUsuario() {
        // User 777 has CHEFE on 2
        List<PerfilDto> result = sgrhService.buscarPerfisUsuario("777");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(p -> p.getPerfil().equals("CHEFE") && p.getUnidadeCodigo().equals(2L)));
    }

    @Test
    void testUsuarioTemPerfil() {
        // User 777 has CHEFE on 2
        boolean result = sgrhService.usuarioTemPerfil("777", "CHEFE", 2L);
        assertTrue(result);

        boolean result2 = sgrhService.usuarioTemPerfil("777", "ADMIN", 2L);
        assertFalse(result2);
        
        // User 6 has ADMIN on 2
        boolean result3 = sgrhService.usuarioTemPerfil("6", "ADMIN", 2L);
        assertTrue(result3);
    }

    @Test
    void testBuscarUnidadesPorPerfil() {
        // User 6 has ADMIN on 2
        List<Long> adminUnits = sgrhService.buscarUnidadesPorPerfil("6", "ADMIN");
        assertTrue(adminUnits.contains(2L));
    }

    @Test
    void testBuscarResponsaveisUnidades() {
        // Unit 2 (User 777 is CHEFE)
        // Unit 8 (User 3 is CHEFE)
        List<Long> unidades = List.of(2L, 8L);
        Map<Long, ResponsavelDto> result = sgrhService.buscarResponsaveisUnidades(unidades);

        assertNotNull(result);
        assertTrue(result.containsKey(2L));
        assertTrue(result.containsKey(8L));
        
        assertEquals("777", result.get(2L).getTitularTitulo());
        assertEquals("3", result.get(8L).getTitularTitulo());
    }

    @Test
    void testBuscarUsuariosPorTitulos() {
        List<String> titulos = List.of("777", "3");
        Map<String, UsuarioDto> result = sgrhService.buscarUsuariosPorTitulos(titulos);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("777"));
        assertTrue(result.containsKey("3"));
    }
}
