package sgc.processo.erros;

import sgc.comum.erros.ErroSituacaoInvalida;

/**
 * Lançada quando um processo está em uma situação que não permite a operação solicitada.
 *
 * <p>Exemplo: tentar iniciar um processo que não está no estado 'CRIADO'.
 */
public class ErroProcessoEmSituacaoInvalida extends ErroSituacaoInvalida {

    public ErroProcessoEmSituacaoInvalida(String message) {
        super(message, "PROCESSO_SITUACAO_INVALIDA");
    }
}
