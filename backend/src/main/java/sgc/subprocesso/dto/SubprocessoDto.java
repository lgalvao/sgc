package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import sgc.subprocesso.model.SituacaoSubprocesso;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubprocessoDto {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
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
