package sgc.conhecimento.dto;

import org.junit.jupiter.api.Test;
import sgc.atividade.dto.ConhecimentoDto;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConhecimentoDtoTest {
    @Test
    void testConhecimentoDtoConstructorAndGettersSetters() {
        ConhecimentoDto dto = new ConhecimentoDto(1L, 100L, "Test Description");

        assertEquals(1L, dto.getCodigo());
        assertEquals(100L, dto.getAtividadeCodigo());
        assertEquals("Test Description", dto.getDescricao());

        dto = new ConhecimentoDto(2L, 200L, "New Description");

        assertEquals(2L, dto.getCodigo());
        assertEquals(200L, dto.getAtividadeCodigo());
        assertEquals("New Description", dto.getDescricao());
    }
}