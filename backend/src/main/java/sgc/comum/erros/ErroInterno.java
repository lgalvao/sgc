package sgc.comum.erros;

/**
 * Exceção base para erros internos que indicam bugs, configuração incorreta ou violação de
 * invariantes do sistema.
 */
public abstract class ErroInterno extends RuntimeException {
    /**
     * Construtor com mensagem de erro.
     * A mensagem será logada mas não exposta ao usuário final.
     */
    protected ErroInterno(String message) {
        super(message);
    }
}
