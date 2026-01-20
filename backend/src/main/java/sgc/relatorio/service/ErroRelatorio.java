package sgc.relatorio.service;

import lombok.Getter;
import org.openpdf.text.DocumentException;
import sgc.comum.erros.ErroInterno;

@Getter
public class ErroRelatorio extends ErroInterno {
    final DocumentException causa;

    public ErroRelatorio(String msg, DocumentException causa) {
        super(msg);
        this.causa = causa;
    }
}
