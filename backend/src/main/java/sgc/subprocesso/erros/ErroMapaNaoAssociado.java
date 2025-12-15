package sgc.subprocesso.erros;

import org.springframework.http.HttpStatus;
import sgc.comum.erros.ErroNegocioBase;

/**
 * Lançada quando um subprocesso não possui um mapa associado.
 */
public class ErroMapaNaoAssociado extends ErroNegocioBase {
    public ErroMapaNaoAssociado(String message) {
        super(message, "MAPA_NAO_ASSOCIADO", HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
