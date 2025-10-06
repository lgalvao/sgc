package sgc.comum.erros;

/**
 * Exceção lançada quando há erro de domínio relacionado ao subprocesso.
 * Usada para validações de regras de negócio do subprocesso.
 */
public class ErroDominioSubprocesso extends RuntimeException {
    public ErroDominioSubprocesso(String message) {
        super(message);
    }
}