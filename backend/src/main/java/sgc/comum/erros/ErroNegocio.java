package sgc.comum.erros;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Interface para exceções de negócio que seguem o contrato padronizado.
 * Toda exceção de domínio deve implementar esta interface para garantir
 * que o RestExceptionHandler possa extrair informações de forma uniforme.
 */
public interface ErroNegocio {
    String getCode();
    HttpStatus getStatus();
    default Map<String, ?> getDetails() { return null; }

    String getMessage();
}
