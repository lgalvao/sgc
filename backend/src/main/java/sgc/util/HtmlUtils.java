package sgc.util;

public class HtmlUtils {
    /**
     * Escapa caracteres HTML em uma string para exibição segura em páginas web.
     * <p>
     * Um wrapper sobre o {@link org.springframework.web.util.HtmlUtils#htmlEscape(String)},
     * convertendo caracteres como {@code <}, {@code >}, {@code &} em suas
     * respectivas entidades HTML ({@code &lt;}, {@code &gt;}, {@code &amp;}).
     *
     * @param texto A string a ser escapada. Pode ser nula.
     * @return A string com os caracteres escapados, ou {@code null} se a entrada for nula.
     */
    public static String escapeHtml(String texto) {
        return texto == null ? null : org.springframework.web.util.HtmlUtils.htmlEscape(texto);
    }
}
