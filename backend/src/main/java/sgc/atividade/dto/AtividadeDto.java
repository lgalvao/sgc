package sgc.atividade.dto;

import jakarta.validation.constraints.NotBlank;
import sgc.comum.json.SanitizeHtml;

/**
 * DTO para Atividade usado nas APIs (entrada/saída).
 * Contém apenas campos primários e referências por codigo para evitar expor entidades JPA.
 *
 * @param codigo     O código da atividade.
 * @param mapaCodigo O código do mapa ao qual a atividade pertence.
 * @param descricao  A descrição da atividade.
 */
public record AtividadeDto(
        Long codigo,
        Long mapaCodigo,

        @NotBlank(message = "Descrição não pode ser vazia")
        @SanitizeHtml
        String descricao)
{
}
