package sgc.alerta;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("E-mails - Padrão visual compartilhado")
class EmailTemplatesPadraoVisualTest {
    private static final Path DIRETORIO_TEMPLATES = Paths.get("src", "main", "resources", "templates", "email");
    private static final String REFERENCIA_LAYOUT = "th:replace=\"~{_layout :: email(cabecalho=~{::cabecalho}, conteudo=~{::conteudo})}\"";

    @Test
    @DisplayName("layout compartilhado deve concentrar a identidade visual do sistema")
    void layoutCompartilhadoDeveConcentrarIdentidadeVisual() throws IOException {
        String conteudo = Files.readString(DIRETORIO_TEMPLATES.resolve("_layout.html"), StandardCharsets.UTF_8);

        assertThat(conteudo)
                .contains("th:fragment=\"email(cabecalho, conteudo)\"")
                .contains(".container")
                .contains(".content")
                .contains("Sistema de Gestão de Competências - SGC")
                .contains("Este é um e-mail automático. Não responda.");
    }

    @Test
    @DisplayName("todos os templates de e-mail devem usar o layout compartilhado")
    void todosOsTemplatesDevemUsarLayoutCompartilhado() throws IOException {
        List<Path> templates = listarTemplates()
                .filter(path -> !path.getFileName().toString().equals("_layout.html"))
                .toList();

        assertThat(templates)
                .isNotEmpty()
                .allSatisfy(path -> assertThat(ler(path))
                        .withFailMessage("Template %s não usa o layout compartilhado.", path.getFileName())
                        .contains(REFERENCIA_LAYOUT));
    }

    @Test
    @DisplayName("templates de conteúdo não devem declarar estrutura visual própria")
    void templatesDeConteudoNaoDevemDeclararEstruturaVisualPropria() throws IOException {
        List<Path> templates = listarTemplates()
                .filter(path -> !path.getFileName().toString().equals("_layout.html"))
                .toList();

        assertThat(templates).allSatisfy(path -> {
            String conteudo = ler(path);
            assertThat(conteudo)
                    .withFailMessage("Template %s não deve definir bloco <style> próprio.", path.getFileName())
                    .doesNotContain("<style");
            assertThat(conteudo)
                    .withFailMessage("Template %s não deve redefinir <head> próprio.", path.getFileName())
                    .doesNotContain("<head>");
            assertThat(conteudo)
                    .withFailMessage("Template %s não deve redefinir a estrutura .container do layout.", path.getFileName())
                    .doesNotContain("class=\"container\"")
                    .doesNotContain("class=\"header\"")
                    .doesNotContain("class=\"content\"")
                    .doesNotContain("class=\"footer\"");
        });
    }

    @Test
    @DisplayName("templates de conteúdo devem expor apenas cabeçalho e conteúdo")
    void templatesDeConteudoDevemExporApenasCabecalhoEConteudo() throws IOException {
        List<Path> templates = listarTemplates()
                .filter(path -> !path.getFileName().toString().equals("_layout.html"))
                .toList();

        assertThat(templates).allSatisfy(path -> {
            String conteudo = ler(path);
            assertThat(conteudo)
                    .withFailMessage("Template %s deve declarar fragmento cabecalho.", path.getFileName())
                    .contains("th:fragment=\"cabecalho\"");
            assertThat(conteudo)
                    .withFailMessage("Template %s deve declarar fragmento conteudo.", path.getFileName())
                    .contains("th:fragment=\"conteudo\"");
        });
    }

    @Test
    @DisplayName("todos os templates de conteúdo não devem usar estilo inline nem botão destacado")
    void todosOsTemplatesDeConteudoNaoDevemUsarEstiloInlineNemBotaoDestacado() throws IOException {
        List<Path> templates = listarTemplates()
                .filter(path -> !path.getFileName().toString().equals("_layout.html"))
                .toList();

        assertThat(templates).allSatisfy(path -> {
            String conteudo = ler(path);
            assertThat(conteudo)
                    .withFailMessage("Template %s não deve usar estilo inline.", path.getFileName())
                    .doesNotContain("style=");
            assertThat(conteudo)
                    .withFailMessage("Template %s não deve usar botão destacado.", path.getFileName())
                    .doesNotContain("class=\"btn\"");
        });
    }

    @Test
    @DisplayName("somente templates explicitamente permitidos podem usar highlight-box")
    void somenteTemplatesPermitidosPodemUsarHighlightBox() throws IOException {
        Set<String> templatesPermitidos = Set.of();

        List<Path> templates = listarTemplates()
                .filter(path -> !path.getFileName().toString().equals("_layout.html"))
                .toList();

        assertThat(templates).allSatisfy(path -> {
            String conteudo = ler(path);
            if (templatesPermitidos.contains(path.getFileName().toString())) {
                return;
            }
            assertThat(conteudo)
                    .withFailMessage("Template %s não deve usar highlight-box sem exigência explícita de CDU.", path.getFileName())
                    .doesNotContain("highlight-box");
        });
    }

    private Stream<Path> listarTemplates() throws IOException {
        return Files.list(DIRETORIO_TEMPLATES)
                .filter(path -> path.getFileName().toString().endsWith(".html"))
                .sorted();
    }

    private String ler(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
