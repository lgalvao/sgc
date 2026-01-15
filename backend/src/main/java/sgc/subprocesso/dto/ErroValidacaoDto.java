package sgc.subprocesso.dto;

import lombok.Builder;

/**
 * DTO de erro de validação retornado na lista de erros de {@link ValidacaoCadastroDto}.
 */
@Builder
public record ErroValidacaoDto(
    String tipo, // "ATIVIDADE_SEM_CONHECIMENTO", "SEM_ATIVIDADES", etc.
    Long atividadeCodigo,
    String descricaoAtividade,
    String mensagem
) {}
