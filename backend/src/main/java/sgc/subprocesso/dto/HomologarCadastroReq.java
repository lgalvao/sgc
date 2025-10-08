package sgc.subprocesso.dto;

/**
 * DTO para requisição de homologação de cadastro (CDU-13 item 11 e CDU-14 item 12).
 */
public record HomologarCadastroReq(
    String observacoes
) {}