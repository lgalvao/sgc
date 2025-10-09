package sgc.processo.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProcessoDetalheDtoTest {

    @Test
    void testProcessoDetalheDtoRecord() {
        // Create test data
        ProcessoResumoDto resumoSubprocesso = new ProcessoResumoDto(1L, "Subprocess 1", "ATIVO", "TIPO_A", LocalDate.now(), LocalDateTime.now(), 1L, "Unidade Teste");
        ProcessoDetalheDto.UnidadeParticipanteDTO unidadeParticipante = new ProcessoDetalheDto.UnidadeParticipanteDTO(1L, "Test Unit", "TU", 10L, "ATIVO", LocalDate.now(), new ArrayList<>());

        List<ProcessoResumoDto> subprocessos = List.of(resumoSubprocesso);
        List<ProcessoDetalheDto.UnidadeParticipanteDTO> unidades = List.of(unidadeParticipante);

        // Test constructor and accessors
        ProcessoDetalheDto dto = new ProcessoDetalheDto(
            1L,
            "Test Description",
            "TIPO_A",
            "ATIVO",
            LocalDate.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            unidades,
            subprocessos
        );

        assertEquals(1L, dto.codigo());
        assertEquals("Test Description", dto.descricao());
        assertEquals("TIPO_A", dto.tipo());
        assertEquals("ATIVO", dto.situacao());
        assertNotNull(dto.dataLimite());
        assertNotNull(dto.dataCriacao());
        assertNotNull(dto.dataFinalizacao());
        assertEquals(1, dto.unidades().size());
        assertEquals(1, dto.resumoSubprocessos().size());
    }

    @Test
    void testUnidadeParticipanteDTORecord() {
        // Test constructor and accessors
        ProcessoDetalheDto.UnidadeParticipanteDTO dto = new ProcessoDetalheDto.UnidadeParticipanteDTO(
            1L,
            "Test Unit",
            "TU",
            10L,
            "ATIVO",
            LocalDate.now(),
            new ArrayList<>()
        );

        assertEquals(1L, dto.unidadeCodigo());
        assertEquals("Test Unit", dto.nome());
        assertEquals("TU", dto.sigla());
        assertEquals(10L, dto.unidadeSuperiorCodigo());
        assertEquals("ATIVO", dto.situacaoSubprocesso());
        assertNotNull(dto.dataLimite());
        assertNotNull(dto.filhos());
        assertTrue(dto.filhos().isEmpty());
    }
}