package sgc.subprocesso.dto;

import lombok.*;
import sgc.subprocesso.model.SituacaoSubprocesso;

import java.time.LocalDateTime;

/**
 * DTO de resposta contendo dados de um subprocesso.
 * 
 * <p>Usado exclusivamente como saída de API. Para criar ou atualizar
 * subprocessos, use {@link CriarSubprocessoRequest} ou {@link AtualizarSubprocessoRequest}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubprocessoDto {

    private Long codigo;
    private Long codProcesso;
    private Long codUnidade;
    private Long codMapa;

    private LocalDateTime dataLimiteEtapa1;
    private LocalDateTime dataFimEtapa1;

    private LocalDateTime dataLimiteEtapa2;
    private LocalDateTime dataFimEtapa2;

    private SituacaoSubprocesso situacao;
}

