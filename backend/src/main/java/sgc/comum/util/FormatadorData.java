package sgc.comum.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilitário centralizado para formatação de datas no padrão brasileiro.
 */
public class FormatadorData {
    private static final DateTimeFormatter FORMATO_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_BR_COM_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private FormatadorData() {
    }

    public static String formatarData(LocalDateTime data) {
        return data == null ? "-" : data.format(FORMATO_BR);
    }

    public static String formatarDataHora(LocalDateTime data) {
        return data == null ? "-" : data.format(FORMATO_BR_COM_HORA);
    }
}
