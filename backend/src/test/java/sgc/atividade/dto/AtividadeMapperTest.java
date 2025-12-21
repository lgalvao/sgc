package sgc.atividade.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.atividade.model.Atividade;
import sgc.mapa.model.Mapa;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários: AtividadeMapper")
class AtividadeMapperTest {
    private static final String TEST_DESCRIPTION = "Test Description";

    private final AtividadeMapper mapper = Mappers.getMapper(AtividadeMapper.class);

    @Test
    @DisplayName("Deve mapear entidade para DTO corretamente")
    void deveMapearEntidadeParaDto() {
        // Given
        Atividade atividade = new Atividade();
        atividade.setCodigo(1L);
        atividade.setDescricao(TEST_DESCRIPTION);

        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);
        atividade.setMapa(mapa);

        // When
        AtividadeDto dto = mapper.toDto(atividade);

        // Then
        assertEquals(1L, dto.getCodigo());
        assertEquals(100L, dto.getMapaCodigo());
        assertEquals(TEST_DESCRIPTION, dto.getDescricao());
    }

    @Test
    @DisplayName("Deve ignorar mapa ao mapear DTO para entidade")
    void deveIgnorarMapaAoMapearParaEntidade() {
        // Given
        AtividadeDto dto = new AtividadeDto(1L, 100L, TEST_DESCRIPTION);

        // When
        Atividade atividade = mapper.toEntity(dto);

        // Then
        assertNotNull(atividade);
        assertEquals(TEST_DESCRIPTION, atividade.getDescricao());
        assertNull(atividade.getMapa());
    }
}
