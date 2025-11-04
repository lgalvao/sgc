package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import sgc.subprocesso.modelo.SituacaoSubprocesso;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubprocessoDto {
    private Long codigo;

    @NotNull(message = "O código do processo é obrigatório")
    private Long codProcesso;

    private Long codUnidade;
    private Long codMapa;

    private LocalDateTime dataLimiteEtapa1;
    private LocalDateTime dataFimEtapa1;

    private LocalDateTime dataLimiteEtapa2;
    private LocalDateTime dataFimEtapa2;

    private SituacaoSubprocesso situacao;
}
