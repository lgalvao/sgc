package sgc.alerta;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.util.*;

/**
 * Converte estilos CSS de blocos {@code <style>} para atributos {@code style} inline,
 * melhorando a compatibilidade com clientes de e-mail que ignoram ou removem blocos de estilo
 * (como Gmail e Outlook).
 *
 * <p>Regras que permanecem no {@code <style>} (não podem ser inlinadas):
 * <ul>
 *   <li>Pseudo-classes: {@code :hover}, {@code :focus}, {@code :last-child}, etc.</li>
 *   <li>{@code @}-regras: {@code @media}, {@code @keyframes}, etc.</li>
 * </ul>
 *
 * <p>Estilos {@code style=""} já existentes nos elementos têm prioridade sobre
 * as declarações da folha de estilo.
 */
public class InlinerCss {

    private InlinerCss() {
        // Utilitário estático
    }

    /**
     * Processa o HTML movendo estilos do bloco {@code <style>} para atributos inline.
     * Se o HTML não contiver bloco {@code <style>}, é retornado sem alterações.
     *
     * @param html HTML completo a ser processado
     * @return HTML com estilos CSS inlinados
     */
    public static String inlinar(String html) {
        if (!html.contains("<style") && !html.contains("<STYLE")) {
            return html;
        }

        Document doc = Jsoup.parse(html);
        doc.outputSettings()
           .syntax(Document.OutputSettings.Syntax.html)
           .prettyPrint(false);

        for (Element styleEl : doc.select("style")) {
            String css = styleEl.html();
            StringBuilder cssRestante = new StringBuilder();

            for (BlocoCSS bloco : extrairBlocos(css)) {
                if (bloco.deveManter()) {
                    cssRestante.append(bloco.prefixo()).append(" {\n")
                               .append(bloco.corpo()).append("\n}\n\n");
                    continue;
                }
                for (String seletor : bloco.prefixo().split(",")) {
                    aplicarInline(doc, seletor.strip(), bloco.corpo());
                }
            }

            String cssFinal = cssRestante.toString().strip();
            if (cssFinal.isEmpty()) {
                styleEl.remove();
            } else {
                styleEl.html(cssFinal);
            }
        }

        return doc.html();
    }

    private static void aplicarInline(Document doc, String seletor, String declaracoes) {
        if (seletor.isEmpty()) return;
        try {
            Elements elementos = doc.select(seletor);
            for (Element el : elementos) {
                Map<String, String> mapa = new LinkedHashMap<>();
                // Declarações da folha de estilo (menor prioridade)
                parsearDeclaracoes(declaracoes).forEach(mapa::put);
                // Estilos inline já presentes no elemento (maior prioridade — prevalecem)
                parsearDeclaracoes(el.attr("style")).forEach(mapa::put);
                el.attr("style", montarEstilo(mapa));
            }
        } catch (Exception ignorado) {
            // Seletor não suportado pelo jsoup — ignorar silenciosamente
        }
    }

    /**
     * Divide o CSS em blocos {@code prefixo { corpo }}, respeitando chaves aninhadas.
     * Visibilidade de pacote para permitir testes unitários.
     */
    static List<BlocoCSS> extrairBlocos(String css) {
        List<BlocoCSS> blocos = new ArrayList<>();
        int i = 0;
        int len = css.length();

        while (i < len) {
            // Avançar até o próximo '{'
            int inicioSeletor = i;
            while (i < len && css.charAt(i) != '{') i++;
            if (i >= len) break;

            String prefixo = css.substring(inicioSeletor, i).strip();
            i++; // pular '{'

            // Coletar corpo balanceando chaves aninhadas (ex: @media com regras internas)
            int nivel = 1;
            int inicioCorpo = i;
            while (i < len && nivel > 0) {
                char c = css.charAt(i);
                if (c == '{') nivel++;
                else if (c == '}') nivel--;
                i++;
            }
            String corpo = css.substring(inicioCorpo, i - 1).strip();

            if (!prefixo.isEmpty()) {
                blocos.add(new BlocoCSS(prefixo, corpo));
            }
        }
        return blocos;
    }

    private static Map<String, String> parsearDeclaracoes(String css) {
        var mapa = new LinkedHashMap<String, String>();
        if (css == null || css.isBlank()) return mapa;
        for (String declaracao : css.split(";")) {
            declaracao = declaracao.strip();
            if (declaracao.isEmpty()) continue;
            int idx = declaracao.indexOf(':');
            if (idx < 1) continue;
            String prop = declaracao.substring(0, idx).strip().toLowerCase();
            String valor = declaracao.substring(idx + 1).strip();
            if (!prop.isEmpty() && !valor.isEmpty()) {
                mapa.put(prop, valor);
            }
        }
        return mapa;
    }

    private static String montarEstilo(Map<String, String> mapa) {
        var sb = new StringBuilder();
        mapa.forEach((prop, valor) -> {
            if (!sb.isEmpty()) sb.append("; ");
            sb.append(prop).append(": ").append(valor);
        });
        return sb.toString();
    }

    /**
     * Representa um bloco CSS com prefixo (seletor ou @-regra) e corpo (declarações).
     */
    record BlocoCSS(String prefixo, String corpo) {

        /**
         * Indica se o bloco deve ser mantido no {@code <style>} em vez de ser inlinado.
         * Blocos com pseudo-classes, {@code @}-regras ou regras aninhadas são sempre preservados.
         */
        boolean deveManter() {
            return prefixo.startsWith("@")
                    || contemPseudoclasse(prefixo)
                    || corpoContemAninhamento(corpo);
        }

        private static boolean contemPseudoclasse(String seletor) {
            return seletor.contains(":hover")
                    || seletor.contains(":focus")
                    || seletor.contains(":last-child")
                    || seletor.contains(":first-child")
                    || seletor.contains(":nth-");
        }

        private static boolean corpoContemAninhamento(String corpo) {
            return corpo.contains("{");
        }
    }
}
