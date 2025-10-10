package sgc.subprocesso.dto;

import sgc.conhecimento.dto.ConhecimentoDto;

import java.util.List;

/**
 * DTO agregado retornado pelo endpoint GET /api/subprocessos/{id}/cadastro
 * Estrutura:
 * {
 *   subprocessoId: Long,
 *   unidadeSigla: String,
 *   atividades: [
 *     { id: Long, descricao: String, conhecimentos: [ConhecimentoDto...] }
 *   ]
 * }
 */
public record SubprocessoCadastroDto(
    Long subprocessoId,
    String unidadeSigla,
    List<AtividadeCadastroDTO> atividades
) {
    public record AtividadeCadastroDTO(
        Long id,
        String descricao,
        List<ConhecimentoDto> conhecimentos
    ) {}
}