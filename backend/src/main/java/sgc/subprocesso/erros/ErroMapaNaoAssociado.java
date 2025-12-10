package sgc.subprocesso.erros;

/**
 * Lançada quando um subprocesso não possui um mapa associado.
 */
public class ErroMapaNaoAssociado extends RuntimeException {
    public ErroMapaNaoAssociado(String message) {
        super(message);
    }
}
