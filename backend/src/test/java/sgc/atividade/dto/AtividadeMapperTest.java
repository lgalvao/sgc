package sgc.atividade.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.atividade.modelo.Atividade;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtividadeMapperTest {

    private static final String TEST_DESCRIPTION = "Test Description";

    @InjectMocks
    private AtividadeMapperImpl mapper;

    @Mock
    private MapaRepo mapaRepo;

    @Test
    void testToDTO() {
        // Create an Atividade entity
        Atividade atividade = new Atividade();
        atividade.setCodigo(1L);
        atividade.setDescricao(TEST_DESCRIPTION);
        
        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);
        atividade.setMapa(mapa);

        // Map to DTO
        AtividadeDto dto = mapper.toDTO(atividade);

        // Verify mapping
        assertEquals(1L, dto.codigo());
        assertEquals(100L, dto.mapaCodigo());
        assertEquals(TEST_DESCRIPTION, dto.descricao());
    }

    @Test
    void testToEntity() {
        // Create an AtividadeDto
        AtividadeDto dto = new AtividadeDto(1L, 100L, TEST_DESCRIPTION);
        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);
        when(mapaRepo.findById(100L)).thenReturn(Optional.of(mapa));

        // Map to entity
        Atividade atividade = mapper.toEntity(dto);

        // Verify mapping
        assertNotNull(atividade);
        assertEquals(TEST_DESCRIPTION, atividade.getDescricao());
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
        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);
        when(mapaRepo.findById(100L)).thenReturn(Optional.of(mapa));
        Mapa result = mapper.map(100L);
        assertNotNull(result);
        assertEquals(100L, result.getCodigo());
    }
}