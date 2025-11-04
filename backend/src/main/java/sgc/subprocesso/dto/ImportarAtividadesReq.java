package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para a requisição de importação de atividades de outro subprocesso.
 * CDU-08
 *
 * @param subprocessoOrigemId O código do subprocesso do qual as atividades serão importadas.
 */
public record ImportarAtividadesReq(
    @NotNull Long subprocessoOrigemId
) {}