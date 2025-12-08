package sgc.comum.erros;

public class ErroAccessoNegado extends RuntimeException {
    public ErroAccessoNegado(String message) {
        super(message);
    }
}
