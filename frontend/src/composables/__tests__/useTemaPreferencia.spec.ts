import {beforeEach, describe, expect, it} from "vitest";
import {useTemaPreferencia} from "@/composables/useTemaPreferencia";

describe("useTemaPreferencia", () => {
    beforeEach(() => {
        localStorage.clear();
    });

    it("deve persistir tema escuro por usuário", () => {
        const tema = useTemaPreferencia();

        tema.setContextoUsuarioTemaEscuro("101");
        expect(tema.getTemaEscuro()).toBe(false);

        tema.setTemaEscuro(true);
        expect(tema.getTemaEscuro()).toBe(true);

        tema.setContextoUsuarioTemaEscuro("202");
        expect(tema.getTemaEscuro()).toBe(false);

        tema.setTemaEscuro(true);
        tema.setContextoUsuarioTemaEscuro("101");
        expect(tema.getTemaEscuro()).toBe(true);
    });
});
