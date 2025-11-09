package sgc.atividade.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sgc.comum.json.SanitizeHtml;

/**
 * DTO para Atividade usado nas APIs (entrada/saída).
 * Contém apenas campos primários e referências por codigo para evitar expor entidades JPA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtividadeDto {
    /** O código da atividade. */
    private Long codigo;
    /** O código do mapa ao qual a atividade pertence. */
    private Long mapaCodigo;

    /** A descrição da atividade. */
    @NotBlank(message = "Descrição não pode ser vazia")
    @SanitizeHtml
    private String descricao;
}
