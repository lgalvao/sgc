package sgc.conhecimento.dto;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.atividade.modelo.Atividade;
import sgc.conhecimento.modelo.Conhecimento;

import static org.junit.jupiter.api.Assertions.*;

class ConhecimentoMapperTest {

    private final ConhecimentoMapper mapper = Mappers.getMapper(ConhecimentoMapper.class);

    @Test
    void testToDTO() {
        // Create a Conhecimento entity
        Conhecimento conhecimento = new Conhecimento();
        conhecimento.setCodigo(1L);
        conhecimento.setDescricao("Test Description");
        
        Atividade atividade = new Atividade();
        atividade.setCodigo(100L);
        conhecimento.setAtividade(atividade);

        // Map to DTO
        ConhecimentoDto dto = mapper.toDTO(conhecimento);

        // Verify mapping
        assertEquals(1L, dto.codigo());
        assertEquals(100L, dto.atividadeCodigo());
        assertEquals("Test Description", dto.descricao());
    }

    @Test
    void testToEntity() {
        // Create a ConhecimentoDto
        ConhecimentoDto dto = new ConhecimentoDto(1L, 100L, "Test Description");

        // Map to entity
        Conhecimento conhecimento = mapper.toEntity(dto);

        // Verify mapping
        assertNotNull(conhecimento);
        assertEquals("Test Description", conhecimento.getDescricao());
        assertNotNull(conhecimento.getAtividade());
        assertEquals(100L, conhecimento.getAtividade().getCodigo());
    }

    @Test
    void testMapWithNullValue() {
        // Test mapping null value
        Atividade result = mapper.map(null);
        assertNull(result);
    }

    @Test
    void testMapWithValidValue() {
        // Test mapping with valid value
        Atividade result = mapper.map(100L);
        assertNotNull(result);
        assertEquals(100L, result.getCodigo());
    }
}