package sgc.processo;

/**
 * Exceção lançada quando há violação de regras de negócio relacionadas a processos.
 * Usada especialmente em validações de estado e transições de processos.
 */
public class ErroProcesso extends RuntimeException {
    public ErroProcesso(String message) {
        super(message);
    }
    
    public ErroProcesso(String message, Throwable cause) {
        super(message, cause);
    }
}