package sgc.subprocesso;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para Subprocesso usado nas APIs.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubprocessoDTO {
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