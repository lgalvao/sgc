package sgc.subprocesso.dto;

import lombok.*;
import org.jspecify.annotations.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;

@Builder
public record SubprocessoListagemDto(
        @Nullable Long codigo,
        @Nullable UnidadeDto unidade,
        SituacaoSubprocesso situacao,
        LocalDateTime dataLimiteEtapa1,
        @Nullable LocalDateTime dataFimEtapa1,
        @Nullable LocalDateTime dataLimiteEtapa2,
        @Nullable LocalDateTime dataFimEtapa2,
        @Nullable Long codProcesso,
        @Nullable Long codUnidade,
        @Nullable Long codMapa,
        @Nullable String processoDescricao,
        @Nullable LocalDateTime dataCriacaoProcesso,
        @Nullable TipoProcesso tipoProcesso,
        boolean isEmAndamento,
        @Nullable Integer etapaAtual) {

    public static SubprocessoListagemDto fromEntity(Subprocesso subprocesso) {
        Processo processo = subprocesso.getProcesso();

        return SubprocessoListagemDto.builder()
                .codigo(subprocesso.getCodigo())
                .unidade(paraUnidadeResumo(subprocesso.getUnidade()))
                .situacao(subprocesso.getSituacao())
                .dataLimiteEtapa1(subprocesso.getDataLimiteEtapa1())
                .dataFimEtapa1(subprocesso.getDataFimEtapa1())
                .dataLimiteEtapa2(subprocesso.getDataLimiteEtapa2())
                .dataFimEtapa2(subprocesso.getDataFimEtapa2())
                .codProcesso(subprocesso.getCodProcesso())
                .codUnidade(subprocesso.getCodUnidade())
                .codMapa(subprocesso.getCodMapa())
                .processoDescricao(processo != null ? processo.getDescricao() : null)
                .dataCriacaoProcesso(processo != null ? processo.getDataCriacao() : null)
                .tipoProcesso(processo != null ? processo.getTipo() : null)
                .isEmAndamento(subprocesso.isEmAndamento())
                .etapaAtual(subprocesso.getEtapaAtual())
                .build();
    }

    private static @Nullable UnidadeDto paraUnidadeResumo(@Nullable Unidade unidade) {
        if (unidade == null) {
            return null;
        }

        return UnidadeDto.builder()
                .codigo(unidade.getCodigo())
                .nome(unidade.getNome())
                .sigla(unidade.getSigla())
                .tipo(unidade.getTipo().name())
                .tituloTitular(unidade.getTituloTitular())
                .build();
    }
}

