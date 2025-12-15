package sgc.comum.erros;

import org.springframework.http.HttpStatus;

/**
 * Lançada quando uma requisição esperada contém um corpo vazio ou nulo.
 */
public class ErroRequisicaoSemCorpo extends ErroNegocioBase {
    public ErroRequisicaoSemCorpo(String message) {
        super(message, "REQUISICAO_SEM_CORPO", HttpStatus.BAD_REQUEST);
    }
}
