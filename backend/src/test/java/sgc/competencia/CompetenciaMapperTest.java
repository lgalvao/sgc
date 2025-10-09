package sgc.competencia;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.competencia.dto.CompetenciaDto;
import sgc.competencia.dto.CompetenciaMapper;
import sgc.competencia.modelo.Competencia;
import sgc.mapa.modelo.Mapa;

import static org.junit.jupiter.api.Assertions.*;

class CompetenciaMapperTest {
    private final CompetenciaMapper mapper = Mappers.getMapper(CompetenciaMapper.class);

    @Test
    void toDTO_comMapa_deveMapearCorretamente() {
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        Competencia competencia = new Competencia(mapa, "Descrição Teste");
        competencia.setCodigo(10L);

        CompetenciaDto dto = mapper.toDTO(competencia);

        assertNotNull(dto);
        assertEquals(10L, dto.getCodigo());
        assertEquals(1L, dto.getMapaCodigo());
        assertEquals("Descrição Teste", dto.getDescricao());
    }

    @Test
    void toDTO_semMapa_deveMapearCorretamente() {
        Competencia competencia = new Competencia(null, "Descrição Teste");
        competencia.setCodigo(10L);

        CompetenciaDto dto = mapper.toDTO(competencia);

        assertNotNull(dto);
        assertEquals(10L, dto.getCodigo());
        assertNull(dto.getMapaCodigo());
        assertEquals("Descrição Teste", dto.getDescricao());
    }

    @Test
    void toEntity_comMapaCodigo_deveMapearCorretamente() {
        CompetenciaDto dto = new CompetenciaDto(10L, 1L, "Descrição Teste");

        Competencia competencia = mapper.toEntity(dto);

        assertNotNull(competencia);
        assertEquals(10L, competencia.getCodigo());
        assertNotNull(competencia.getMapa());
        assertEquals(1L, competencia.getMapa().getCodigo());
        assertEquals("Descrição Teste", competencia.getDescricao());
    }

    @Test
    void toEntity_semMapaCodigo_deveMapearCorretamente() {
        CompetenciaDto dto = new CompetenciaDto(10L, null, "Descrição Teste");

        Competencia competencia = mapper.toEntity(dto);

        assertNotNull(competencia);
        assertEquals(10L, competencia.getCodigo());
        assertNull(competencia.getMapa());
        assertEquals("Descrição Teste", competencia.getDescricao());
    }

    @Test
    void map_comValorNulo_deveRetornarNulo() {
        Mapa mapa = mapper.map(null);

        assertNull(mapa);
    }
}