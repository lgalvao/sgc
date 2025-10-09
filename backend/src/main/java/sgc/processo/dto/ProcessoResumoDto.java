package sgc.processo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProcessoResumoDto(
    Long codigo,
    String descricao,
    String situacao,
    String tipo,
    LocalDate dataLimite,
    LocalDateTime dataCriacao,
    Long unidadeCodigo,
    String unidadeNome
) {}