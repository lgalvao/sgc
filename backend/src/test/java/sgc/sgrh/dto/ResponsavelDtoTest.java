package sgc.sgrh.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResponsavelDtoTest {

    @Test
    void testResponsavelDtoConstructor() {
        // Test record constructor
        ResponsavelDto dto = new ResponsavelDto(1L, "12345678901", "João Silva", "98765432109", "Maria Santos");

        // Test getters (record provides accessors)
        assertEquals(1L, dto.unidadeCodigo());
        assertEquals("12345678901", dto.titularTitulo());
        assertEquals("João Silva", dto.titularNome());
        assertEquals("98765432109", dto.substitutoTitulo());
        assertEquals("Maria Santos", dto.substitutoNome());
    }
}