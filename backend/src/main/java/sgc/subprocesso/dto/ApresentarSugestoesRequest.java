package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request para apresentar sugestões ao mapa de competências (CDU-19 item 8).
 * Usado pelo CHEFE da unidade para fornecer feedback sobre o mapa disponibilizado.
 *
 * @param sugestoes Texto com as sugestões do CHEFE (obrigatório)
 */
public record ApresentarSugestoesRequest(
    @NotBlank(message = "As sugestões são obrigatórias")
    String sugestoes
) {}