package sgc.auth.exceptions;

/**
 * Exceção lançada quando as credenciais são inválidas no Sistema Acesso.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super();
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}