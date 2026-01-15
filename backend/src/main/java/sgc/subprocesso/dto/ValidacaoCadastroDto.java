package sgc.subprocesso.dto;

import lombok.Builder;

import java.util.List;

/**
 * DTO de resposta para validação de cadastro de subprocesso.
 */
@Builder
public record ValidacaoCadastroDto(
    Boolean valido,
    List<ErroValidacaoDto> erros
) {}
