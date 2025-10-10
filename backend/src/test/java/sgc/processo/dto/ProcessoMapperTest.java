package sgc.processo.dto;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.comum.enums.SituacaoProcesso;
import sgc.processo.modelo.Processo;

import sgc.processo.enums.TipoProcesso;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProcessoMapperTest {
    private final ProcessoMapper mapper = Mappers.getMapper(ProcessoMapper.class);

    @Test
    void testToDTO() {
        // Create a Processo entity
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDataCriacao(LocalDateTime.now());
        processo.setDataFinalizacao(LocalDateTime.now().plusDays(1));
        processo.setDataLimite(LocalDate.now().plusDays(5));
        processo.setDescricao("Test Description");
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);

        // Map to DTO
        ProcessoDto dto = mapper.toDTO(processo);

        // Verify mapping
        assertEquals(1L, dto.codigo());
        assertEquals("Test Description", dto.descricao());
        assertEquals(SituacaoProcesso.EM_ANDAMENTO, dto.situacao());
        assertEquals(TipoProcesso.MAPEAMENTO.name(), dto.tipo());
        assertNotNull(dto.dataCriacao());
        assertNotNull(dto.dataFinalizacao());
        assertNotNull(dto.dataLimite());
    }

    @Test
    void testToEntity() {
        // Create a ProcessoDto
        ProcessoDto dto = new ProcessoDto(1L, LocalDateTime.now(), LocalDateTime.now().plusDays(1), 
                                         LocalDate.now().plusDays(5), "Test Description", SituacaoProcesso.EM_ANDAMENTO, TipoProcesso.MAPEAMENTO.name());

        // Map to entity
        Processo processo = mapper.toEntity(dto);

        // Verify mapping
        assertNotNull(processo);
        assertEquals("Test Description", processo.getDescricao());
        assertEquals(SituacaoProcesso.EM_ANDAMENTO, processo.getSituacao());
        assertEquals(TipoProcesso.MAPEAMENTO, processo.getTipo());
        assertNotNull(processo.getDataCriacao());
        assertNotNull(processo.getDataFinalizacao());
        assertNotNull(processo.getDataLimite());
        assertEquals(1L, processo.getCodigo());
    }
}
