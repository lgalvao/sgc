package sgc.sgrh.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PerfilDtoTest {
    @Test
    void testPerfilDtoConstructor() {
        // Test record constructor
        PerfilDto dto =
                new PerfilDto("12345678901", 1L, "SEDOC - Secretaria de Documentação", "ADMIN");

        // Test getters (record provides accessors)
        assertEquals("12345678901", dto.getUsuarioTitulo());
        assertEquals(1L, dto.getUnidadeCodigo());
        assertEquals("SEDOC - Secretaria de Documentação", dto.getUnidadeNome());
        assertEquals("ADMIN", dto.getPerfil());
    }
}
