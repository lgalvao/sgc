import {describe, expect, it} from "vitest";
import {
    excedeLimiteHtmlFormatado,
    LIMITE_PADRAO_TEXTO_FORMATADO,
    obterComprimentoHtmlFormatado
} from "@/utils/textoFormatado";

describe("textoFormatado", () => {
    it("conta o comprimento do html formatado incluindo tags", () => {
        expect(obterComprimentoHtmlFormatado("<p>abc</p>")).toBe(10);
    });

    it("não excede o limite quando o html normalizado ocupa exatamente 500 caracteres", () => {
        const html = `<p>${"a".repeat(LIMITE_PADRAO_TEXTO_FORMATADO - 7)}</p>`;
        expect(excedeLimiteHtmlFormatado(html)).toBe(false);
    });

    it("excede o limite quando as tags fazem o html passar de 500 caracteres", () => {
        const html = `<p>${"a".repeat(LIMITE_PADRAO_TEXTO_FORMATADO - 6)}</p>`;
        expect(excedeLimiteHtmlFormatado(html)).toBe(true);
    });
});
