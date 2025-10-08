package sgc.atividade.dto;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.atividade.modelo.Atividade;
import sgc.mapa.modelo.Mapa;

import static org.junit.jupiter.api.Assertions.*;

class AtividadeMapperTest {

    private final AtividadeMapper mapper = Mappers.getMapper(AtividadeMapper.class);

    @Test
    void testToDTO() {
        // Create an Atividade entity
        Atividade atividade = new Atividade();
        atividade.setCodigo(1L);
        atividade.setDescricao("Test Description");
        
        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);
        atividade.setMapa(mapa);

        // Map to DTO
        AtividadeDto dto = mapper.toDTO(atividade);

        // Verify mapping
        assertEquals(1L, dto.getCodigo());
        assertEquals(100L, dto.getMapaCodigo());
        assertEquals("Test Description", dto.getDescricao());
    }

    @Test
    void testToEntity() {
        // Create an AtividadeDto
        AtividadeDto dto = new AtividadeDto(1L, 100L, "Test Description");

        // Map to entity
        Atividade atividade = mapper.toEntity(dto);

        // Verify mapping
        assertNotNull(atividade);
        assertEquals("Test Description", atividade.getDescricao());
        assertNotNull(atividade.getMapa());
        assertEquals(100L, atividade.getMapa().getCodigo());
    }

    @Test
    void testMapWithNullValue() {
        // Test mapping null value
        Mapa result = mapper.map(null);
        assertNull(result);
    }

    @Test
    void testMapWithValidValue() {
        // Test mapping with valid value
        Mapa result = mapper.map(100L);
        assertNotNull(result);
        assertEquals(100L, result.getCodigo());
    }
}