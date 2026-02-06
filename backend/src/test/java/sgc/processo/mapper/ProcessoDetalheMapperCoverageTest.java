package sgc.processo.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.ProcessoDetalheDto;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProcessoDetalheMapper Coverage Tests")
class ProcessoDetalheMapperCoverageTest {

    private final ProcessoDetalheMapper mapper = new ProcessoDetalheMapperImpl();

    @Test
    @DisplayName("Deve retornar null quando unidade é nula")
    void deveRetornarNullQuandoUnidadeNula() {
        assertThat(mapper.toUnidadeParticipanteDto(null)).isNull();
    }

    @Test
    @DisplayName("Deve cobrir unidade com unidade superior")
    void deveCobrirUnidadeComSuperior() {
        Unidade superior = Unidade.builder().codigo(10L).sigla("SUP").build();
        Unidade unidade = Unidade.builder()
                .codigo(20L)
                .sigla("UNI")
                .unidadeSuperior(superior)
                .build();

        ProcessoDetalheDto.UnidadeParticipanteDto dto = mapper.toUnidadeParticipanteDto(unidade);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodUnidade()).isEqualTo(20L);
        assertThat(dto.getCodUnidadeSuperior()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Deve cobrir unidade sem unidade superior")
    void deveCobrirUnidadeSemSuperior() {
        Unidade unidade = Unidade.builder()
                .codigo(20L)
                .sigla("UNI")
                .unidadeSuperior(null)
                .build();

        ProcessoDetalheDto.UnidadeParticipanteDto dto = mapper.toUnidadeParticipanteDto(unidade);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodUnidade()).isEqualTo(20L);
        assertThat(dto.getCodUnidadeSuperior()).isNull();
    }
}
