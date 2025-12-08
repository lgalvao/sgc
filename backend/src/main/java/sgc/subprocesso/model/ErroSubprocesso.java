package sgc.subprocesso.model;

/**
 * Exceção lançada quando há erro de domínio relacionado ao subprocesso. Usada para validações de
 * regras de negócio do subprocesso.
 */
public class ErroSubprocesso extends RuntimeException {
    public ErroSubprocesso(String message) {
        super(message);
    }
}
