package sgc.auth.exceptions;

/**
 * Exceção que representa falhas na comunicação ou respostas inválidas
 * vindas do Sistema Acesso (erros 4xx/5xx, timeouts, parsing, etc).
 */
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException() {
        super();
    }

    public ExternalServiceException(String message) {
        super(message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}