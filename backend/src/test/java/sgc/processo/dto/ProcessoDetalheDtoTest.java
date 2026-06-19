package sgc.processo.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.processo.ProcessoDtoMapper;
import sgc.processo.dto.ProcessoDetalheDto.UnidadeParticipanteDto;
import sgc.processo.model.UnidadeProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProcessoDetalheDto Test suite")
class ProcessoDetalheDtoTest {
    private final ProcessoDtoMapper mapper = new ProcessoDtoMapper();

    @Test
    @DisplayName("ProcessoDtoMapper.paraUnidadeParticipante(Unidade) deve mapear campos corretamente")
    void paraUnidadeParticipanteDeveMapearCampos() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setNome("Unidade 1");
        unidade.setSigla("U1");
        Unidade superior = new Unidade();
        superior.setCodigo(2L);
        unidade.setUnidadeSuperior(superior);

        UnidadeParticipanteDto dto = mapper.paraUnidadeParticipante(unidade);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodUnidade()).isEqualTo(1L);
        assertThat(dto.getNome()).isEqualTo("Unidade 1");
        assertThat(dto.getSigla()).isEqualTo("U1");
        assertThat(dto.getCodUnidadeSuperior()).isEqualTo(2L);
        assertThat(dto.getFilhos()).isEmpty();
    }

    @Test
    @DisplayName("ProcessoDtoMapper.paraUnidadeParticipante(Unidade) deve lidar com unidade superior null")
    void paraUnidadeParticipanteDeveLidarComSuperiorNull() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setUnidadeSuperior(null);

        UnidadeParticipanteDto dto = mapper.paraUnidadeParticipante(unidade);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodUnidadeSuperior()).isNull();
    }

    @Test
    @DisplayName("ProcessoDtoMapper.paraUnidadeParticipante(Snapshot) deve mapear campos corretamente")
    void paraUnidadeParticipanteSnapshotDeveMapearCampos() {
        UnidadeProcesso snapshot = new UnidadeProcesso();
        snapshot.setUnidadeCodigo(10L);
        snapshot.setNome("Snapshot unidade");
        snapshot.setSigla("SU");
        snapshot.setUnidadeSuperiorCodigo(20L);

        UnidadeParticipanteDto dto = mapper.paraUnidadeParticipante(snapshot);

        assertThat(dto).isNotNull();
        assertThat(dto.getCodUnidade()).isEqualTo(10L);
        assertThat(dto.getNome()).isEqualTo("Snapshot unidade");
        assertThat(dto.getSigla()).isEqualTo("SU");
        assertThat(dto.getCodUnidadeSuperior()).isEqualTo(20L);
        assertThat(dto.getFilhos()).isEmpty();
    }

    @Test
    @DisplayName("UnidadeParticipanteDto.preencherComSubprocesso deve mapear dados de subprocesso")
    void preencherComSubprocessoDeveMapearDados() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setNome("Unidade 1");
        unidade.setSigla("U1");

        UnidadeParticipanteDto dto = mapper.paraUnidadeParticipante(unidade);

        Mapa mapa = new Mapa();
        mapa.setCodigo(99L);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(30L);
        subprocesso.setMapa(mapa);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocesso.setDataLimiteEtapa1(java.time.LocalDateTime.of(2026, 1, 15, 10, 0));

        Unidade localizacao = new Unidade();
        localizacao.setCodigo(77L);

        mapper.preencherParticipanteComSubprocesso(dto, subprocesso, localizacao);

        assertThat(dto.getSituacaoSubprocesso()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO.name());
        assertThat(dto.getDataLimite()).isEqualTo(java.time.LocalDateTime.of(2026, 1, 15, 10, 0));
        assertThat(dto.getCodSubprocesso()).isEqualTo(30L);
        assertThat(dto.getMapaCodigo()).isEqualTo(99L);
        assertThat(dto.getLocalizacaoAtualCodigo()).isEqualTo(77L);
    }
}
