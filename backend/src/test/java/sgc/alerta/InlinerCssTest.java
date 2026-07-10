package sgc.alerta;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class InlinerCssTest {

    // -------------------------------------------------------------------------
    // extrairBlocos
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("extrairBlocos")
    class ExtrairBlocos {

        @Test
        @DisplayName("Extrai regra simples de elemento")
        void deveExtrairRegraSimples() {
            var blocos = InlinerCss.extrairBlocos("body { color: red; }");

            assertThat(blocos).hasSize(1);
            assertThat(blocos.get(0).prefixo()).isEqualTo("body");
            assertThat(blocos.get(0).corpo()).isEqualTo("color: red;");
        }

        @Test
        @DisplayName("Extrai múltiplos blocos")
        void deveExtrairMultiplosBlocos() {
            String css = "a { color: blue; } p { margin: 0; }";
            var blocos = InlinerCss.extrairBlocos(css);

            assertThat(blocos).hasSize(2);
            assertThat(blocos.get(0).prefixo()).isEqualTo("a");
            assertThat(blocos.get(1).prefixo()).isEqualTo("p");
        }

        @Test
        @DisplayName("Preserva @media com regras aninhadas")
        void devePreservarAtMedia() {
            String css = "@media (max-width: 640px) { .container { width: 100%; } }";
            var blocos = InlinerCss.extrairBlocos(css);

            assertThat(blocos).hasSize(1);
            assertThat(blocos.get(0).prefixo()).isEqualTo("@media (max-width: 640px)");
            assertThat(blocos.get(0).corpo()).contains(".container");
        }

        @Test
        @DisplayName("Ignora CSS vazio")
        void deveIgnorarCssVazio() {
            assertThat(InlinerCss.extrairBlocos("")).isEmpty();
            assertThat(InlinerCss.extrairBlocos("   ")).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // BlocoCSS.deveManter
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("BlocoCSS.deveManter")
    class DeveManter {

        @Test
        @DisplayName("Regra simples não deve ser mantida no style")
        void regrasSimpleNaoDeveManter() {
            var bloco = new InlinerCss.BlocoCSS(".content p", "margin: 0;");
            assertThat(bloco.deveManter()).isFalse();
        }

        @Test
        @DisplayName(":hover deve ser mantido no style")
        void hoverDeveManter() {
            var bloco = new InlinerCss.BlocoCSS(".btn:hover", "background-color: blue;");
            assertThat(bloco.deveManter()).isTrue();
        }

        @Test
        @DisplayName(":focus deve ser mantido no style")
        void focusDeveManter() {
            var bloco = new InlinerCss.BlocoCSS(".btn:focus", "background-color: blue;");
            assertThat(bloco.deveManter()).isTrue();
        }

        @Test
        @DisplayName(":last-child deve ser mantido no style")
        void lastChildDeveManter() {
            var bloco = new InlinerCss.BlocoCSS(".content p:last-child", "margin-bottom: 0;");
            assertThat(bloco.deveManter()).isTrue();
        }

        @Test
        @DisplayName("@media deve ser mantido no style")
        void atMediaDeveManter() {
            var bloco = new InlinerCss.BlocoCSS("@media (max-width: 640px)", ".x { width: 100%; }");
            assertThat(bloco.deveManter()).isTrue();
        }

        @Test
        @DisplayName("Corpo com aninhamento deve ser mantido no style")
        void corpoAninhado() {
            var bloco = new InlinerCss.BlocoCSS("@keyframes spin", "from { transform: rotate(0); }");
            assertThat(bloco.deveManter()).isTrue();
        }
    }

    // -------------------------------------------------------------------------
    // inlinar
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("inlinar")
    class Inlinar {

        @Test
        @DisplayName("Retorna HTML inalterado quando não há bloco <style>")
        void semStyleRetornaIgual() {
            String html = "<h1>Olá</h1>";
            assertThat(InlinerCss.inlinar(html)).isEqualTo(html);
        }

        @Test
        @DisplayName("Inlina regra de elemento")
        void inlinaRegraDeElemento() {
            String html = """
                    <html><head><style>p { color: red; }</style></head>
                    <body><p>texto</p></body></html>""";

            String resultado = InlinerCss.inlinar(html);

            assertThat(resultado).contains("color: red");
            assertThat(resultado).doesNotContain("<style>");
        }

        @Test
        @DisplayName("Inlina regra de classe")
        void inlinaRegraDeClasse() {
            String html = """
                    <html><head><style>.box { padding: 16px; }</style></head>
                    <body><div class="box">conteúdo</div></body></html>""";

            String resultado = InlinerCss.inlinar(html);

            assertThat(resultado).contains("padding: 16px");
        }

        @Test
        @DisplayName("Inlina regra de descendente")
        void inlinaRegraDeDescendente() {
            String html = """
                    <html><head><style>.content p { margin: 0 0 12px; }</style></head>
                    <body><div class="content"><p>texto</p></div></body></html>""";

            String resultado = InlinerCss.inlinar(html);

            assertThat(resultado).contains("margin: 0 0 12px");
        }

        @Test
        @DisplayName("Preserva :hover no bloco <style>")
        void preservaHoverNoStyle() {
            String html = """
                    <html><head><style>.btn:hover { background: blue; }</style></head>
                    <body><a class="btn">link</a></body></html>""";

            String resultado = InlinerCss.inlinar(html);

            assertThat(resultado).contains(".btn:hover");
            assertThat(resultado).contains("<style>");
        }

        @Test
        @DisplayName("Preserva @media no bloco <style>")
        void preservaAtMediaNoStyle() {
            String html = """
                    <html><head><style>@media (max-width: 640px) { .x { width: 100%; } }</style></head>
                    <body><div class="x"></div></body></html>""";

            String resultado = InlinerCss.inlinar(html);

            assertThat(resultado).contains("@media");
            assertThat(resultado).contains("<style>");
        }

        @Test
        @DisplayName("Estilos inline existentes têm prioridade sobre o CSS da folha")
        void estilosInlineExistentesTemPrioridade() {
            String html = """
                    <html><head><style>p { color: red; }</style></head>
                    <body><p style="color: green;">texto</p></body></html>""";

            String resultado = InlinerCss.inlinar(html);

            assertThat(resultado).contains("color: green");
            assertThat(resultado).doesNotContain("color: red");
        }

        @Test
        @DisplayName("Inlina múltiplos seletores separados por vírgula")
        void inlinaMultiplosSeletores() {
            String html = """
                    <html><head><style>h1, h2 { font-family: Arial; }</style></head>
                    <body><h1>Título</h1><h2>Subtítulo</h2></body></html>""";

            String resultado = InlinerCss.inlinar(html);

            assertThat(resultado).containsPattern("(?s)<h1[^>]*style=\"[^\"]*font-family: Arial[^\"]*\"");
            assertThat(resultado).containsPattern("(?s)<h2[^>]*style=\"[^\"]*font-family: Arial[^\"]*\"");
        }

        @Test
        @DisplayName("Remove o bloco <style> quando todas as regras forem inlinadas")
        void removeStyleQuandoTudasAsRegrasSaoInlinadas() {
            String html = """
                    <html><head><style>p { margin: 0; }</style></head>
                    <body><p>texto</p></body></html>""";

            String resultado = InlinerCss.inlinar(html);

            assertThat(resultado).doesNotContain("<style>");
        }

        @Test
        @DisplayName("Mantém o bloco <style> quando houver regras não-inlináveis")
        void mantemStyleComRegrasNaoInlinadas() {
            String html = """
                    <html><head><style>p { margin: 0; } .btn:hover { background: blue; }</style></head>
                    <body><p>texto</p><a class="btn">link</a></body></html>""";

            String resultado = InlinerCss.inlinar(html);

            assertThat(resultado).contains("<style>");
            assertThat(resultado).contains(".btn:hover");
            assertThat(resultado).doesNotContain("p {");
        }

        @Test
        @DisplayName("Inlina regra do elemento <a>")
        void inlinaRegraDeLink() {
            String html = """
                    <html><head><style>a { color: #0b69a3; }</style></head>
                    <body><a href="#">link</a></body></html>""";

            String resultado = InlinerCss.inlinar(html);

            assertThat(resultado).contains("color: #0b69a3");
        }
    }
}
