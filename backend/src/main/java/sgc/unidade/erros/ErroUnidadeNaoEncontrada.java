package sgc.unidade.erros;

/**
 * Lançada quando uma unidade esperada não é encontrada no sistema.
 * <p>
 * Exemplo: tentar notificar a unidade 'SEDOC' quando ela não existe no banco de dados.
 */
public class ErroUnidadeNaoEncontrada extends RuntimeException {
    public ErroUnidadeNaoEncontrada(String message) {
        super(message);
    }
}
