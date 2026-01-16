package sgc.subprocesso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.subprocesso.model.SituacaoSubprocesso;

import java.time.LocalDateTime;

/**
 * DTO de resposta contendo dados de um subprocesso.
 * 
 * <p>Usado exclusivamente como saída de API. Para criar ou atualizar
 * subprocessos, use {@link CriarSubprocessoRequest} ou {@link AtualizarSubprocessoRequest}.
 * 
 * <p>Requer @NoArgsConstructor e @Setter para uso em testes.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
