import {beforeEach, describe, expect, it} from "vitest";
import {useTemaPreferencia} from "@/composables/useTemaPreferencia";

describe("useTemaPreferencia", () => {
    beforeEach(() => {
        localStorage.clear();
    });

    it("deve persistir tema escuro por usuário", () => {
        const tema = useTemaPreferencia();

        tema.definirContextoUsuarioTemaEscuro("101");
        expect(tema.obterTemaEscuro()).toBe(false);

        tema.definirTemaEscuro(true);
        expect(tema.obterTemaEscuro()).toBe(true);

        tema.definirContextoUsuarioTemaEscuro("202");
        expect(tema.obterTemaEscuro()).toBe(false);

        tema.definirTemaEscuro(true);
        tema.definirContextoUsuarioTemaEscuro("101");
        expect(tema.obterTemaEscuro()).toBe(true);
    });
});
