package sgc.seguranca.sanitizacao;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * Utilitário centralizado para sanitização de HTML.
 * Garante que a política de segurança seja aplicada consistentemente em todo o
 * sistema.
 */
public final class UtilSanitizacao {
    private static final PolicyFactory POLITICA_PADRAO = new HtmlPolicyBuilder().toFactory();

    private UtilSanitizacao() {
        // Construtor privado para impedir instanciação
    }

    /**
     * Sanitiza o texto fornecido, removendo todas as tags HTML.
     * Se a entrada for nula, retorna nulo.
     *
     * @param entrada O texto a ser sanitizado.
     * @return O texto sem tags HTML, ou nulo se a entrada for nula.
     */
    public static String sanitizar(String entrada) {
        if (entrada == null) {
            return null;
        }
        return POLITICA_PADRAO.sanitize(entrada);
    }
}
