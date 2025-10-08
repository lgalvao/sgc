package sgc.processo;

/**
 * Exceção lançada quando há violação de regras de negócio relacionadas a processos.
 * Usada especialmente em validações de estado e transições de processos.
 */
public class ProcessoErro extends RuntimeException {
    public ProcessoErro(String message) {
        super(message);
    }
    
    public ProcessoErro(String message, Throwable cause) {
        super(message, cause);
    }
}