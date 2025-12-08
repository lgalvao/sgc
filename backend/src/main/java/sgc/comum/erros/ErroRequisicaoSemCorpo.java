package sgc.comum.erros;

/** Lançada quando uma requisição esperada contém um corpo vazio ou nulo. */
public class ErroRequisicaoSemCorpo extends RuntimeException {
    public ErroRequisicaoSemCorpo(String message) {
        super(message);
    }
}
