package sgc.processo.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProcessoDtoTest {
    @Test
    void testProcessoDtoConstructorAndAccessors() {
        var now = LocalDateTime.now();
        var limitDate = LocalDate.now();

        var dto = new ProcessoDto(
            1L,
            now,
            now,
            limitDate,
            "Test Description",
            "ATIVO",
            "TIPO_A"
        );

        assertEquals(1L, dto.codigo());
        assertEquals(now, dto.dataCriacao());
        assertEquals(now, dto.dataFinalizacao());
        assertEquals(limitDate, dto.dataLimite());
        assertEquals("Test Description", dto.descricao());
        assertEquals("ATIVO", dto.situacao());
        assertEquals("TIPO_A", dto.tipo());
    }
}