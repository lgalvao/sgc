package sgc.comum.erros;

/**
 * Exceção que representa falhas na comunicação ou respostas inválidas
 * vindas do Sistema Acesso (erros 4xx/5xx, timeouts, parsing, etc).
 */
public class ErroServicoExterno extends RuntimeException {
    public ErroServicoExterno(String message) {
        super(message);
    }

    public ErroServicoExterno(String message, Throwable cause) {
        super(message, cause);
    }
}