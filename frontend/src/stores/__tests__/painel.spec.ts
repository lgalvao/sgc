import {beforeEach, describe, expect, it} from "vitest";
import {createPinia, setActivePinia} from "pinia";
import {usePainelStore} from "../painel";

describe("usePainelStore", () => {
    beforeEach(() => {
        setActivePinia(createPinia());
    });

    it("deve iniciar sem alertas marcados como lidos", () => {
        const store = usePainelStore();
        expect(store.isMarcadoComoLido(1)).toBe(false);
    });

    it("deve registrar e verificar leitura de alertas", () => {
        const store = usePainelStore();

        store.registrarLeitura([1, 2]);
        expect(store.isMarcadoComoLido(1)).toBe(true);
        expect(store.isMarcadoComoLido(2)).toBe(true);
        expect(store.isMarcadoComoLido(3)).toBe(false);
    });

    it("invalidar deve limpar marcações locais", () => {
        const store = usePainelStore();
        store.registrarLeitura([1, 2]);

        store.invalidar();

        expect(store.isMarcadoComoLido(1)).toBe(false);
        expect(store.isMarcadoComoLido(2)).toBe(false);
        expect(store.precisaRecarregar).toBe(true);
        expect(store.versaoInvalidacao).toBe(1);
    });

    it("resetar deve limpar marcações locais", () => {
        const store = usePainelStore();
        store.registrarLeitura([1]);
        store.invalidar();

        store.resetar();

        expect(store.isMarcadoComoLido(1)).toBe(false);
        expect(store.precisaRecarregar).toBe(false);
        expect(store.versaoInvalidacao).toBe(0);
    });

    it("marcarRecarregado deve limpar o estado sujo do painel", () => {
        const store = usePainelStore();

        store.invalidar();
        store.marcarRecarregado();

        expect(store.precisaRecarregar).toBe(false);
    });
});
