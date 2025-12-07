package sgc.sgrh.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ResponsavelDtoTest {
    @Test
    void testResponsavelDtoConstructor() {
        // Test record constructor
        ResponsavelDto dto =
                new ResponsavelDto(1L, "12345678901", "João Silva", "98765432109", "Maria Santos");

        // Test getters (record provides accessors)
        assertEquals(1L, dto.getUnidadeCodigo());
        assertEquals("12345678901", dto.getTitularTitulo());
        assertEquals("João Silva", dto.getTitularNome());
        assertEquals("98765432109", dto.getSubstitutoTitulo());
        assertEquals("Maria Santos", dto.getSubstitutoNome());
    }
}
