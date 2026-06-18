package sgc.comum.util;

import lombok.*;

/**
 * Utilitário para mascaramento de dados sensíveis em logs.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MascaraUtil {

    /**
     * Mascara um valor (como Título Eleitoral), exibindo apenas os últimos 4 caracteres.
     */
    public static String mascarar(String valor) {
        if (valor.length() <= 4) {
            return "***";
        }
        return "***" + valor.substring(valor.length() - 4);
    }
}
