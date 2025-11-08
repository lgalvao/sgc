package sgc.atividade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import sgc.comum.json.SanitizeHtml;

/**
 * DTO para Conhecimento usado nas APIs (entrada/saída).
 * Contém apenas campos primários e referência por codigo para evitar expor entidades JPA.
 */
public record ConhecimentoDto(
    Long codigo,
    @NotNull(message = "Código da atividade é obrigatório")
    Long atividadeCodigo,
    @NotBlank(message = "Descrição não pode ser vazia")
    @SanitizeHtml
    String descricao
) {
}