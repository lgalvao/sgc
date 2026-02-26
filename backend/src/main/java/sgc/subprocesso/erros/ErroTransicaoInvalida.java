package sgc.subprocesso.erros;

import org.springframework.http.*;
import sgc.comum.erros.*;

/**
 * Exceção lançada quando uma transição de situação entre estados de um subprocesso é inválida.
 */
public class ErroTransicaoInvalida extends ErroNegocioBase {

    public ErroTransicaoInvalida(String message) {
        super(message, "TRANSICAO_INVALIDA", HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
