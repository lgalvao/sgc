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

    /**
     * Formata uma data no padrão brasileiro (dd/MM/yyyy).
     *
     * @param data a data a ser formatada
     * @return a data formatada ou null se a data for null
     */
    public static String formatarData(LocalDateTime data) {
        return data == null ? "-" : data.format(FORMATO_BR);
    }

    /**
     * Formata uma data com hora no padrão brasileiro (dd/MM/yyyy HH:mm).
     *
     * @param data a data a ser formatada
     * @return a data formatada ou null se a data for null
     */
    public static String formatarDataHora(LocalDateTime data) {
        return data == null ? "-" : data.format(FORMATO_BR_COM_HORA);
    }
}
