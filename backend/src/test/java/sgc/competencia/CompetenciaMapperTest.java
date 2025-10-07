package sgc.competencia;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.mapa.Mapa;

import static org.junit.jupiter.api.Assertions.*;

class CompetenciaMapperTest {

    private final CompetenciaMapper mapper = Mappers.getMapper(CompetenciaMapper.class);

    @Test
    void toDTO_comMapa_deveMapearCorretamente() {
        // Given
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        Competencia competencia = new Competencia(mapa, "Descrição Teste");
        competencia.setCodigo(10L);

        // When
        CompetenciaDTO dto = mapper.toDTO(competencia);

        // Then
        assertNotNull(dto);
        assertEquals(10L, dto.getCodigo());
        assertEquals(1L, dto.getMapaCodigo());
        assertEquals("Descrição Teste", dto.getDescricao());
    }

    @Test
    void toDTO_semMapa_deveMapearCorretamente() {
        // Given
        Competencia competencia = new Competencia(null, "Descrição Teste");
        competencia.setCodigo(10L);

        // When
        CompetenciaDTO dto = mapper.toDTO(competencia);

        // Then
        assertNotNull(dto);
        assertEquals(10L, dto.getCodigo());
        assertNull(dto.getMapaCodigo());
        assertEquals("Descrição Teste", dto.getDescricao());
    }

    @Test
    void toEntity_comMapaCodigo_deveMapearCorretamente() {
        // Given
        CompetenciaDTO dto = new CompetenciaDTO(10L, 1L, "Descrição Teste");

        // When
        Competencia competencia = mapper.toEntity(dto);

        // Then
        assertNotNull(competencia);
        assertEquals(10L, competencia.getCodigo());
        assertNotNull(competencia.getMapa());
        assertEquals(1L, competencia.getMapa().getCodigo());
        assertEquals("Descrição Teste", competencia.getDescricao());
    }

    @Test
    void toEntity_semMapaCodigo_deveMapearCorretamente() {
        // Given
        CompetenciaDTO dto = new CompetenciaDTO(10L, null, "Descrição Teste");

        // When
        Competencia competencia = mapper.toEntity(dto);

        // Then
        assertNotNull(competencia);
        assertEquals(10L, competencia.getCodigo());
        assertNull(competencia.getMapa());
        assertEquals("Descrição Teste", competencia.getDescricao());
    }

    @Test
    void map_comValorNulo_deveRetornarNulo() {
        // When
        Mapa mapa = mapper.map(null);

        // Then
        assertNull(mapa);
    }
}