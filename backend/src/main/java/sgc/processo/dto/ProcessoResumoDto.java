package sgc.processo.dto;

import sgc.comum.enums.SituacaoProcesso;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProcessoResumoDto(
    Long codigo,
    String descricao,
    SituacaoProcesso situacao,
    String tipo,
    LocalDate dataLimite,
    LocalDateTime dataCriacao,
    Long unidadeCodigo,
    String unidadeNome
) {}
