import {describe, expect, it} from "vitest";
import {
    obterComprimentoHtmlFormatado
} from "@/utils/textoFormatado";

describe("textoFormatado", () => {
    it("conta o comprimento do html formatado incluindo tags", () => {
        expect(obterComprimentoHtmlFormatado("<p>abc</p>")).toBe(10);
    });
});
