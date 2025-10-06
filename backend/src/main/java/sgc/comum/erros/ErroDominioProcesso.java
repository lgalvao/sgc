package sgc.comum.erros;

/**
 * Exceção lançada quando há violação de regras de negócio relacionadas a processos.
 * Usada especialmente em validações de estado e transições de processos.
 */
public class ErroDominioProcesso extends RuntimeException {
    public ErroDominioProcesso(String message) {
        super(message);
    }
    
    public ErroDominioProcesso(String message, Throwable cause) {
        super(message, cause);
    }
}