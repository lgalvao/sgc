package sgc.processo.erros;

import org.springframework.http.HttpStatus;
import sgc.comum.erros.ErroNegocioBase;

/**
 * Lançada quando um processo não possui unidades participantes definidas.
 */
public class ErroUnidadesNaoDefinidas extends ErroNegocioBase {

    public ErroUnidadesNaoDefinidas(String message) {
        super(message, "UNIDADES_NAO_DEFINIDAS", HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
