package sgc.seguranca.sanitizacao;

import org.jspecify.annotations.Nullable;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * Utilitário centralizado para sanitização de HTML.
 * Garante que a política de segurança seja aplicada consistentemente
 */
public final class UtilSanitizacao {
    private static final PolicyFactory POLITICA_FORMATADA = new HtmlPolicyBuilder()
            .allowElements("p", "br", "strong", "b", "em", "i", "u", "ul", "ol", "li")
            .toFactory();

    private static final PolicyFactory POLITICA_TEXTO_PURO = new HtmlPolicyBuilder().toFactory();

    private UtilSanitizacao() {
        // Construtor privado para impedir instanciação
    }

    /**
     * Sanitiza o texto fornecido, preservando apenas a formatação mínima permitida pelo sistema.
     */
    public static String sanitizar(@Nullable String entrada) {
        return limparTags(entrada);
    }

    /**
     * Sanitiza o texto fornecido, preservando apenas a formatação mínima permitida pelo sistema.
     */
    public static String sanitizarFormatado(@Nullable String entrada) {
        return POLITICA_FORMATADA.sanitize(entrada);
    }

    /**
     * Remove todas as tags HTML do texto fornecido.
     */
    public static String limparTags(@Nullable String entrada) {
        return POLITICA_TEXTO_PURO.sanitize(entrada);
    }
}
