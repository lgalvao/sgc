package sgc.subprocesso.erros;

/**
 * Lançada quando atividades devem ser importadas para um subprocesso em situação inválida.
 * <p>
 * Exemplo: tentar importar atividades para um subprocesso que não está em 'CADASTRO_EM_ANDAMENTO'.
 */
public class ErroAtividadesEmSituacaoInvalida extends RuntimeException {
    public ErroAtividadesEmSituacaoInvalida(String message) {
        super(message);
    }
}
