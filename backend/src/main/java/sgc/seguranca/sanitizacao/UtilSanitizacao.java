package sgc.seguranca.sanitizacao;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * Utilitário centralizado para sanitização de HTML.
 * Garante que a política de segurança seja aplicada consistentemente
 */
public final class UtilSanitizacao {
    private static final PolicyFactory POLITICA_PADRAO = new HtmlPolicyBuilder().toFactory();

    private UtilSanitizacao() {
        // Construtor privado para impedir instanciação
    }

    /**
     * Sanitiza o texto fornecido, removendo todas as tags HTML.
     *
     * @param entrada O texto a ser sanitizado.
     * @return O texto sem tags HTML.
     */
    public static String sanitizar(String entrada) {
        return POLITICA_PADRAO.sanitize(entrada);
    }
}
