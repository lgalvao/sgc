package sgc.subprocesso.internal.erros;

import org.springframework.http.HttpStatus;
import sgc.comum.erros.ErroNegocioBase;

/**
 * Lançada quando atividades devem ser importadas para um subprocesso em situação inválida.
 *
 * <p>Exemplo: tentar importar atividades para um subprocesso que não está em
 * 'CADASTRO_EM_ANDAMENTO'.
 */
public class ErroAtividadesEmSituacaoInvalida extends ErroNegocioBase {
    public ErroAtividadesEmSituacaoInvalida(String message) {
        super(message, "ATIVIDADES_SITUACAO_INVALIDA", HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
