package sgc.subprocesso.dto;

import lombok.Builder;

import java.util.List;

/**
 * DTO de resposta para validação de cadastro de subprocesso.
 */
@Builder
public record ValidacaoCadastroDto(
        Boolean valido,
        List<Erro> erros
) {
    /**
     * DTO de erro de validação retornado na lista de erros.
     */
    @Builder
    public record Erro(
            String tipo,
            Long atividadeCodigo,
            String descricaoAtividade,
            String mensagem
    ) {
    }
}
