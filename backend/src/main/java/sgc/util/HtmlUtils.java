package sgc.util;

public class HtmlUtils {

    public static String escapeHtml(String text) {
        if (text == null) {
            return null;
        }
        return org.springframework.web.util.HtmlUtils.htmlEscape(text);
    }
}
