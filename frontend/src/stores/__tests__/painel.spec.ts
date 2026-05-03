import {beforeEach, describe, expect, it, vi} from "vitest";
import {createPinia, setActivePinia} from "pinia";
import {usePainelStore} from "../painel";

describe("usePainelStore", () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        vi.useFakeTimers();
    });

    it("deve iniciar com estado vazio", () => {
        const store = usePainelStore();
        expect(store.processos).toEqual([]);
        expect(store.alertas).toEqual([]);
        expect(store.carregado).toBe(false);
        expect(store.carregadoEm).toBeNull();
    });

    it("deve definir dados corretamente", () => {
        const store = usePainelStore();
        const processos = [{codigo: 1, sigla: "P1"} as any];
        const alertas = [{codigo: 10, mensagem: "A1"} as any];
        const agora = Date.now();
        
        store.definirDados(processos, alertas);
        
        expect(store.processos).toEqual(processos);
        expect(store.alertas).toEqual(alertas);
        expect(store.carregado).toBe(true);
        expect(store.carregadoEm).toBeGreaterThanOrEqual(agora);
    });

    it("deve invalidar dados", () => {
        const store = usePainelStore();
        store.definirDados([], []);
        expect(store.carregado).toBe(true);
        
        store.invalidar();
        expect(store.carregado).toBe(false);
        expect(store.carregadoEm).toBeNull();
    });

    it("deve verificar se dados são válidos baseados no TTL", () => {
        const store = usePainelStore();
        
        // Caso 1: Não carregado
        expect(store.dadosValidos()).toBe(false);
        
        // Caso 2: Carregado agora
        store.definirDados([], []);
        expect(store.dadosValidos()).toBe(true);
        
        // Caso 3: Passado o TTL (5 minutos = 300000ms)
        vi.advanceTimersByTime(300001);
        expect(store.dadosValidos()).toBe(false);
    });

    it("deve registrar e verificar leitura de alertas", () => {
        const store = usePainelStore();
        
        expect(store.isMarcadoComoLido(1)).toBe(false);
        
        store.registrarLeitura([1, 2]);
        expect(store.isMarcadoComoLido(1)).toBe(true);
        expect(store.isMarcadoComoLido(2)).toBe(true);
        expect(store.isMarcadoComoLido(3)).toBe(false);
    });
});
