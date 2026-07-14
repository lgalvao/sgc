package sgc.integracao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import sgc.alerta.InlinerCss;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DisplayName("Templates de e-mail - compatibilidade Can I Email")
class EmailTemplatesCaniemailIntegrationTest extends BaseIntegrationTest {
    private static final Path DIRETORIO = Path.of("src", "main", "resources", "templates", "email");
    // O dataset organiza Android e iOS dentro da família do cliente; a iteração
    // abaixo percorre todas as plataformas de Outlook, Gmail e Apple Mail.
    private static final Set<String> CLIENTES = Set.of("outlook", "gmail", "apple-mail");
    private static final Pattern VARIAVEL = Pattern.compile("\\$\\{([a-zA-Z_][a-zA-Z0-9_]*)");

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Test
    @DisplayName("valida todos os templates renderizados contra a matriz oficial")
    void deveValidarTodosOsTemplatesRenderizados() throws Exception {
        JsonNode recursos = new ObjectMapper().readTree(
                getClass().getResourceAsStream("/caniemail/data.json"));
        Map<String, JsonNode> porSlug = indexar(recursos.path("data"));

        List<Path> templates = Files.list(DIRETORIO)
                .filter(path -> path.getFileName().toString().endsWith(".html"))
                .filter(path -> !path.getFileName().toString().equals("_layout.html"))
                .sorted()
                .toList();

        assertThat(templates).isNotEmpty();
        for (Path template : templates) {
            String nome = template.getFileName().toString().replaceFirst("\\.html$", "");
            String html = InlinerCss.inlinar(templateEngine.process(nome, contexto(template)));
            Set<String> incompatibilidades = incompatibilidades(html, porSlug);

            assertThat(incompatibilidades)
                    .withFailMessage("Template %s possui incompatibilidades Can I Email: %s", nome, incompatibilidades)
                    .isEmpty();
        }
    }

    private Context contexto(Path template) throws Exception {
        Context contexto = new Context();
        String conteudo = Files.readString(template, StandardCharsets.UTF_8);
        var variaveis = VARIAVEL.matcher(conteudo);
        while (variaveis.find()) {
            String nome = variaveis.group(1);
            if (Set.of("siglasUnidades", "siglasSubordinadas").contains(nome)) {
                contexto.setVariable(nome, List.of("SESEL", "STIC"));
            } else if (nome.equals("isParticipante")) {
                contexto.setVariable(nome, false);
            } else if (nome.equals("etapa")) {
                contexto.setVariable(nome, 1);
            } else {
                contexto.setVariable(nome, "Exemplo");
            }
        }
        return contexto;
    }

    private Set<String> incompatibilidades(String html, Map<String, JsonNode> porSlug) {
        Set<String> propriedades = new HashSet<>();
        var documento = Jsoup.parse(html);
        documento.select("[style]").forEach(elemento -> {
            for (String declaracao : elemento.attr("style").split(";")) {
                int separador = declaracao.indexOf(':');
                if (separador > 0) {
                    propriedades.add(declaracao.substring(0, separador).trim().toLowerCase(Locale.ROOT));
                }
            }
        });

        Set<String> problemas = new HashSet<>();
        for (String propriedade : propriedades) {
            JsonNode recurso = porSlug.get("css-" + propriedade);
            if (recurso == null) continue;
            for (String cliente : CLIENTES) {
                if (semSuporte(recurso.path("stats").path(cliente))) {
                    problemas.add(propriedade + " sem suporte em " + cliente);
                }
            }
        }
        return problemas;
    }

    private Map<String, JsonNode> indexar(JsonNode recursos) {
        Map<String, JsonNode> resultado = new java.util.HashMap<>();
        recursos.forEach(recurso -> resultado.put(recurso.path("slug").asText(), recurso));
        return resultado;
    }

    private boolean semSuporte(JsonNode cliente) {
        var plataformas = cliente.elements();
        while (plataformas.hasNext()) {
            var versoes = plataformas.next().elements();
            while (versoes.hasNext()) {
                if (versoes.next().asText("u").startsWith("n")) return true;
            }
        }
        return false;
    }
}
