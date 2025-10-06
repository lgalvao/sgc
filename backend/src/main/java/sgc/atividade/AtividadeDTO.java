package sgc.atividade;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para Atividade usado nas APIs (entrada/saída).
 * Contém apenas campos primários e referências por id para evitar expor entidades JPA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AtividadeDTO {
    private Long codigo;
    private Long mapaCodigo;

    @NotBlank(message = "descricao não pode ser vazia")
    private String descricao;
}
