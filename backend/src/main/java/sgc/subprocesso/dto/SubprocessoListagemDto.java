package sgc.subprocesso.dto;

import lombok.*;
import org.jspecify.annotations.*;
import sgc.organizacao.dto.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;

@Builder
public record SubprocessoListagemDto(
        Long codigo,
        UnidadeResumoDto unidade,
        SituacaoSubprocesso situacao,
        LocalDateTime dataLimiteEtapa1,
        @Nullable LocalDateTime dataFimEtapa1,
        @Nullable LocalDateTime dataLimiteEtapa2,
        @Nullable LocalDateTime dataFimEtapa2,
        Long codProcesso,
        Long codUnidade,
        @Nullable Long codMapa,
        String processoDescricao,
        LocalDateTime dataCriacaoProcesso,
        TipoProcesso tipoProcesso,
        boolean isEmAndamento,
        @Nullable Integer etapaAtual) {

    public static SubprocessoListagemDto fromEntity(Subprocesso subprocesso) {
        return fromResumo(SubprocessoResumoDto.fromEntity(subprocesso));
    }

    static SubprocessoListagemDto fromResumo(SubprocessoResumoDto resumo) {
        return SubprocessoListagemDto.builder()
                .codigo(resumo.codigo())
                .unidade(resumo.unidade())
                .situacao(resumo.situacao())
                .dataLimiteEtapa1(resumo.dataLimiteEtapa1())
                .dataFimEtapa1(resumo.dataFimEtapa1())
                .dataLimiteEtapa2(resumo.dataLimiteEtapa2())
                .dataFimEtapa2(resumo.dataFimEtapa2())
                .codProcesso(resumo.codProcesso())
                .codUnidade(resumo.codUnidade())
                .codMapa(resumo.codMapa())
                .processoDescricao(resumo.processoDescricao())
                .dataCriacaoProcesso(resumo.dataCriacaoProcesso())
                .tipoProcesso(resumo.tipoProcesso())
                .isEmAndamento(resumo.isEmAndamento())
                .etapaAtual(resumo.etapaAtual())
                .build();
    }
}
