package sgc.processo.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProcessoDtoTest {

    @Test
    void testProcessoDtoRecord() {
        // Create test data
        Long codigo = 1L;
        LocalDateTime dataCriacao = LocalDateTime.now();
        LocalDateTime dataFinalizacao = LocalDateTime.now().plusDays(1);
        LocalDate dataLimite = LocalDate.now().plusDays(30);
        String descricao = "Test Description";
        String situacao = "ATIVO";
        String tipo = "TIPO_A";

        // Test constructor and accessors
        ProcessoDto dto = new ProcessoDto(
            codigo,
            dataCriacao,
            dataFinalizacao,
            dataLimite,
            descricao,
            situacao,
            tipo
        );

        assertEquals(codigo, dto.codigo());
        assertEquals(dataCriacao, dto.dataCriacao());
        assertEquals(dataFinalizacao, dto.dataFinalizacao());
        assertEquals(dataLimite, dto.dataLimite());
        assertEquals(descricao, dto.descricao());
        assertEquals(situacao, dto.situacao());
        assertEquals(tipo, dto.tipo());
    }
}