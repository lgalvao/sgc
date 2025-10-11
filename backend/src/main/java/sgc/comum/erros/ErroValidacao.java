package sgc.comum.erros;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class ErroValidacao extends RuntimeException {
    public ErroValidacao(String message) {
        super(message);
    }
}