package sgc.processo.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProcessoResumoDtoTest {

    @Test
    void testProcessoResumoDtoConstructorAndGettersSetters() {
        // Test constructor with parameters
        ProcessoResumoDto dto = new ProcessoResumoDto(
            1L,
            "Test Description",
            "ATIVO",
            "TIPO_A",
            LocalDate.now(),
            LocalDateTime.now(),
            10L,
            "Test Unit"
        );

        // Test getters
        assertEquals(1L, dto.getCodigo());
        assertEquals("Test Description", dto.getDescricao());
        assertEquals("ATIVO", dto.getSituacao());
        assertEquals("TIPO_A", dto.getTipo());
        assertNotNull(dto.getDataLimite());
        assertNotNull(dto.getDataCriacao());
        assertEquals(10L, dto.getUnidadeCodigo());
        assertEquals("Test Unit", dto.getUnidadeNome());

        // Test setters
        dto.setCodigo(2L);
        dto.setDescricao("New Description");
        dto.setSituacao("CANCELADO");
        dto.setTipo("TIPO_B");
        dto.setDataLimite(LocalDate.now().plusDays(1));
        dto.setUnidadeCodigo(20L);
        dto.setUnidadeNome("New Unit");

        assertEquals(2L, dto.getCodigo());
        assertEquals("New Description", dto.getDescricao());
        assertEquals("CANCELADO", dto.getSituacao());
        assertEquals("TIPO_B", dto.getTipo());
        assertNotNull(dto.getDataLimite());
        assertEquals(20L, dto.getUnidadeCodigo());
        assertEquals("New Unit", dto.getUnidadeNome());
    }

    @Test
    void testProcessoResumoDtoNoArgsConstructor() {
        // Test no-args constructor
        ProcessoResumoDto dto = new ProcessoResumoDto();

        // Initially should be null/0
        assertNull(dto.getCodigo());
        assertNull(dto.getDescricao());
        assertNull(dto.getSituacao());
        assertNull(dto.getTipo());
        assertNull(dto.getDataLimite());
        assertNull(dto.getDataCriacao());
        assertNull(dto.getUnidadeCodigo());
        assertNull(dto.getUnidadeNome());

        // Test setting values
        dto.setCodigo(1L);
        dto.setDescricao("Test Description");
        dto.setSituacao("ATIVO");
        dto.setTipo("TIPO_A");
        dto.setDataLimite(LocalDate.now());
        dto.setUnidadeCodigo(10L);
        dto.setUnidadeNome("Test Unit");

        assertEquals(1L, dto.getCodigo());
        assertEquals("Test Description", dto.getDescricao());
        assertEquals("ATIVO", dto.getSituacao());
        assertEquals("TIPO_A", dto.getTipo());
        assertNotNull(dto.getDataLimite());
        assertEquals(10L, dto.getUnidadeCodigo());
        assertEquals("Test Unit", dto.getUnidadeNome());
    }
}