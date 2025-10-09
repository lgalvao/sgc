package sgc.processo.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProcessoResumoDtoTest {

    @Test
    void testProcessoResumoDtoRecord() {
        // Create test data
        Long codigo = 1L;
        String descricao = "Test Description";
        String situacao = "ATIVO";
        String tipo = "TIPO_A";
        LocalDate dataLimite = LocalDate.now();
        LocalDateTime dataCriacao = LocalDateTime.now();
        Long unidadeCodigo = 10L;
        String unidadeNome = "Test Unit";

        // Test constructor and accessors
        ProcessoResumoDto dto = new ProcessoResumoDto(
            codigo,
            descricao,
            situacao,
            tipo,
            dataLimite,
            dataCriacao,
            unidadeCodigo,
            unidadeNome
        );

        assertEquals(codigo, dto.codigo());
        assertEquals(descricao, dto.descricao());
        assertEquals(situacao, dto.situacao());
        assertEquals(tipo, dto.tipo());
        assertEquals(dataLimite, dto.dataLimite());
        assertEquals(dataCriacao, dto.dataCriacao());
        assertEquals(unidadeCodigo, dto.unidadeCodigo());
        assertEquals(unidadeNome, dto.unidadeNome());
    }
}