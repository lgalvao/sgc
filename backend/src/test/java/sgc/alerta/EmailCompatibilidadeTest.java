package sgc.alerta;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Compatibilidade dos e-mails")
class EmailCompatibilidadeTest {
    private static final Path LAYOUT = Path.of("src", "main", "resources", "templates", "email", "_layout.html");

    @Test
    @DisplayName("mantém o layout de tabela e os fallbacks exigidos pelos clientes de e-mail")
    void deveManterFallbacksDeClientesDeEmail() throws Exception {
        String html = Files.readString(LAYOUT, StandardCharsets.UTF_8);
        String htmlFinal = InlinerCss.inlinar(html);
        Document documento = Jsoup.parse(htmlFinal);

        assertThat(documento.select("table")).hasSize(2).allSatisfy(tabela -> {
            assertThat(tabela.attr("role")).isEqualTo("presentation");
            assertThat(tabela.hasAttr("width")).isTrue();
            assertThat(tabela.hasAttr("cellpadding")).isTrue();
            assertThat(tabela.hasAttr("cellspacing")).isTrue();
            assertThat(tabela.hasAttr("border")).isTrue();
        });

        Element corpo = documento.selectFirst("body");
        assertThat(corpo).isNotNull();
        assertThat(corpo.attr("style"))
                .contains("margin:0")
                .contains("padding:0")
                .contains("background-color:#f5f7fa");

        Element container = documento.selectFirst("table.container");
        assertThat(container).isNotNull();
        assertThat(container.attr("style"))
                .contains("width:640px")
                .contains("border-collapse:collapse")
                .contains("mso-table-lspace:0")
                .contains("mso-table-rspace:0");
    }

    @Test
    @DisplayName("não depende de CSS estrutural incompatível com Outlook")
    void naoDeveUsarCssEstruturalIncompativel() throws Exception {
        String html = InlinerCss.inlinar(Files.readString(LAYOUT, StandardCharsets.UTF_8));

        assertThat(html)
                .doesNotContain("display: flex")
                .doesNotContain("display: grid")
                .doesNotContain("position: fixed")
                .doesNotContain("position: absolute")
                .doesNotContain(":last-child");
    }
}
