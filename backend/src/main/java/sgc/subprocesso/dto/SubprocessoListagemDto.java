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
        Long codigo,
        UnidadeDto unidade,
        SituacaoSubprocesso situacao,
        LocalDateTime dataLimiteEtapa1,
        @Nullable LocalDateTime dataFimEtapa1,
        @Nullable LocalDateTime dataLimiteEtapa2,
        @Nullable LocalDateTime dataFimEtapa2,
        @Nullable Long codProcesso,
        @Nullable Long codUnidade,
        @Nullable Long codMapa,
        String processoDescricao,
        LocalDateTime dataCriacaoProcesso,
        TipoProcesso tipoProcesso,
        boolean isEmAndamento,
        @Nullable Integer etapaAtual) {

    public static SubprocessoListagemDto fromEntity(Subprocesso subprocesso) {
        Processo processo = subprocesso.getProcesso();
        Unidade unidade = subprocesso.getUnidade();
        if (processo == null || unidade == null) {
            throw new IllegalStateException("Subprocesso deve possuir processo e unidade associados");
        }

        return SubprocessoListagemDto.builder()
                .codigo(subprocesso.getCodigo())
                .unidade(paraUnidadeResumo(unidade))
                .situacao(subprocesso.getSituacao())
                .dataLimiteEtapa1(subprocesso.getDataLimiteEtapa1())
                .dataFimEtapa1(subprocesso.getDataFimEtapa1())
                .dataLimiteEtapa2(subprocesso.getDataLimiteEtapa2())
                .dataFimEtapa2(subprocesso.getDataFimEtapa2())
                .codProcesso(subprocesso.getCodProcesso())
                .codUnidade(subprocesso.getCodUnidade())
                .codMapa(subprocesso.getCodMapa())
                .processoDescricao(processo.getDescricao())
                .dataCriacaoProcesso(processo.getDataCriacao())
                .tipoProcesso(processo.getTipo())
                .isEmAndamento(subprocesso.isEmAndamento())
                .etapaAtual(subprocesso.getEtapaAtual())
                .build();
    }

    private static UnidadeDto paraUnidadeResumo(Unidade unidade) {
        return UnidadeDto.builder()
                .codigo(unidade.getCodigo())
                .nome(unidade.getNome())
                .sigla(unidade.getSigla())
                .tipo(unidade.getTipo().name())
                .tituloTitular(unidade.getTituloTitular())
                .build();
    }
}
