package sgc.comum.erros;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class ErroValidacao extends RuntimeException {
    private final Map<String, ?> details;

    public ErroValidacao(String message) {
        super(message);
        this.details = null;
    }

    public ErroValidacao(String message, Map<String, ?> details) {
        super(message);
        this.details = details;
    }

    public Map<String, ?> getDetails() {
        return details;
    }
}