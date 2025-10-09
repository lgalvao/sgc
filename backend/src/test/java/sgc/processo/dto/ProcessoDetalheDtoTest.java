package sgc.processo.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProcessoDetalheDtoTest {
    @Test
    void testProcessoDetalheDtoConstructorAndGettersSetters() {
        // Create test data
        List<ProcessoResumoDto> subprocessos = new ArrayList<>();
        subprocessos.add(new ProcessoResumoDto(1L, "Subprocess 1", "ATIVO", "TIPO_A", LocalDate.now(), LocalDateTime.now(), 1L, "Unidade Teste"));
        
        List<ProcessoDetalheDto.UnidadeParticipanteDTO> unidades = new ArrayList<>();
        ProcessoDetalheDto.UnidadeParticipanteDTO unidade = 
            new ProcessoDetalheDto.UnidadeParticipanteDTO(1L, "Test Unit", "TU", 10L, "ATIVO", LocalDate.now(), new ArrayList<>());
        unidades.add(unidade);

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

        // Test getters
        assertEquals(1L, dto.getCodigo());
        assertEquals("Test Description", dto.getDescricao());
        assertEquals("TIPO_A", dto.getTipo());
        assertEquals("ATIVO", dto.getSituacao());
        assertNotNull(dto.getDataLimite());
        assertNotNull(dto.getDataCriacao());
        assertNotNull(dto.getDataFinalizacao());
        assertEquals(1, dto.getUnidades().size());
        assertEquals(1, dto.getResumoSubprocessos().size());

        // Test setters
        dto.setCodigo(2L);
        dto.setDescricao("New Description");
        dto.setTipo("TIPO_B");
        dto.setSituacao("CANCELADO");
        dto.setDataLimite(LocalDate.now().plusDays(1));
        dto.setDataFinalizacao(null);

        assertEquals(2L, dto.getCodigo());
        assertEquals("New Description", dto.getDescricao());
        assertEquals("TIPO_B", dto.getTipo());
        assertEquals("CANCELADO", dto.getSituacao());
        assertNotNull(dto.getDataLimite());
        assertNull(dto.getDataFinalizacao());
    }

    @Test
    void testProcessoDetalheDtoNoArgsConstructor() {
        // Test no-args constructor
        ProcessoDetalheDto dto = new ProcessoDetalheDto();

        // Initially should have default values
        assertNull(dto.getCodigo());
        assertNull(dto.getDescricao());
        assertNull(dto.getTipo());
        assertNull(dto.getSituacao());
        assertNull(dto.getDataLimite());
        assertNull(dto.getDataCriacao());
        assertNull(dto.getDataFinalizacao());
        assertNotNull(dto.getUnidades()); // Initialized with ArrayList
        assertNotNull(dto.getResumoSubprocessos()); // Initialized with ArrayList
        assertTrue(dto.getUnidades().isEmpty());
        assertTrue(dto.getResumoSubprocessos().isEmpty());

        // Test setting values
        dto.setCodigo(1L);
        dto.setDescricao("Test Description");

        assertEquals(1L, dto.getCodigo());
        assertEquals("Test Description", dto.getDescricao());
    }

    @Test
    void testUnidadeParticipanteDTO() {
        // Test UnidadeParticipanteDTO construction and methods
        ProcessoDetalheDto.UnidadeParticipanteDTO dto = 
            new ProcessoDetalheDto.UnidadeParticipanteDTO(1L, "Test Unit", "TU", 10L, "ATIVO", LocalDate.now(), new ArrayList<>());

        assertEquals(1L, dto.getUnidadeCodigo());
        assertEquals("Test Unit", dto.getNome());
        assertEquals("TU", dto.getSigla());
        assertEquals(10L, dto.getUnidadeSuperiorCodigo());
        assertEquals("ATIVO", dto.getSituacaoSubprocesso());
        assertNotNull(dto.getDataLimite());
        assertNotNull(dto.getFilhos());
        assertTrue(dto.getFilhos().isEmpty());

        // Test setters
        dto.setNome("New Unit Name");
        assertEquals("New Unit Name", dto.getNome());

        List<ProcessoDetalheDto.UnidadeParticipanteDTO> filhos = new ArrayList<>();
        ProcessoDetalheDto.UnidadeParticipanteDTO filho = new ProcessoDetalheDto.UnidadeParticipanteDTO();
        filhos.add(filho);
        dto.setFilhos(filhos);
        assertEquals(1, dto.getFilhos().size());
    }

    @Test
    void testUnidadeParticipanteDTONoArgsConstructor() {
        ProcessoDetalheDto.UnidadeParticipanteDTO dto = new ProcessoDetalheDto.UnidadeParticipanteDTO();

        // Initially should be null/empty
        assertNull(dto.getUnidadeCodigo());
        assertNull(dto.getNome());
        assertNull(dto.getSigla());
        assertNull(dto.getUnidadeSuperiorCodigo());
        assertNull(dto.getSituacaoSubprocesso());
        assertNull(dto.getDataLimite());
        assertNotNull(dto.getFilhos());
        assertTrue(dto.getFilhos().isEmpty());

        // Test setting values
        dto.setUnidadeCodigo(1L);
        dto.setNome("Test Unit");
        dto.setSigla("TU");

        assertEquals(1L, dto.getUnidadeCodigo());
        assertEquals("Test Unit", dto.getNome());
        assertEquals("TU", dto.getSigla());
    }
}