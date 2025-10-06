package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request para devolver validação do mapa (CDU-20 item 7).
 */
public record DevolverValidacaoRequest(
    @NotBlank String justificativa
) {}