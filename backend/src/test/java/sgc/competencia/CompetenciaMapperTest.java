package sgc.competencia;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.competencia.dto.CompetenciaDto;
import sgc.competencia.dto.CompetenciaMapper;
import sgc.competencia.modelo.Competencia;
import sgc.mapa.modelo.Mapa;

import static org.junit.jupiter.api.Assertions.*;

class CompetenciaMapperTest {
    private static final String DESCRICAO_TESTE = "Descrição Teste";
    private final CompetenciaMapper mapper = Mappers.getMapper(CompetenciaMapper.class);

    @Test
    void toDTO_comMapa_deveMapearCorretamente() {
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        Competencia competencia = new Competencia(mapa, DESCRICAO_TESTE);
        competencia.setCodigo(10L);

        CompetenciaDto dto = mapper.toDTO(competencia);

        assertNotNull(dto);
        assertEquals(10L, dto.getCodigo());
        assertEquals(1L, dto.getMapaCodigo());
        assertEquals(DESCRICAO_TESTE, dto.getDescricao());
    }

    @Test
    void toDTO_semMapa_deveMapearCorretamente() {
        Competencia competencia = new Competencia();
        competencia.setCodigo(10L);
        competencia.setDescricao(DESCRICAO_TESTE);

        CompetenciaDto dto = mapper.toDTO(competencia);

        assertNotNull(dto);
        assertEquals(10L, dto.getCodigo());
        assertNull(dto.getMapaCodigo());
        assertEquals(DESCRICAO_TESTE, dto.getDescricao());
    }

    @Test
    void toEntity_comMapaCodigo_deveMapearCorretamente() {
        CompetenciaDto dto = new CompetenciaDto(10L, 1L, DESCRICAO_TESTE);

        Competencia competencia = mapper.toEntity(dto);

        assertNotNull(competencia);
        assertEquals(10L, competencia.getCodigo());
        assertNotNull(competencia.getMapa());
        assertEquals(1L, competencia.getMapa().getCodigo());
        assertEquals(DESCRICAO_TESTE, competencia.getDescricao());
    }

    @Test
    void toEntity_semMapaCodigo_deveMapearCorretamente() {
        CompetenciaDto dto = new CompetenciaDto(10L, null, DESCRICAO_TESTE);

        Competencia competencia = mapper.toEntity(dto);

        assertNotNull(competencia);
        assertEquals(10L, competencia.getCodigo());
        assertNull(competencia.getMapa());
        assertEquals(DESCRICAO_TESTE, competencia.getDescricao());
    }

    @Test
    void map_comValorNulo_deveRetornarNulo() {
        Mapa mapa = mapper.map(null);

        assertNull(mapa);
    }
}