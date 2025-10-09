package sgc.processo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProcessoDto(
    Long codigo,
    LocalDateTime dataCriacao,
    LocalDateTime dataFinalizacao,
    LocalDate dataLimite,
    String descricao,
    String situacao,
    String tipo
) {}