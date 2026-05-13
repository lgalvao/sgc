package sgc.seguranca.sanitizacao;

import org.jspecify.annotations.*;
import org.owasp.html.*;

/**
 * Utilitário centralizado para sanitização de HTML.
 * Garante que a política de segurança seja aplicada consistentemente
 */
public final class UtilSanitizacao {
    private static final PolicyFactory POLITICA_PADRAO = new HtmlPolicyBuilder()
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
        return POLITICA_PADRAO.sanitize(entrada);
    }

    /**
     * Remove todas as tags HTML do texto fornecido.
     */
    public static String limparTags(@Nullable String entrada) {
        return POLITICA_TEXTO_PURO.sanitize(entrada);
    }
}
