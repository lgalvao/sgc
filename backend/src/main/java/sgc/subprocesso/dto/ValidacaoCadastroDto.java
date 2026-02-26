package sgc.subprocesso.dto;

import lombok.*;

import java.util.*;

/**
 * DTO de resposta para validação de cadastro de subprocesso.
 */
@Builder
public record ValidacaoCadastroDto(
        Boolean valido,
        List<ErroValidacaoDto> erros
) {
}
