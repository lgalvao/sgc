package sgc.comum.erros;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErroSubApi {
    private String object;
    private String field;
    private Object rejectedValue;
    private String message;
}