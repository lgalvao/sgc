package sgc.sgrh.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UsuarioDtoTest {
    @Test
    void testUsuarioDtoBuilder() {
        UsuarioDto dto = UsuarioDto.builder()
                .codigo("12345678901")
                .tituloEleitoral("12345678901")
                .nome("João Silva")
                .email("joao.silva@tre-pe.jus.br")
                .matricula("MAT001")
                .unidadeCodigo(10L)
                .build();

        assertEquals("12345678901", dto.getCodigo());
        assertEquals("12345678901", dto.getTituloEleitoral());
        assertEquals("João Silva", dto.getNome());
        assertEquals("joao.silva@tre-pe.jus.br", dto.getEmail());
        assertEquals("MAT001", dto.getMatricula());
        assertEquals(10L, dto.getUnidadeCodigo());
    }
}
