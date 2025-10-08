package sgc.sgrh.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PerfilDtoTest {

    @Test
    void testPerfilDtoConstructor() {
        // Test record constructor
        PerfilDto dto = new PerfilDto("12345678901", 1L, "SEDOC - Secretaria de Documentação", "ADMIN");

        // Test getters (record provides accessors)
        assertEquals("12345678901", dto.usuarioTitulo());
        assertEquals(1L, dto.unidadeCodigo());
        assertEquals("SEDOC - Secretaria de Documentação", dto.unidadeNome());
        assertEquals("ADMIN", dto.perfil());
    }
}