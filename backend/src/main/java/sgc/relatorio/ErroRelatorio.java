package sgc.relatorio;

import lombok.*;
import org.openpdf.text.*;
import sgc.comum.erros.*;

@Getter
public class ErroRelatorio extends ErroInterno {
    final DocumentException causa;

    public ErroRelatorio(String msg, DocumentException causa) {
        super(msg);
        this.causa = causa;
    }
}
