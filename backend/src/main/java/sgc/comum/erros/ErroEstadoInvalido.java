package sgc.comum.erros;

/**
 * Exceção lançada quando uma operação não pode ser executada devido ao estado atual de um recurso ou entidade.
 * Usada para sinalizar retornos 409 Conflict.
 */
public class ErroEstadoInvalido extends RuntimeException {
    public ErroEstadoInvalido(String message) {
        super(message);
    }

    public ErroEstadoInvalido(String recurso, String estadoEsperado, String estadoAtual) {
        super("Operação inválida para '%s'. Estado esperado: '%s', Estado atual: '%s'.".formatted(recurso, estadoEsperado, estadoAtual));
    }
}
