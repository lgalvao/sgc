package sgc.exception;

/**
 * Exceção lançada quando um domínio (entidade) não é encontrado.
 * Usada para sinalizar retornos 404 nas camadas de serviço/controller.
 */
public class DomainNotFoundException extends RuntimeException {
    public DomainNotFoundException(String message) {
        super(message);
    }
}