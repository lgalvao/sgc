package sgc.subprocesso;

/**
 * DTO para requisição de aceite de cadastro (CDU-13 item 10 e CDU-14 item 11).
 */
public record AceitarCadastroRequest(
    String observacoes
) {}