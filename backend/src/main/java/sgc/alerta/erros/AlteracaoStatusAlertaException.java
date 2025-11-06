package sgc.alerta.erros;

/**
 * Exceção lançada quando não é possível alterar o status de um alerta.
 * <p>
 * Ocorre quando operações como marcar como lido ou marcar como não lido
 * falham, impedindo a alteração do estado do alerta.
 */
public class AlteracaoStatusAlertaException extends RuntimeException {
    public AlteracaoStatusAlertaException(String message) {
        super(message);
    }

    public AlteracaoStatusAlertaException(String message, Throwable cause) {
        super(message, cause);
    }
}
