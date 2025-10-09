package sgc.conhecimento.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConhecimentoDtoTest {

    @Test
    void testConhecimentoDtoRecord() {
        // Test constructor and accessors
        Long codigo = 1L;
        Long atividadeCodigo = 100L;
        String descricao = "Test Description";

        ConhecimentoDto dto = new ConhecimentoDto(codigo, atividadeCodigo, descricao);

        assertEquals(codigo, dto.codigo());
        assertEquals(atividadeCodigo, dto.atividadeCodigo());
        assertEquals(descricao, dto.descricao());
    }
}