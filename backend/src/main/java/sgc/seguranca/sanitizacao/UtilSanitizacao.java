package sgc.seguranca.sanitizacao;

import org.owasp.html.*;

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
     */
    public static String sanitizar(String entrada) {
        return POLITICA_PADRAO.sanitize(entrada);
    }
}
