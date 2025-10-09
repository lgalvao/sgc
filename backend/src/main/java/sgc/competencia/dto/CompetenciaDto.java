package sgc.competencia.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para Competência usado nas APIs (entrada/saída).
 * Contém apenas campos primários e referências por id para evitar expor entidades JPA.
 * Nota: mapaCodigo é opcional para compatibilidade com clientes existentes.
 */
public record CompetenciaDto(
    Long codigo,
    Long mapaCodigo,
    @NotBlank(message = "Descrição não pode ser vazia")
    String descricao
) {}
