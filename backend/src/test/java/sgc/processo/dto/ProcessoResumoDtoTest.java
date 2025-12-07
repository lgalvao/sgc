package sgc.processo.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import sgc.processo.model.SituacaoProcesso;

class ProcessoResumoDtoTest {
    @Test
    void testProcessoResumoDtoBuilderAndAccessors() {
        var now = LocalDateTime.now();
        var limitDate = LocalDateTime.now();
        var dto =
                ProcessoResumoDto.builder()
                        .codigo(1L)
                        .descricao("Test Description")
                        .situacao(SituacaoProcesso.EM_ANDAMENTO)
                        .tipo("TIPO_A")
                        .dataLimite(limitDate)
                        .dataCriacao(now)
                        .unidadeCodigo(10L)
                        .unidadeNome("Test Unit")
                        .build();

        assertEquals(1L, dto.getCodigo());
        assertEquals("Test Description", dto.getDescricao());
        assertEquals(SituacaoProcesso.EM_ANDAMENTO, dto.getSituacao());
        assertEquals("TIPO_A", dto.getTipo());
        assertEquals(limitDate, dto.getDataLimite());
        assertEquals(now, dto.getDataCriacao());
        assertEquals(10L, dto.getUnidadeCodigo());
        assertEquals("Test Unit", dto.getUnidadeNome());
    }
}
