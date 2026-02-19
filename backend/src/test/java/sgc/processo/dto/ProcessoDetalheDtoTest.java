package sgc.processo.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.UnidadeProcesso;
import sgc.processo.dto.ProcessoDetalheDto.UnidadeParticipanteDto;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("ProcessoDetalheDto Test Suite")
class ProcessoDetalheDtoTest {

    @Test
    @DisplayName("UnidadeParticipanteDto.fromUnidade deve retornar null se entrada for null")
    void fromUnidadeDeveRetornarNullSeEntradaNull() {
        assertThat(UnidadeParticipanteDto.fromUnidade(null)).isNull();
    }

    @Test
    @DisplayName("UnidadeParticipanteDto.fromUnidade deve mapear campos corretamente")
    void fromUnidadeDeveMapearCampos() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setNome("Unidade 1");
        unidade.setSigla("U1");
        Unidade superior = new Unidade();
        superior.setCodigo(2L);
        unidade.setUnidadeSuperior(superior);

        UnidadeParticipanteDto dto = UnidadeParticipanteDto.fromUnidade(unidade);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodUnidade()).isEqualTo(1L);
        assertThat(dto.getNome()).isEqualTo("Unidade 1");
        assertThat(dto.getSigla()).isEqualTo("U1");
        assertThat(dto.getCodUnidadeSuperior()).isEqualTo(2L);
        assertThat(dto.getFilhos()).isEmpty();
    }

    @Test
    @DisplayName("UnidadeParticipanteDto.fromUnidade deve lidar com unidade superior null")
    void fromUnidadeDeveLidarComSuperiorNull() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setUnidadeSuperior(null);

        UnidadeParticipanteDto dto = UnidadeParticipanteDto.fromUnidade(unidade);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodUnidadeSuperior()).isNull();
    }

    @Test
    @DisplayName("UnidadeParticipanteDto.fromSnapshot deve retornar null se entrada for null")
    void fromSnapshotDeveRetornarNullSeEntradaNull() {
        assertThat(UnidadeParticipanteDto.fromSnapshot(null)).isNull();
    }

    @Test
    @DisplayName("UnidadeParticipanteDto.fromSnapshot deve mapear campos corretamente")
    void fromSnapshotDeveMapearCampos() {
        UnidadeProcesso snapshot = new UnidadeProcesso();
        snapshot.setUnidadeCodigo(10L);
        snapshot.setNome("Snapshot Unidade");
        snapshot.setSigla("SU");
        snapshot.setUnidadeSuperiorCodigo(20L);

        UnidadeParticipanteDto dto = UnidadeParticipanteDto.fromSnapshot(snapshot);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodUnidade()).isEqualTo(10L);
        assertThat(dto.getNome()).isEqualTo("Snapshot Unidade");
        assertThat(dto.getSigla()).isEqualTo("SU");
        assertThat(dto.getCodUnidadeSuperior()).isEqualTo(20L);
        assertThat(dto.getFilhos()).isEmpty();
    }
}
