package sgc.sgrh.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UsuarioDtoTest {
    @Test
    void testUsuarioDtoConstructor() {
        // Test record constructor
        UsuarioDto dto =
                new UsuarioDto(
                        "12345678901",
                        "João Silva",
                        "joao.silva@tre-pe.jus.br",
                        "MAT001",
                        "Analista Judiciário");

        // Test getters (record provides accessors)
        assertEquals("12345678901", dto.getTitulo());
        assertEquals("João Silva", dto.getNome());
        assertEquals("joao.silva@tre-pe.jus.br", dto.getEmail());
        assertEquals("MAT001", dto.getMatricula());
        assertEquals("Analista Judiciário", dto.getCargo());
    }
}
