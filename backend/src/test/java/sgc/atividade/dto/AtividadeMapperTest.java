package sgc.atividade.dto;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.atividade.model.Atividade;
import sgc.mapa.model.Mapa;

class AtividadeMapperTest {
    private static final String TEST_DESCRIPTION = "Test Description";

    private final AtividadeMapper mapper = Mappers.getMapper(AtividadeMapper.class);

    @Test
    void testToDto() {
        Atividade atividade = new Atividade();
        atividade.setCodigo(1L);
        atividade.setDescricao(TEST_DESCRIPTION);

        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);
        atividade.setMapa(mapa);

        AtividadeDto dto = mapper.toDto(atividade);

        assertEquals(1L, dto.getCodigo());
        assertEquals(100L, dto.getMapaCodigo());
        assertEquals(TEST_DESCRIPTION, dto.getDescricao());
    }

    @Test
    void testToEntityIgnoraMapa() {
        AtividadeDto dto = new AtividadeDto(1L, 100L, TEST_DESCRIPTION);

        Atividade atividade = mapper.toEntity(dto);

        assertNotNull(atividade);
        assertEquals(TEST_DESCRIPTION, atividade.getDescricao());
        assertNull(atividade.getMapa());
    }
}
