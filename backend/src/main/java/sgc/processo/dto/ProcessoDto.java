package sgc.processo.dto;

import sgc.comum.enums.SituacaoProcesso;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProcessoDto(
    Long codigo,
    LocalDateTime dataCriacao,
    LocalDateTime dataFinalizacao,
    LocalDate dataLimite,
    String descricao,
    SituacaoProcesso situacao,
    String tipo
) {}
