package sgc.conhecimento.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConhecimentoDtoTest {

    @Test
    void testConhecimentoDtoConstructorAndGettersSetters() {
        // Test constructor with parameters
        ConhecimentoDto dto = new ConhecimentoDto(1L, 100L, "Test Description");

        // Test getters
        assertEquals(1L, dto.getCodigo());
        assertEquals(100L, dto.getAtividadeCodigo());
        assertEquals("Test Description", dto.getDescricao());

        // Test setters
        dto.setCodigo(2L);
        dto.setAtividadeCodigo(200L);
        dto.setDescricao("New Description");

        assertEquals(2L, dto.getCodigo());
        assertEquals(200L, dto.getAtividadeCodigo());
        assertEquals("New Description", dto.getDescricao());
    }

    @Test
    void testConhecimentoDtoNoArgsConstructor() {
        // Test no-args constructor
        ConhecimentoDto dto = new ConhecimentoDto();

        // Initially should be null/0
        assertNull(dto.getCodigo());
        assertNull(dto.getAtividadeCodigo());
        assertNull(dto.getDescricao());

        // Test setting values
        dto.setCodigo(1L);
        dto.setAtividadeCodigo(100L);
        dto.setDescricao("Test Description");

        assertEquals(1L, dto.getCodigo());
        assertEquals(100L, dto.getAtividadeCodigo());
        assertEquals("Test Description", dto.getDescricao());
    }
}