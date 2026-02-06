package sgc.processo.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.model.Processo;
import sgc.processo.model.UnidadeProcesso;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProcessoDetalheMapper Tests")
class ProcessoDetalheMapperTest {

    private final ProcessoDetalheMapper mapper = new ProcessoDetalheMapperImpl();

    @Test
    @DisplayName("Deve retornar null quando unidade é nula (fromUnidade)")
    void deveRetornarNullQuandoUnidadeNula() {
        assertThat(mapper.fromUnidade(null)).isNull();
    }

    @Test
    @DisplayName("Deve retornar null quando snapshot é nulo (fromSnapshot)")
    void deveRetornarNullQuandoSnapshotNulo() {
        assertThat(mapper.fromSnapshot(null)).isNull();
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

        ProcessoDetalheDto.UnidadeParticipanteDto dto = mapper.fromUnidade(unidade);

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

        ProcessoDetalheDto.UnidadeParticipanteDto dto = mapper.fromUnidade(unidade);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodUnidade()).isEqualTo(20L);
        assertThat(dto.getCodUnidadeSuperior()).isNull();
    }

    @Test
    @DisplayName("Deve cobrir snapshot com unidade superior")
    void deveCobrirSnapshotComSuperior() {
        Unidade superior = Unidade.builder().codigo(10L).sigla("SUP").build();
        Unidade unidade = Unidade.builder()
                .codigo(20L)
                .sigla("UNI")
                .nome("Unidade Teste")
                .unidadeSuperior(superior)
                .build();

        Processo processo = Processo.builder().codigo(1L).build();
        UnidadeProcesso snapshot = UnidadeProcesso.criarSnapshot(processo, unidade);

        ProcessoDetalheDto.UnidadeParticipanteDto dto = mapper.fromSnapshot(snapshot);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodUnidade()).isEqualTo(20L);
        assertThat(dto.getCodUnidadeSuperior()).isEqualTo(10L);
        assertThat(dto.getSigla()).isEqualTo("UNI");
    }

    @Test
    @DisplayName("Deve cobrir snapshot sem unidade superior")
    void deveCobrirSnapshotSemSuperior() {
        Unidade unidade = Unidade.builder()
                .codigo(20L)
                .sigla("UNI")
                .nome("Unidade Teste")
                .unidadeSuperior(null)
                .build();

        Processo processo = Processo.builder().codigo(1L).build();
        UnidadeProcesso snapshot = UnidadeProcesso.criarSnapshot(processo, unidade);

        ProcessoDetalheDto.UnidadeParticipanteDto dto = mapper.fromSnapshot(snapshot);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodUnidade()).isEqualTo(20L);
        assertThat(dto.getCodUnidadeSuperior()).isNull();
    }
}
