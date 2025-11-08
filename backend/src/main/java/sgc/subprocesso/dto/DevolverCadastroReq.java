package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de devolução de cadastro (CDU-13 item 9 e CDU-14 item 10).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevolverCadastroReq {
    /** O motivo da devolução. */
    @NotBlank(message = "Motivo da devolução é obrigatório")
    private String motivo;

    /** Observações adicionais. */
    private String observacoes;
}
