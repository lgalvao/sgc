package sgc.alerta.erros;

import org.springframework.http.HttpStatus;
import sgc.comum.erros.ErroNegocioBase;

/**
 * Exceção lançada quando não é possível alterar o status de um alerta.
 *
 * <p>Ocorre quando operações como marcar como lido ou marcar como não lido falham, impedindo a
 * alteração do estado do alerta.
 */
public class ErroAlerta extends ErroNegocioBase {
    public ErroAlerta(String message) {
        super(message, "ERRO_ALERTA", HttpStatus.CONFLICT);
    }
}
