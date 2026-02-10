package sgc.processo.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.model.Processo;
import sgc.processo.model.UnidadeProcesso;
import sgc.testutils.UnidadeTestBuilder;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProcessoDetalheMapper Tests")
class ProcessoDetalheMapperTest {
    private final ProcessoDetalheMapper mapper = new ProcessoDetalheMapperImpl();

    @Test
    @DisplayName("Deve cobrir unidade com unidade superior")
    void deveCobrirUnidadeComSuperior() {
        Unidade superior = UnidadeTestBuilder.umaDe().comCodigo("10").comSigla("SUP").build();
        Unidade unidade = UnidadeTestBuilder.umaDe()
                .comCodigo("20")
                .comSigla("UNI")
                .comSuperior(superior)
                .build();

        ProcessoDetalheDto.UnidadeParticipanteDto dto = mapper.fromUnidade(unidade);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodUnidade()).isEqualTo(20L);
        assertThat(dto.getCodUnidadeSuperior()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Deve cobrir unidade sem unidade superior")
    void deveCobrirUnidadeSemSuperior() {
        Unidade unidade = UnidadeTestBuilder.umaDe()
                .comCodigo("20")
                .comSigla("UNI")
                .comSuperior(null)
                .build();

        ProcessoDetalheDto.UnidadeParticipanteDto dto = mapper.fromUnidade(unidade);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodUnidade()).isEqualTo(20L);
        assertThat(dto.getCodUnidadeSuperior()).isNull();
    }

    @Test
    @DisplayName("Deve cobrir snapshot com unidade superior")
    void deveCobrirSnapshotComSuperior() {
        Unidade superior = UnidadeTestBuilder.umaDe().comCodigo("10").comSigla("SUP").build();
        Unidade unidade = UnidadeTestBuilder.umaDe()
                .comCodigo("20")
                .comSigla("UNI")
                .comNome("Unidade Teste")
                .comSuperior(superior)
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
        Unidade unidade = UnidadeTestBuilder.umaDe()
                .comCodigo("20")
                .comSigla("UNI")
                .comNome("Unidade Teste")
                .comSuperior(null)
                .build();

        Processo processo = Processo.builder().codigo(1L).build();
        UnidadeProcesso snapshot = UnidadeProcesso.criarSnapshot(processo, unidade);

        ProcessoDetalheDto.UnidadeParticipanteDto dto = mapper.fromSnapshot(snapshot);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodUnidade()).isEqualTo(20L);
        assertThat(dto.getCodUnidadeSuperior()).isNull();
    }
}
