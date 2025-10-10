package sgc.sgrh.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UsuarioDtoTest {
    @Test
    void testUsuarioDtoConstructor() {
        // Test record constructor
        UsuarioDto dto = new UsuarioDto("12345678901", "João Silva", "joao.silva@tre-pe.jus.br", "MAT001", "Analista Judiciário");

        // Test getters (record provides accessors)
        assertEquals("12345678901", dto.titulo());
        assertEquals("João Silva", dto.nome());
        assertEquals("joao.silva@tre-pe.jus.br", dto.email());
        assertEquals("MAT001", dto.matricula());
        assertEquals("Analista Judiciário", dto.cargo());
    }
}