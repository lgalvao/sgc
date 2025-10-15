package sgc.atividade.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para Atividade usado nas APIs (entrada/saída).
 * Contém apenas campos primários e referências por id para evitar expor entidades JPA.
 */
public record AtividadeDto(
    Long codigo,
    Long mapaCodigo,

    @NotBlank(message = "Descrição não pode ser vazia")
    String descricao
) {}
