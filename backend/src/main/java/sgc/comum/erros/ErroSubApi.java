package sgc.comum.erros;

import lombok.Builder;

@Builder
public record ErroSubApi(
        String objeto,
        String campo,
        String mensagem) {
}
