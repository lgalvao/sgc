package sgc.comum.erros;

import org.springframework.http.HttpStatus;

/**
 * Lançada quando o usuário não possui permissão para realizar uma operação.
 */
public class ErroAccessoNegado extends ErroNegocioBase {

    public ErroAccessoNegado(String message) {
        super(message, "ACESSO_NEGADO", HttpStatus.FORBIDDEN);
    }
}
