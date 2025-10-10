package sgc.conhecimento.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.conhecimento.modelo.Conhecimento;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConhecimentoMapperTest {

    @Mock
    private AtividadeRepo atividadeRepo;

    @InjectMocks
    private ConhecimentoMapperImpl mapper;

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
        Atividade atividade = new Atividade();
        atividade.setCodigo(100L);
        when(atividadeRepo.findById(100L)).thenReturn(Optional.of(atividade));

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
        Atividade atividade = new Atividade();
        atividade.setCodigo(100L);
        when(atividadeRepo.findById(100L)).thenReturn(Optional.of(atividade));
        Atividade result = mapper.map(100L);
        assertNotNull(result);
        assertEquals(100L, result.getCodigo());
    }
}