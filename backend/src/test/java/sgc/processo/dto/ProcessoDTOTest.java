package sgc.processo.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProcessoDtoTest {

    @Test
    void testProcessoDtoConstructorAndGettersSetters() {
        // Test constructor with parameters
        ProcessoDto dto = new ProcessoDto(
            1L,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDate.now(),
            "Test Description",
            "ATIVO",
            "TIPO_A"
        );

        // Test getters
        assertEquals(1L, dto.getCodigo());
        assertNotNull(dto.getDataCriacao());
        assertNotNull(dto.getDataFinalizacao());
        assertNotNull(dto.getDataLimite());
        assertEquals("Test Description", dto.getDescricao());
        assertEquals("ATIVO", dto.getSituacao());
        assertEquals("TIPO_A", dto.getTipo());

        // Test setters
        dto.setCodigo(2L);
        dto.setDataCriacao(LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
        dto.setDataFinalizacao(null);
        dto.setDataLimite(LocalDate.now().plusDays(1));
        dto.setDescricao("New Description");
        dto.setSituacao("CANCELADO");
        dto.setTipo("TIPO_B");

        assertEquals(2L, dto.getCodigo());
        assertNotNull(dto.getDataCriacao());
        assertNull(dto.getDataFinalizacao());
        assertNotNull(dto.getDataLimite());
        assertEquals("New Description", dto.getDescricao());
        assertEquals("CANCELADO", dto.getSituacao());
        assertEquals("TIPO_B", dto.getTipo());
    }

    @Test
    void testProcessoDtoNoArgsConstructor() {
        // Test no-args constructor
        ProcessoDto dto = new ProcessoDto();

        // Initially should be null/0
        assertNull(dto.getCodigo());
        assertNull(dto.getDataCriacao());
        assertNull(dto.getDataFinalizacao());
        assertNull(dto.getDataLimite());
        assertNull(dto.getDescricao());
        assertNull(dto.getSituacao());
        assertNull(dto.getTipo());

        // Test setting values
        dto.setCodigo(1L);
        dto.setDescricao("Test Description");
        dto.setSituacao("ATIVO");
        dto.setTipo("TIPO_A");
        dto.setDataLimite(LocalDate.now());
        dto.setDataCriacao(LocalDateTime.now());
        dto.setDataFinalizacao(LocalDateTime.now());

        assertEquals(1L, dto.getCodigo());
        assertEquals("Test Description", dto.getDescricao());
        assertEquals("ATIVO", dto.getSituacao());
        assertEquals("TIPO_A", dto.getTipo());
        assertNotNull(dto.getDataLimite());
        assertNotNull(dto.getDataCriacao());
        assertNotNull(dto.getDataFinalizacao());
    }
}