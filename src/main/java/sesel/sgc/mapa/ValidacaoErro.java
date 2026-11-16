package sesel.sgc.mapa;

/**
 * Exceção lançada quando ocorre uma violação de regra de negócio.
 */
public class ValidacaoErro extends RuntimeException {

    public ValidacaoErro(String message) {
        super(message);
    }

    public ValidacaoErro(String message, Throwable cause) {
        super(message, cause);
    }
}
