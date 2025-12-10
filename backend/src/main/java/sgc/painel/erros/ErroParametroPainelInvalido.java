package sgc.painel.erros;

/**
 * Lançada quando um parâmetro obrigatório para o painel não é fornecido.
 */
public class ErroParametroPainelInvalido extends RuntimeException {
    public ErroParametroPainelInvalido(String message) {
        super(message);
    }
}
