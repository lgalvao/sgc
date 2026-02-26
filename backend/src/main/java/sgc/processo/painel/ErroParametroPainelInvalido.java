package sgc.processo.painel;

import org.springframework.http.*;
import sgc.comum.erros.*;

/**
 * Lançada quando um parâmetro obrigatório para o painel não é fornecido.
 */
public class ErroParametroPainelInvalido extends ErroNegocioBase {

    public ErroParametroPainelInvalido(String message) {
        super(message, "PARAMETRO_PAINEL_INVALIDO", HttpStatus.BAD_REQUEST);
    }
}
