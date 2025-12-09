package sgc.atividade.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AtividadeDtoTest {

    @Test
    void testAtividadeDtoConstructorAndGetters() {
        // Test constructor with parameters
        AtividadeDto dto = new AtividadeDto(1L, 100L, "Test Description");

        // Test getters (accessors)
        assertEquals(1L, dto.getCodigo());
        assertEquals(100L, dto.getMapaCodigo());
        assertEquals("Test Description", dto.getDescricao());
    }
}
