package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para Subprocesso usado nas APIs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubprocessoDto {
    private Long codigo;

    @NotNull(message = "Código do processo é obrigatório")
    private Long processoCodigo;

    private Long unidadeCodigo;
    private Long mapaCodigo;
    private LocalDate dataLimiteEtapa1;
    private LocalDateTime dataFimEtapa1;
    private LocalDate dataLimiteEtapa2;
    private LocalDateTime dataFimEtapa2;
    private String situacaoId;
}