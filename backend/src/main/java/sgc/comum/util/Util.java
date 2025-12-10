package sgc.comum.util;

public class Util {
    public static String msgConcordante(String singular, String plural, int quantidade) {
        return quantidade == 1 ? singular : plural;
    }

    public static String msgConcordante(String singular, int quantidade) {
        return msgConcordante(singular, "%ss".formatted(singular), quantidade);
    }
}
