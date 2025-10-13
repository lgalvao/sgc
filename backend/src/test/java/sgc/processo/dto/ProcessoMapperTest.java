package sgc.processo.dto;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.processo.SituacaoProcesso;
import sgc.processo.modelo.Processo;

import sgc.processo.modelo.TipoProcesso;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProcessoMapperTest {
    private static final String TEST_DESCRIPTION = "Test Description";
    private final ProcessoConversor mapper = Mappers.getMapper(ProcessoConversor.class);

    @Test
    void testToDTO() {
        // Create a Processo entity
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDataCriacao(LocalDateTime.now());
        processo.setDataFinalizacao(LocalDateTime.now().plusDays(1));
        processo.setDataLimite(LocalDate.now().plusDays(5));
        processo.setDescricao(TEST_DESCRIPTION);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);

        // Map to DTO
        ProcessoDto dto = mapper.toDTO(processo);

        // Verify mapping
        assertEquals(1L, dto.getCodigo());
        assertEquals(TEST_DESCRIPTION, dto.getDescricao());
        assertEquals(SituacaoProcesso.EM_ANDAMENTO, dto.getSituacao());
        assertEquals(TipoProcesso.MAPEAMENTO.name(), dto.getTipo());
        assertNotNull(dto.getDataCriacao());
        assertNotNull(dto.getDataFinalizacao());
        assertNotNull(dto.getDataLimite());
    }

    @Test
    void testToEntity() {
        // Create a ProcessoDto
        ProcessoDto dto = ProcessoDto.builder()
            .codigo(1L)
            .dataCriacao(LocalDateTime.now())
            .dataFinalizacao(LocalDateTime.now().plusDays(1))
            .dataLimite(LocalDate.now().plusDays(5))
            .descricao(TEST_DESCRIPTION)
            .situacao(SituacaoProcesso.EM_ANDAMENTO)
            .tipo(TipoProcesso.MAPEAMENTO.name())
            .build();

        // Map to entity
        Processo processo = mapper.toEntity(dto);

        // Verify mapping
        assertNotNull(processo);
        assertEquals(TEST_DESCRIPTION, processo.getDescricao());
        assertEquals(SituacaoProcesso.EM_ANDAMENTO, processo.getSituacao());
        assertEquals(TipoProcesso.MAPEAMENTO, processo.getTipo());
        assertNotNull(processo.getDataCriacao());
        assertNotNull(processo.getDataFinalizacao());
        assertNotNull(processo.getDataLimite());
        assertEquals(1L, processo.getCodigo());
    }
}
