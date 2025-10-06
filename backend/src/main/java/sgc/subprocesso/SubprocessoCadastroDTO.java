package sgc.subprocesso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sgc.conhecimento.ConhecimentoDTO;

import java.util.List;

/**
 * DTO agregado retornado pelo endpoint GET /api/subprocessos/{id}/cadastro
 * Estrutura:
 * {
 *   subprocessoId: Long,
 *   unidadeSigla: String,
 *   atividades: [
 *     { id: Long, descricao: String, conhecimentos: [ConhecimentoDTO...] }
 *   ]
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubprocessoCadastroDTO {
    private Long subprocessoId;
    private String unidadeSigla;
    private List<AtividadeCadastroDTO> atividades;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AtividadeCadastroDTO {
        private Long id;
        private String descricao;
        private List<ConhecimentoDTO> conhecimentos;
    }
}