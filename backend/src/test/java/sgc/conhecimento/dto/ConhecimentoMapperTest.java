package sgc.conhecimento.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sgc.atividade.api.ConhecimentoDto;
import sgc.atividade.internal.ConhecimentoMapper;
import sgc.atividade.internal.model.Atividade;
import sgc.atividade.internal.model.AtividadeRepo;
import sgc.atividade.internal.model.Conhecimento;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConhecimentoMapperTest {
    private static final String DESC = "Test Description";

    @Mock
    private AtividadeRepo atividadeRepo;

    private ConhecimentoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(ConhecimentoMapper.class);
        ReflectionTestUtils.setField(mapper, "atividadeRepo", atividadeRepo);
    }

    @Test
    void testToDto() {
        Conhecimento conhecimento = new Conhecimento();
        conhecimento.setCodigo(1L);
        conhecimento.setDescricao(DESC);

        Atividade atividade = new Atividade();
        atividade.setCodigo(100L);
        conhecimento.setAtividade(atividade);

        ConhecimentoDto dto = mapper.toDto(conhecimento);

        assertEquals(1L, dto.getCodigo());
        assertEquals(100L, dto.getAtividadeCodigo());
        assertEquals(DESC, dto.getDescricao());
    }

    @Test
    void testToEntity() {
        ConhecimentoDto dto = new ConhecimentoDto(1L, 100L, DESC);
        Atividade atividade = new Atividade();
        atividade.setCodigo(100L);
        when(atividadeRepo.findById(100L)).thenReturn(Optional.of(atividade));

        Conhecimento conhecimento = mapper.toEntity(dto);

        assertNotNull(conhecimento);
        assertEquals(DESC, conhecimento.getDescricao());
        assertNotNull(conhecimento.getAtividade());
        assertEquals(100L, conhecimento.getAtividade().getCodigo());
    }

    @Test
    void testMapWithNullValue() {
        Atividade result = mapper.map(null);
        assertNull(result);
    }

    @Test
    void testMapWithValidValue() {
        Atividade atividade = new Atividade();
        atividade.setCodigo(100L);
        when(atividadeRepo.findById(100L)).thenReturn(Optional.of(atividade));
        Atividade result = mapper.map(100L);
        assertNotNull(result);
        assertEquals(100L, result.getCodigo());
    }
}
