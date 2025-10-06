package sgc.comum.erros;

/**
 * Exceção lançada quando as credenciais são inválidas no Sistema Acesso.
 */
public class ErroCredenciaisInvalidas extends RuntimeException {
    public ErroCredenciaisInvalidas(String message) {
        super(message);
    }
}