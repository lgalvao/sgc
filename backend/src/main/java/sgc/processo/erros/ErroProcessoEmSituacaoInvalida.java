package sgc.processo.erros;

/**
 * Lançada quando um processo está em uma situação que não permite a operação solicitada.
 *
 * <p>Exemplo: tentar iniciar um processo que não está no estado 'CRIADO'.
 */
public class ErroProcessoEmSituacaoInvalida extends RuntimeException {
    public ErroProcessoEmSituacaoInvalida(String message) {
        super(message);
    }
}
