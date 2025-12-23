package sgc.processo.internal.erros;

import org.springframework.http.HttpStatus;
import sgc.comum.erros.ErroNegocioBase;

/**
 * Lançada quando um processo está em uma situação que não permite a operação solicitada.
 *
 * <p>Exemplo: tentar iniciar um processo que não está no estado 'CRIADO'.
 */
public class ErroProcessoEmSituacaoInvalida extends ErroNegocioBase {
    public ErroProcessoEmSituacaoInvalida(String message) {
        super(message, "PROCESSO_SITUACAO_INVALIDA", HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
