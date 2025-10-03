package sgc.exception;

public class DomainAccessDeniedException extends RuntimeException {
    public DomainAccessDeniedException(String message) {
        super(message);
    }
}