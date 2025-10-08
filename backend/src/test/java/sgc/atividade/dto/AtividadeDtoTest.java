package sgc.atividade.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AtividadeDtoTest {

    @Test
    void testAtividadeDtoConstructorAndGettersSetters() {
        // Test constructor with parameters
        AtividadeDto dto = new AtividadeDto(1L, 100L, "Test Description");

        // Test getters
        assertEquals(1L, dto.getCodigo());
        assertEquals(100L, dto.getMapaCodigo());
        assertEquals("Test Description", dto.getDescricao());

        // Test setters
        dto.setCodigo(2L);
        dto.setMapaCodigo(200L);
        dto.setDescricao("New Description");

        assertEquals(2L, dto.getCodigo());
        assertEquals(200L, dto.getMapaCodigo());
        assertEquals("New Description", dto.getDescricao());
    }

    @Test
    void testAtividadeDtoNoArgsConstructor() {
        // Test no-args constructor
        AtividadeDto dto = new AtividadeDto();

        // Initially should be null/0
        assertNull(dto.getCodigo());
        assertNull(dto.getMapaCodigo());
        assertNull(dto.getDescricao());

        // Test setting values
        dto.setCodigo(1L);
        dto.setMapaCodigo(100L);
        dto.setDescricao("Test Description");

        assertEquals(1L, dto.getCodigo());
        assertEquals(100L, dto.getMapaCodigo());
        assertEquals("Test Description", dto.getDescricao());
    }
}