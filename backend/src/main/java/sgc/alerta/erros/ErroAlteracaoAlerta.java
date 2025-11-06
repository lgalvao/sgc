package sgc.alerta.erros;

/**
 * Exceção lançada quando não é possível alterar o status de um alerta.
 * <p>
 * Ocorre quando operações como marcar como lido ou marcar como não lido
 * falham, impedindo a alteração do estado do alerta.
 */
public class ErroAlteracaoAlerta extends RuntimeException {
    public ErroAlteracaoAlerta(String message) {
        super(message);
    }

    public ErroAlteracaoAlerta(String message, Throwable cause) {
        super(message, cause);
    }
}
