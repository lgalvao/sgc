package sgc.sgrh;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.sgrh.dto.PerfilDto;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.dto.UsuarioDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SgrhServiceTest {
    private static final String TITULO = "123456789012";
    private SgrhService sgrhService;

    @BeforeEach
    void setUp() {
        sgrhService = new SgrhService();
    }

    @Test
    void testBuscarUsuarioPorTitulo() {
        Optional<UsuarioDto> result = sgrhService.buscarUsuarioPorTitulo(TITULO);

        assertTrue(result.isPresent());
        assertEquals(TITULO, result.get().titulo());
        assertTrue(result.get().nome().contains("Usuário Mock"));
    }

    @Test
    void testBuscarUsuarioPorEmail() {
        String email = "joao.silva@tre-pe.jus.br";
        Optional<UsuarioDto> result = sgrhService.buscarUsuarioPorEmail(email);

        assertTrue(result.isPresent());
        assertEquals("joao.silva", result.get().titulo()); // extracted from email (before @)
        assertEquals(email, result.get().email());
    }

    @Test
    void testBuscarUsuariosAtivos() {
        List<UsuarioDto> result = sgrhService.buscarUsuariosAtivos();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(3, result.size());
        
        UsuarioDto primeiro = result.getFirst();
        assertEquals(TITULO, primeiro.titulo());
        assertEquals("joao.silva@tre-pe.jus.br", primeiro.email());
    }

    @Test
    void testBuscarUnidadePorCodigo() {
        Optional<UnidadeDto> result = sgrhService.buscarUnidadePorCodigo(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().codigo());
        assertEquals("SEDOC - Secretaria de Documentação", result.get().nome());
    }

    @Test
    void testBuscarUnidadesAtivas() {
        List<UnidadeDto> result = sgrhService.buscarUnidadesAtivas();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.size() >= 6); // From the mock data in the implementation
    }

    @Test
    void testBuscarSubunidades() {
        List<UnidadeDto> result = sgrhService.buscarSubunidades(1L); // SEDOC

        assertNotNull(result);
        // Should return COP and CGC (the direct children of SEDOC)
        assertTrue(result.size() >= 2);
        for (UnidadeDto unidade : result) {
            assertEquals(1L, unidade.codigoPai()); // All should have SEDOC as parent
        }
    }

    @Test
    void testConstruirArvoreHierarquica() {
        List<UnidadeDto> result = sgrhService.construirArvoreHierarquica();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        // Should return root units (with no parent)
        for (UnidadeDto unidade : result) {
            assertNull(unidade.codigoPai()); // All in the list should be roots
        }
    }

    @Test
    void testBuscarResponsavelUnidade() {
        Optional<ResponsavelDto> result = sgrhService.buscarResponsavelUnidade(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().unidadeCodigo());
        assertEquals(TITULO, result.get().titularTitulo());
        assertEquals("987654321098", result.get().substitutoTitulo());
    }

    @Test
    void testBuscarUnidadesOndeEhResponsavel() {
        List<Long> result = sgrhService.buscarUnidadesOndeEhResponsavel(TITULO);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains(1L)); // Should contain at least unit 1
    }

    @Test
    void testBuscarPerfisUsuario() {
        List<PerfilDto> result = sgrhService.buscarPerfisUsuario(TITULO);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        // Check if it contains expected profile
        assertTrue(result.stream().anyMatch(p -> p.usuarioTitulo().equals(TITULO)));
    }

    @Test
    void testUsuarioTemPerfil() {
        boolean result = sgrhService.usuarioTemPerfil(TITULO, "ADMIN", 1L);
        assertTrue(result); // According to the mock implementation

        boolean result2 = sgrhService.usuarioTemPerfil(TITULO, "GESTOR", 1L);
        assertFalse(result2); // According to the mock implementation
    }

    @Test
    void testBuscarUnidadesPorPerfil() {
        List<Long> adminUnits = sgrhService.buscarUnidadesPorPerfil(TITULO, "ADMIN");
        assertTrue(adminUnits.contains(1L));
        
        List<Long> gestorUnits = sgrhService.buscarUnidadesPorPerfil(TITULO, "GESTOR");
        assertTrue(gestorUnits.contains(2L) || gestorUnits.contains(3L));
    }

    @Test
    void testBuscarResponsaveisUnidades() {
        List<Long> unidades = List.of(1L, 2L);
        Map<Long, ResponsavelDto> result = sgrhService.buscarResponsaveisUnidades(unidades);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(1L));
        assertTrue(result.containsKey(2L));
    }

    @Test
    void testBuscarUsuariosPorTitulos() {
        List<String> titulos = List.of(TITULO, "987654321098");
        Map<String, UsuarioDto> result = sgrhService.buscarUsuariosPorTitulos(titulos);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(TITULO));
        assertTrue(result.containsKey("987654321098"));
    }
}
