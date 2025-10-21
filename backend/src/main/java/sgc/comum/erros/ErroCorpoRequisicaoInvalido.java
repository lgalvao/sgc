package sgc.comum.erros;

/**
 * Exceção lançada quando o corpo da requisição está nulo ou malformado.
 * Usada para sinalizar retornos 400 Bad Request.
 */
public class ErroCorpoRequisicaoInvalido extends RuntimeException {
    public ErroCorpoRequisicaoInvalido(String message) {
        super(message);
    }
}
