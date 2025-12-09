package sgc.subprocesso.dto;

import lombok.Builder;
import lombok.Data;
import sgc.subprocesso.model.SituacaoSubprocesso;

/**
 * DTO que representa o status atual de um subprocesso.
 * 
 * <p>Usado para retornar informações básicas de status sem precisar
 * carregar todo o processo ou subprocesso completo.
 */
@Data
@Builder
public class SubprocessoStatusDto {
    private Long codigo;
    private SituacaoSubprocesso situacao;
    private String situacaoLabel;
}
