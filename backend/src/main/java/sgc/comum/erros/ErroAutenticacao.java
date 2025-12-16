package sgc.comum.erros;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class ErroAutenticacao extends RuntimeException {
    public ErroAutenticacao(String mensagem) {
        super(mensagem);
    }
}
