package sgc.comum.erros;

/**
 * Exceção lançada quando um domínio (entidade) não é encontrado.
 * Usada para sinalizar retornos 404 nas camadas de serviço/controle.
 */
public class ErroDominioNaoEncontrado extends RuntimeException {
    public ErroDominioNaoEncontrado(String message) {
        super(message);
    }

    public ErroDominioNaoEncontrado(String entidade, Object id) {
        super("'%s' com id '%s' não encontrado(a).".formatted(entidade, id));
    }
}