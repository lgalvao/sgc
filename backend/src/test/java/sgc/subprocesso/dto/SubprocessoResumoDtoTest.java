package sgc.subprocesso.dto;

import org.junit.jupiter.api.*;
import sgc.mapa.model.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.*;
import sgc.subprocesso.model.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SubprocessoResumoDto")
class SubprocessoResumoDtoTest {
    private final SubprocessoDtoMapper mapper = new SubprocessoDtoMapper(new OrganizacaoDtoMapper());


    @Test
    @DisplayName("deve mapear subprocesso resumo com dependencias obrigatorias")
    void deveMapearSubprocessoResumoComDependenciasObrigatorias() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setNome("Unidade");
        unidade.setSigla("UND");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setTituloTitular("999");

        Processo processo = Processo.builder()
                .descricao("Processo")
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .tipo(TipoProcesso.MAPEAMENTO)
                .dataCriacao(LocalDateTime.of(2025, 1, 1, 10, 0))
                .dataLimite(LocalDateTime.of(2025, 1, 30, 10, 0))
                .build();
        processo.setCodigo(50L);

        Mapa mapa = new Mapa();
        mapa.setCodigo(60L);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(20L);
        subprocesso.setProcesso(processo);
        subprocesso.setUnidade(unidade);
        subprocesso.setMapa(mapa);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.of(2025, 1, 10, 10, 0));

        SubprocessoResumoDto dto = mapper.paraResumo(subprocesso);

        assertThat(dto.codigo()).isEqualTo(20L);
        assertThat(dto.codProcesso()).isEqualTo(50L);
        assertThat(dto.codUnidade()).isEqualTo(10L);
        assertThat(dto.unidade().nome()).isEqualTo("Unidade");
        assertThat(dto.processoDescricao()).isEqualTo("Processo");
        assertThat(dto.isEmAndamento()).isTrue();
    }

    @Test
    @DisplayName("deve falhar quando processo ou unidade estiverem ausentes")
    void deveFalharQuandoProcessoOuUnidadeEstiveremAusentes() {
        Subprocesso subprocesso = new Subprocesso();

        assertThatThrownBy(() -> mapper.paraResumo(subprocesso))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Subprocesso deve possuir processo e unidade associados");
    }

    @Test
    @DisplayName("deve falhar quando processo estiver presente mas unidade estiver ausente")
    void deveFalharQuandoProcessoEstiverPresenteMasUnidadeEstiverAusente() {
        Processo processo = Processo.builder()
                .descricao("Processo")
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .tipo(TipoProcesso.MAPEAMENTO)
                .dataCriacao(LocalDateTime.of(2025, 1, 1, 10, 0))
                .dataLimite(LocalDateTime.of(2025, 1, 30, 10, 0))
                .build();
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setProcesso(processo);

        assertThatThrownBy(() -> mapper.paraResumo(subprocesso))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Subprocesso deve possuir processo e unidade associados");
    }

    @Test
    @DisplayName("deve retornar dataLimiteEtapa1 como ultimaDataLimite quando etapa1 for posterior a etapa2")
    void deveRetornarDataLimiteEtapa1QuandoEtapa1ForPosteriorAEtapa2() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setNome("Unidade");
        unidade.setSigla("UND");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setTituloTitular("999");

        Processo processo = Processo.builder()
                .descricao("Processo")
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .tipo(TipoProcesso.REVISAO)
                .dataCriacao(LocalDateTime.of(2025, 1, 1, 10, 0))
                .dataLimite(LocalDateTime.of(2025, 2, 28, 10, 0))
                .build();
        processo.setCodigo(50L);

        LocalDateTime etapa1 = LocalDateTime.of(2025, 2, 20, 10, 0);
        LocalDateTime etapa2 = LocalDateTime.of(2025, 2, 10, 10, 0);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(20L);
        subprocesso.setProcesso(processo);
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        subprocesso.setDataLimiteEtapa1(etapa1);
        subprocesso.setDataLimiteEtapa2(etapa2);

        SubprocessoResumoDto dto = mapper.paraResumo(subprocesso);

        assertThat(dto.ultimaDataLimite()).isEqualTo(etapa1);
    }

    @Test
    @DisplayName("deve retornar dataLimiteEtapa2 como ultimaDataLimite quando etapa2 for posterior a etapa1")
    void deveRetornarDataLimiteEtapa2QuandoEtapa2ForPosteriorAEtapa1() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setNome("Unidade");
        unidade.setSigla("UND");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setTituloTitular("999");

        Processo processo = Processo.builder()
                .descricao("Processo")
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .tipo(TipoProcesso.REVISAO)
                .dataCriacao(LocalDateTime.of(2025, 1, 1, 10, 0))
                .dataLimite(LocalDateTime.of(2025, 3, 31, 10, 0))
                .build();
        processo.setCodigo(50L);

        LocalDateTime etapa1 = LocalDateTime.of(2025, 2, 10, 10, 0);
        LocalDateTime etapa2 = LocalDateTime.of(2025, 3, 20, 10, 0);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(20L);
        subprocesso.setProcesso(processo);
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        subprocesso.setDataLimiteEtapa1(etapa1);
        subprocesso.setDataLimiteEtapa2(etapa2);

        SubprocessoResumoDto dto = mapper.paraResumo(subprocesso);

        assertThat(dto.ultimaDataLimite()).isEqualTo(etapa2);
    }
}
