package sgc.alerta;

import sgc.comum.erros.*;

public class ErroEnvioEmail extends ErroInterno {
    public ErroEnvioEmail(String destinatario, Throwable cause) {
        super("Falha ao enviar e-mail para '%s'.".formatted(destinatario), cause);
    }
}
