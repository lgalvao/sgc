package sgc.painel;

import org.springframework.http.HttpStatus;
import sgc.comum.erros.ErroNegocioBase;

/**
 * Lançada quando um parâmetro obrigatório para o painel não é fornecido.
 */
public class ErroParametroPainelInvalido extends ErroNegocioBase {

    public ErroParametroPainelInvalido(String message) {
        super(message, "PARAMETRO_PAINEL_INVALIDO", HttpStatus.BAD_REQUEST);
    }
}
