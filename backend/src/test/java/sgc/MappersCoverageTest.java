package sgc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Conhecimento;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cobertura de Mappers.
 * 
 * <p><b>Nota:</b> Após simplificação (Fase 1), os mappers foram purificados
 * e não possuem mais dependências de repositories. Este teste foi simplificado
 * para testar apenas a funcionalidade real de mapeamento.
 */
@DisplayName("Cobertura de Mappers")
@Tag("unit")
class MappersCoverageTest {
    private final ConhecimentoMapper conhecimentoMapper = Mappers.getMapper(ConhecimentoMapper.class);

    @Test
    @DisplayName("Deve mapear Conhecimento para ConhecimentoResponse")
    void deveMappearConhecimentoParaResponse() {
        Atividade atividade = new Atividade();
        atividade.setCodigo(1L);
        
        Conhecimento conhecimento = new Conhecimento();
        conhecimento.setCodigo(100L);
        conhecimento.setDescricao("Conhecimento de teste");
        conhecimento.setAtividade(atividade);

        sgc.mapa.dto.ConhecimentoResponse response = conhecimentoMapper.toResponse(conhecimento);

        assertThat(response).isNotNull();
        assertThat(response.codigo()).isEqualTo(100L);
        assertThat(response.descricao()).isEqualTo("Conhecimento de teste");
        assertThat(response.atividadeCodigo()).isEqualTo(1L);
    }
}