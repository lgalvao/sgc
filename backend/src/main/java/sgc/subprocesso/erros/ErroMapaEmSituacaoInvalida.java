package sgc.subprocesso.erros;

import sgc.comum.erros.ErroSituacaoInvalida;

/**
 * Lançada quando um mapa está em uma situação que não permite a operação solicitada.
 *
 * <p>Exemplo: tentar ajustar um mapa que não está em estado 'REVISAO_CADASTRO_HOMOLOGADA' ou
 * 'MAPA_AJUSTADO'.
 */
public class ErroMapaEmSituacaoInvalida extends ErroSituacaoInvalida {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    public ErroMapaEmSituacaoInvalida(String message) {
        super(message, "MAPA_SITUACAO_INVALIDA");
    }
}
