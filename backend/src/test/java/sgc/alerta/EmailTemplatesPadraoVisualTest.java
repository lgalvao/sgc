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
    private static final Set<String> TEMPLATES_SEM_ESTILO_INLINE = Set.of(
            "aceite-cadastro.html",
            "aceite-cadastro-superior.html",
            "aceite-revisao-cadastro.html",
            "aceite-revisao-cadastro-superior.html",
            "cadastro-devolvido.html",
            "cadastro-devolvido-superior.html",
            "cadastro-disponibilizado.html",
            "cadastro-disponibilizado-superior.html",
            "atribuicao-temporaria.html",
            "devolucao-revisao-cadastro.html",
            "devolucao-revisao-cadastro-superior.html",
            "disponibilizacao-revisao-cadastro.html",
            "disponibilizacao-revisao-cadastro-superior.html",
            "lembrete-prazo.html"
            ,
            "mapa-disponibilizado.html",
            "mapa-disponibilizado-superior.html",
            "sugestoes-mapa.html",
            "sugestoes-mapa-superior.html",
            "validacao-mapa.html",
            "validacao-mapa-superior.html",
            "devolucao-validacao.html",
            "devolucao-validacao-superior.html",
            "aceite-validacao.html",
            "aceite-validacao-superior.html"
    );

    @Test
    @DisplayName("layout compartilhado deve concentrar a identidade visual do sistema")
    void layoutCompartilhadoDeveConcentrarIdentidadeVisual() throws IOException {
        String conteudo = Files.readString(DIRETORIO_TEMPLATES.resolve("_layout.html"), StandardCharsets.UTF_8);

        assertThat(conteudo)
                .contains("th:fragment=\"email(cabecalho, conteudo)\"")
                .contains(".container")
                .contains(".header")
                .contains(".content")
                .contains(".footer")
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
    @DisplayName("templates já revisados não devem usar estilo inline nem botão destacado")
    void templatesRevisadosNaoDevemUsarEstiloInlineNemBotaoDestacado() throws IOException {
        for (String nomeTemplate : TEMPLATES_SEM_ESTILO_INLINE) {
            String conteudo = ler(DIRETORIO_TEMPLATES.resolve(nomeTemplate));
            assertThat(conteudo)
                    .withFailMessage("Template %s não deve usar estilo inline.", nomeTemplate)
                    .doesNotContain("style=");
            assertThat(conteudo)
                    .withFailMessage("Template %s não deve usar botão destacado.", nomeTemplate)
                    .doesNotContain("class=\"btn\"");
        }
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
