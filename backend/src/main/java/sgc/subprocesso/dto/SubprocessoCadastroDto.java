package sgc.subprocesso.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubprocessoCadastroDto {
    private Long subprocessoId;
    private String unidadeSigla;
    private List<AtividadeCadastroDTO> atividades;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AtividadeCadastroDTO {
        private Long id;
        private String descricao;
        private List<ConhecimentoDto> conhecimentos;
    }
}