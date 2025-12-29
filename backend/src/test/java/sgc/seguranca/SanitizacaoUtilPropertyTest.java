package sgc.seguranca;

import net.jqwik.api.*;
import net.jqwik.api.constraints.WithNull;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class SanitizacaoUtilPropertyTest {

    @Property
    void sanitizacaoDeveSerIdempotente(@ForAll @WithNull String input) {
        String primeiraPassada = SanitizacaoUtil.sanitizar(input);
        String segundaPassada = SanitizacaoUtil.sanitizar(primeiraPassada);
        assertThat(segundaPassada).isEqualTo(primeiraPassada);
    }

    @Property
    void nullDeveRetornarNull(@ForAll("nulls") String input) {
        assertThat(SanitizacaoUtil.sanitizar(input)).isNull();
    }

    @Property
    void tagsHtmlDevemSerRemovidas(@ForAll("htmlStrings") String input) {
        String output = SanitizacaoUtil.sanitizar(input);

        // Verifica se não sobraram tags HTML comuns
        // Esta é uma verificação simplificada, pois o output pode conter < ou > legítimos (matemáticos),
        // mas não deve conter tags estruturais se a política for "strip all".
        assertThat(output).doesNotContain("<script", "</div>", "</body>", "<a href=");
    }

    @Provide
    Arbitrary<String> nulls() {
        return Arbitraries.of((String) null);
    }

    @Provide
    Arbitrary<String> htmlStrings() {
        // Gera strings misturando texto normal e tags HTML
        Arbitrary<String> tags = Arbitraries.of("<div>", "<span>", "<script>alert(1)</script>", "<a href='x'>link</a>", "<br/>");
        Arbitrary<String> text = Arbitraries.strings().alpha().ofLength(5);

        return Arbitraries.oneOf(tags, text)
                .list()
                .ofMinSize(1)
                .ofMaxSize(10)
                .map(list -> String.join("", list));
    }
}
