package sgc.subprocesso.erros;

import org.springframework.http.HttpStatus;
import sgc.comum.erros.ErroNegocioBase;

/**
 * Exceção lançada quando uma transição de situação entre estados de um subprocesso é inválida.
 */
public class ErroTransicaoInvalida extends ErroNegocioBase {

    public ErroTransicaoInvalida(String message) {
        super(message, "TRANSICAO_INVALIDA", HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
