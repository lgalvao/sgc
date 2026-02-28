package sgc.comum.erros;

import lombok.*;

@Builder
public record ErroSubApi(
        String object,
        String field,
        String message) {
}
