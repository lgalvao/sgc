package sgc.comum.erros;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import java.util.Map;

@Getter
public abstract class ErroNegocioBase extends RuntimeException implements ErroNegocio {
    private final String code;
    private final HttpStatus status;
    private final Map<String, ?> details;

    protected ErroNegocioBase(String message, String code, HttpStatus status) {
        this(message, code, status, null);
    }

    protected ErroNegocioBase(String message, String code, HttpStatus status, Map<String, ?> details) {
        super(message);
        this.code = code;
        this.status = status;
        this.details = details;
    }
}
