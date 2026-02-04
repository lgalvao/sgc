package sgc.subprocesso.erros;

import org.springframework.http.HttpStatus;
import sgc.comum.erros.ErroNegocioBase;

/**
 * Lançada quando um mapa está em uma situação que não permite a operação solicitada.
 *
 * <p>Exemplo: tentar ajustar um mapa que não está em estado 'REVISAO_CADASTRO_HOMOLOGADA' ou
 * 'MAPA_AJUSTADO'.
 */
public class ErroMapaEmSituacaoInvalida extends ErroNegocioBase {

    public ErroMapaEmSituacaoInvalida(String message) {
        super(message, "MAPA_SITUACAO_INVALIDA", HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
