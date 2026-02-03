package sgc.subprocesso.erros;

import sgc.comum.erros.ErroSituacaoInvalida;

/**
 * Exceção lançada quando uma transição de situação entre estados de um subprocesso é inválida.
 */
public class ErroTransicaoInvalida extends ErroSituacaoInvalida {

    public ErroTransicaoInvalida(String message) {
        super(message, "TRANSICAO_INVALIDA");
    }
}
