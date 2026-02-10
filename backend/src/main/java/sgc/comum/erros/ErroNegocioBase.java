package sgc.comum.erros;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class ErroNegocioBase extends RuntimeException implements ErroNegocio {
    private final String code;
    private final HttpStatus status;
    private final Map<String, ?> details;

    protected ErroNegocioBase(String message, String code, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
        this.details = new HashMap<>();
    }

    protected ErroNegocioBase(String message, String code, HttpStatus status, Map<String, ?> details) {
        super(message);
        this.code = code;
        this.status = status;
        this.details = details;
    }

    protected ErroNegocioBase(String message, String code, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.status = status;
        this.details = new HashMap<>();
    }
}
