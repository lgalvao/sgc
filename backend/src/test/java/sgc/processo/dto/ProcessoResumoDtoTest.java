package sgc.processo.dto;

import org.junit.jupiter.api.Test;
import sgc.comum.enums.SituacaoProcesso;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProcessoResumoDtoTest {
    @Test
    void testProcessoResumoDtoConstructorAndAccessors() {
        var now = LocalDateTime.now();
        var limitDate = LocalDate.now();
        var dto = new ProcessoResumoDto(
            1L,
            "Test Description",
            SituacaoProcesso.EM_ANDAMENTO,
            "TIPO_A",
            limitDate,
            now,
            10L,
            "Test Unit"
        );

        assertEquals(1L, dto.codigo());
        assertEquals("Test Description", dto.descricao());
        assertEquals(SituacaoProcesso.EM_ANDAMENTO, dto.situacao());
        assertEquals("TIPO_A", dto.tipo());
        assertEquals(limitDate, dto.dataLimite());
        assertEquals(now, dto.dataCriacao());
        assertEquals(10L, dto.unidadeCodigo());
        assertEquals("Test Unit", dto.unidadeNome());
    }
}
