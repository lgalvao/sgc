package sgc.comum.util;

import org.jspecify.annotations.*;

import java.time.*;
import java.time.format.*;

/**
 * Utilitário centralizado para formatação de datas no padrão brasileiro.
 */
public class FormatadorData {
    private static final DateTimeFormatter FORMATO_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_BR_COM_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private FormatadorData() {
    }

    public static String formatarData(@Nullable LocalDateTime data) {
        return data == null ? "-" : data.format(FORMATO_BR);
    }

    public static String formatarDataHora(@Nullable LocalDateTime data) {
        return data == null ? "-" : data.format(FORMATO_BR_COM_HORA);
    }
}
