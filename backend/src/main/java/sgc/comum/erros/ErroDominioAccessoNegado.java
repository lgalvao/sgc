package sgc.comum.erros;

public class ErroDominioAccessoNegado extends RuntimeException {
    public ErroDominioAccessoNegado(String message) {
        super(message);
    }
}