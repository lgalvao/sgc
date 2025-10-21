package sgc.comum.erros;

/**
 * Exceção lançada quando um parâmetro fornecido é inválido ou obrigatório e não foi informado.
 * Usada para sinalizar retornos 400 Bad Request.
 */
public class ErroParametroInvalido extends RuntimeException {
    public ErroParametroInvalido(String message) {
        super(message);
    }

    public ErroParametroInvalido(String parametro, String motivo) {
        super("Parâmetro '%s' inválido: %s".formatted(parametro, motivo));
    }
}
