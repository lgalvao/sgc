package sgc.subprocesso.dto;

import lombok.*;
import org.jspecify.annotations.*;
import sgc.organizacao.dto.*;

import java.time.*;

@Builder
public record SubprocessoResumoDto(
        Long codigo,
        UnidadeResumoDto unidade,
        String situacao,
        LocalDateTime dataLimiteEtapa1,
        @Nullable LocalDateTime dataFimEtapa1,
        @Nullable LocalDateTime dataLimiteEtapa2,
        @Nullable LocalDateTime dataFimEtapa2,
        @Nullable LocalDateTime ultimaDataLimite,
        Long codProcesso,
        Long codUnidade,
        @Nullable Long codMapa,
        String processoDescricao,
        LocalDateTime dataCriacaoProcesso,
        String tipoProcesso,
        boolean isEmAndamento,
        @Nullable Integer etapaAtual) {
}
