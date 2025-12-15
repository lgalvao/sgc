package sgc.unidade.erros;

import org.springframework.http.HttpStatus;
import sgc.comum.erros.ErroNegocioBase;

/**
 * Lançada quando uma unidade esperada não é encontrada no sistema.
 *
 * <p>Exemplo: tentar notificar a unidade 'SEDOC' quando ela não existe no banco de dados.
 */
public class ErroUnidadeNaoEncontrada extends ErroNegocioBase {
    public ErroUnidadeNaoEncontrada(String message) {
        super(message, "UNIDADE_NAO_ENCONTRADA", HttpStatus.NOT_FOUND);
    }
}
