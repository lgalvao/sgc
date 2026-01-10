package sgc.comum.erros;

import org.springframework.http.HttpStatus;

/**
 * Lançada quando o usuário não possui permissão para realizar uma operação.
 */
public class ErroAccessoNegado extends ErroNegocioBase {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    public ErroAccessoNegado(String message) {
        super(message, "ACESSO_NEGADO", HttpStatus.FORBIDDEN);
    }
}
