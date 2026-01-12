package sgc.subprocesso.erros;

import sgc.comum.erros.ErroSituacaoInvalida;

/**
 * Lançada quando atividades devem ser importadas para um subprocesso em situação inválida.
 *
 * <p>Exemplo: tentar importar atividades para um subprocesso que não está em
 * 'CADASTRO_EM_ANDAMENTO'.
 */
public class ErroAtividadesEmSituacaoInvalida extends ErroSituacaoInvalida {

    public ErroAtividadesEmSituacaoInvalida(String message) {
        super(message, "ATIVIDADES_SITUACAO_INVALIDA");
    }
}
