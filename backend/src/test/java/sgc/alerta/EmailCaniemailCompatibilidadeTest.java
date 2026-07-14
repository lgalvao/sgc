package sgc.alerta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valida o HTML final contra um snapshot versionado da matriz Can I Email.
 *
 * <p>O teste deliberadamente considera apenas suporte inexistente ({@code n})
 * como falha. Suporte parcial ({@code a}) é esperado em e-mail e deve ser
 * tratado pelo fallback estrutural do template.</p>
 */
@DisplayName("Compatibilidade Can I Email dos e-mails")
class EmailCaniemailCompatibilidadeTest {
    // Cada família é percorrida em todas as plataformas disponíveis no snapshot,
    // incluindo explicitamente as variantes Android e iOS.
    private static final Set<String> CLIENTES_ALVO = Set.of("outlook", "gmail", "apple-mail");
    private static final Set<String> PROPRIEDADES_IGNORADAS = Set.of(
            "font-family", "font-size", "font-style", "font-weight", "color",
            "display", "text-decoration", "mso-table-lspace", "mso-table-rspace"
    );

    @Test
    @DisplayName("não usa propriedades sem suporte no Outlook ou Gmail")
    void naoDeveUsarPropriedadesSemSuporte() throws Exception {
        String fonte = new String(
                EmailCaniemailCompatibilidadeTest.class
                        .getResourceAsStream("/caniemail/data.json").readAllBytes(),
                StandardCharsets.UTF_8);
        JsonNode raiz = new ObjectMapper().readTree(fonte);
        JsonNode porSlug = indexarPorSlug(raiz.path("data"));
        Set<String> propriedades = propriedadesCssDoLayout();
        Set<String> incompatibilidades = new HashSet<>();

        for (String propriedade : propriedades) {
            if (PROPRIEDADES_IGNORADAS.contains(propriedade)) continue;
            JsonNode recurso = porSlug.get("css-" + propriedade);
            if (recurso == null) continue;

            for (String cliente : CLIENTES_ALVO) {
                if (possuiSuporteNulo(recurso.path("stats").path(cliente))) {
                    incompatibilidades.add(propriedade + " sem suporte em " + cliente);
                }
            }
        }

        assertThat(incompatibilidades)
                .withFailMessage("Incompatibilidades encontradas na matriz Can I Email: %s", incompatibilidades)
                .isEmpty();
    }

    @Test
    @DisplayName("mantém o snapshot oficial identificável")
    void deveConterMetadadosDoSnapshot() throws Exception {
        try (InputStream entrada = getClass().getResourceAsStream("/caniemail/data.json")) {
            assertThat(entrada).isNotNull();
            JsonNode raiz = new ObjectMapper().readTree(entrada);
            assertThat(raiz.path("api_version").asText()).isNotBlank();
            assertThat(raiz.path("last_update_date").asText()).isNotBlank();
            assertThat(raiz.path("data").size()).isGreaterThan(100);
        }
    }

    private Set<String> propriedadesCssDoLayout() throws Exception {
        String html;
        try (InputStream entrada = getClass().getResourceAsStream("/templates/email/_layout.html")) {
            assertThat(entrada).isNotNull();
            html = new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        }

        Document documento = Jsoup.parse(InlinerCss.inlinar(html));
        Set<String> propriedades = new HashSet<>();
        for (Element elemento : documento.select("[style]")) {
            for (String declaracao : elemento.attr("style").split(";")) {
                int separador = declaracao.indexOf(':');
                if (separador > 0) {
                    propriedades.add(declaracao.substring(0, separador).trim().toLowerCase(Locale.ROOT));
                }
            }
        }
        return propriedades;
    }

    private JsonNode indexarPorSlug(JsonNode recursos) {
        JsonNode resultado = new ObjectMapper().createObjectNode();
        recursos.forEach(recurso -> ((com.fasterxml.jackson.databind.node.ObjectNode) resultado)
                .set(recurso.path("slug").asText(), recurso));
        return resultado;
    }

    private boolean possuiSuporteNulo(JsonNode cliente) {
        var iterador = cliente.elements();
        while (iterador.hasNext()) {
            JsonNode versoes = iterador.next();
            var nomesVersoes = versoes.fieldNames();
            while (nomesVersoes.hasNext()) {
                String suporte = versoes.path(nomesVersoes.next()).asText("u");
                if (suporte.startsWith("n")) return true;
            }
        }
        return false;
    }
}
