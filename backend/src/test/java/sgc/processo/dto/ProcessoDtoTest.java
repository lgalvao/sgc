package sgc.processo.dto;

import org.junit.jupiter.api.Test;
import sgc.processo.modelo.SituacaoProcesso;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProcessoDtoTest {
    @Test
    void testProcessoDtoBuilderAndAccessors() {
        var now = LocalDateTime.now();
        var limitDate = LocalDateTime.now();

        var dto = ProcessoDto.builder()
            .codigo(1L)
            .dataCriacao(now)
            .dataFinalizacao(now)
            .dataLimite(limitDate)
            .descricao("Test Description")
            .situacao(SituacaoProcesso.EM_ANDAMENTO)
            .tipo("TIPO_A")
            .build();

        assertEquals(1L, dto.getCodigo());
        assertEquals(now, dto.getDataCriacao());
        assertEquals(now, dto.getDataFinalizacao());
        assertEquals(limitDate, dto.getDataLimite());
        assertEquals("Test Description", dto.getDescricao());
        assertEquals(SituacaoProcesso.EM_ANDAMENTO, dto.getSituacao());
        assertEquals("TIPO_A", dto.getTipo());
    }
}
