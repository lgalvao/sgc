package sgc.atividade.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
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

    private final AtividadeMapper mapper = Mappers.getMapper(AtividadeMapper.class);

    @Mock
    private MapaRepo mapaRepo;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        // Injeta o mock do mapaRepo no mapper
        java.lang.reflect.Field field = AtividadeMapper.class.getDeclaredField("mapaRepo");
        field.setAccessible(true);
        field.set(mapper, mapaRepo);
    }

    @Test
    void testToDto() {
        Atividade atividade = new Atividade();
        atividade.setCodigo(1L);
        atividade.setDescricao(TEST_DESCRIPTION);

        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);
        atividade.setMapa(mapa);

        AtividadeDto dto = mapper.toDto(atividade);

        assertEquals(1L, dto.codigo());
        assertEquals(100L, dto.mapaCodigo());
        assertEquals(TEST_DESCRIPTION, dto.descricao());
    }

    @Test
    void testToEntity() {
        AtividadeDto dto = new AtividadeDto(1L, 100L, TEST_DESCRIPTION);
        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);
        when(mapaRepo.findById(100L)).thenReturn(Optional.of(mapa));

        Atividade atividade = mapper.toEntity(dto);

        assertNotNull(atividade);
        assertEquals(TEST_DESCRIPTION, atividade.getDescricao());
        assertNotNull(atividade.getMapa());
        assertEquals(100L, atividade.getMapa().getCodigo());
    }

    @Test
    void testMapWithNullValue() {
        Mapa result = mapper.map(null);
        assertNull(result);
    }

    @Test
    void testMapWithValidValue() {
        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);
        when(mapaRepo.findById(100L)).thenReturn(Optional.of(mapa));
        Mapa result = mapper.map(100L);
        assertNotNull(result);
        assertEquals(100L, result.getCodigo());
    }
}