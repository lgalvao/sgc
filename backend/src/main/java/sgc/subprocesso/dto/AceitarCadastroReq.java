package sgc.subprocesso.dto;

/**
 * DTO para requisição de aceite de cadastro (CDU-13 item 10 e CDU-14 item 11).
 *
 * @param observacoes Observações adicionais.
 */
public record AceitarCadastroReq(
    String observacoes
) {}