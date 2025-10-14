package sgc.subprocesso.dto;

import lombok.Builder;
import lombok.Getter;
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
@Getter
@Builder
public class SubprocessoCadastroDto {
    private final Long subprocessoId;
    private final String unidadeSigla;
    private final List<AtividadeCadastroDTO> atividades;

    @Getter
    @Builder
    public static class AtividadeCadastroDTO {
        private final Long id;
        private final String descricao;
        private final List<ConhecimentoDto> conhecimentos;
    }
}