package sgc.processo.erros;

import org.springframework.http.*;
import sgc.comum.erros.*;

/**
 * Exceção lançada quando há violação de regras de negócio relacionadas a processos. Usada
 * especialmente em validações de estado e transições de processos.
 */
public class ErroProcesso extends ErroNegocioBase {
    public ErroProcesso(String message) {
        super(message, "ERRO_PROCESSO", HttpStatus.CONFLICT);
    }
}
