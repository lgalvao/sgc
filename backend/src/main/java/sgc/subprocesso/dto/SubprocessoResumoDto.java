package sgc.subprocesso.dto;

import lombok.Builder;
import sgc.organizacao.dto.UnidadeResumoDto;

import java.time.LocalDateTime;

@Builder
public record SubprocessoResumoDto(
        Long codigo,
        UnidadeResumoDto unidade,
        String situacao,
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
        String tipoProcesso,
        boolean isEmAndamento,
        Integer etapaAtual) {
}
