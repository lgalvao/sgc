package sgc.processo.erros;

/** Lançada quando um processo não possui unidades participantes definidas. */
public class ErroUnidadesNaoDefinidas extends RuntimeException {
    public ErroUnidadesNaoDefinidas(String message) {
        super(message);
    }
}
