package sgc.alerta.erros;

/**
 * Exceção lançada quando não é possível alterar o status de um alerta.
 * <p>
 * Ocorre quando operações como marcar como lido ou marcar como não lido
 * falham, impedindo a alteração do estado do alerta.
 */
public class ErroAlerta extends RuntimeException {
    public ErroAlerta(String message) {
        super(message);
    }

    public ErroAlerta(String message, Throwable cause) {
        super(message, cause);
    }
}
