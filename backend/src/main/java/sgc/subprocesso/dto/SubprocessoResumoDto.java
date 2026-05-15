package sgc.subprocesso.dto;

import lombok.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;

@Builder
public record SubprocessoResumoDto(
        Long codigo,
        UnidadeResumoDto unidade,
        SituacaoSubprocesso situacao,
        LocalDateTime dataLimiteEtapa1,
        LocalDateTime dataFimEtapa1,
        LocalDateTime dataLimiteEtapa2,
        LocalDateTime dataFimEtapa2,
        LocalDateTime ultimaDataLimite,
        Long codProcesso,
        Long codUnidade,
        Long codMapa,
        String processoDescricao,
        LocalDateTime dataCriacaoProcesso,
        TipoProcesso tipoProcesso,
        boolean isEmAndamento,
        Integer etapaAtual) {

    public static SubprocessoResumoDto fromEntity(Subprocesso subprocesso) {
        Processo processo = subprocesso.getProcesso();
        Unidade unidade = subprocesso.getUnidade();
        if (processo == null || unidade == null) {
            throw new IllegalStateException("Subprocesso deve possuir processo e unidade associados");
        }

        LocalDateTime dataLimiteEtapa1 = subprocesso.getDataLimiteEtapa1();
        LocalDateTime dataLimiteEtapa2 = subprocesso.getDataLimiteEtapa2();
        LocalDateTime ultimaDataLimite = calcularUltimaDataLimite(dataLimiteEtapa1, dataLimiteEtapa2);

        return SubprocessoResumoDto.builder()
                .codigo(subprocesso.getCodigo())
                .unidade(UnidadeResumoDto.fromEntityObrigatoria(unidade))
                .situacao(subprocesso.getSituacao())
                .dataLimiteEtapa1(dataLimiteEtapa1)
                .dataFimEtapa1(subprocesso.getDataFimEtapa1())
                .dataLimiteEtapa2(dataLimiteEtapa2)
                .dataFimEtapa2(subprocesso.getDataFimEtapa2())
                .ultimaDataLimite(ultimaDataLimite)
                .codProcesso(processo.getCodigo())
                .codUnidade(unidade.getCodigo())
                .codMapa(subprocesso.getCodMapa())
                .processoDescricao(processo.getDescricao())
                .dataCriacaoProcesso(processo.getDataCriacao())
                .tipoProcesso(processo.getTipo())
                .isEmAndamento(subprocesso.isEmAndamento())
                .etapaAtual(subprocesso.getEtapaAtual())
                .build();
    }

    /**
     * Retorna a data limite mais recente entre etapa 1 e etapa 2.
     * Se não houver etapa 2, retorna a data da etapa 1.
     */
    private static LocalDateTime calcularUltimaDataLimite(LocalDateTime dataLimiteEtapa1, LocalDateTime dataLimiteEtapa2) {
        if (dataLimiteEtapa2 == null) {
            return dataLimiteEtapa1;
        }
        return dataLimiteEtapa1.isAfter(dataLimiteEtapa2) ? dataLimiteEtapa1 : dataLimiteEtapa2;
    }
}
