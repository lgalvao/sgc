package sgc.comum.erros;

import org.springframework.http.*;

public class ErroAutenticacao extends ErroNegocioBase {

    public ErroAutenticacao(String mensagem) {
        super(mensagem, "NAO_AUTORIZADO", HttpStatus.UNAUTHORIZED);
    }
}
