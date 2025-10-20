package sgc.util;

public class HtmlUtils {

    /**
     * Escapa caracteres HTML em uma string para exibição segura em páginas web.
     * <p>
     * Este método é um wrapper sobre o {@link org.springframework.web.util.HtmlUtils#htmlEscape(String)},
     * convertendo caracteres como {@code <}, {@code >}, {@code &} em suas
     * respectivas entidades HTML ({@code &lt;}, {@code &gt;}, {@code &amp;}).
     *
     * @param text A string a ser escapada. Pode ser nula.
     * @return A string com os caracteres escapados, ou {@code null} se a entrada for nula.
     */
    public static String escapeHtml(String text) {
        if (text == null) {
            return null;
        }
        return org.springframework.web.util.HtmlUtils.htmlEscape(text);
    }
}
