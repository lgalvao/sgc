import {beforeEach, describe, expect, it, vi} from "vitest";
import {initPinia} from "@/test-utils/helpers";
import {useConfiguracoesStore} from "../configuracoes";

// Mock do localStorage para isolar os testes
const localStorageMock = (() => {
    let store: { [key: string]: string } = {};
    return {
        getItem: (key: string) => store[key] || null,
        setItem: (key: string, value: string) => (store[key] = value.toString()),
        clear: () => (store = {}),
    };
})();

Object.defineProperty(window, "localStorage", {value: localStorageMock});

describe("useConfiguracoesStore", () => {
    beforeEach(() => {
        initPinia();
        localStorageMock.clear();
        vi.spyOn(console, "error").mockImplementation(() => {
        });
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    describe("Inicialização", () => {
        it("deve ter valores padrão quando o localStorage está vazio", () => {
            const store = useConfiguracoesStore();
            expect(store.diasInativacaoProcesso).toBe(10);
            expect(store.diasAlertaNovo).toBe(7);
        });

        it("deve carregar as configurações do localStorage, se existirem", () => {
            const storedConfig = {diasInativacaoProcesso: 45, diasAlertaNovo: 15};
            localStorageMock.setItem(
                "appConfiguracoes",
                JSON.stringify(storedConfig),
            );
            const store = useConfiguracoesStore();
            store.carregarConfiguracoes();
            expect(store.diasInativacaoProcesso).toBe(45);
            expect(store.diasAlertaNovo).toBe(15);
        });
    });

    describe("Interação com LocalStorage", () => {
        it("deve salvar as configurações no localStorage", () => {
            const store = useConfiguracoesStore();
            store.definirDiasInativacaoProcesso(60);
            store.definirDiasAlertaNovo(20);
            store.salvarConfiguracoes();
            const savedConfig = JSON.parse(
                localStorageMock.getItem("appConfiguracoes") || "{}",
            );
            expect(savedConfig.diasInativacaoProcesso).toBe(60);
            expect(savedConfig.diasAlertaNovo).toBe(20);
        });

        it("deve lidar com erros de leitura do localStorage de forma graciosa", () => {
            vi.spyOn(localStorageMock, "getItem").mockImplementation(() => {
                throw new Error("Erro de leitura");
            });
            const store = useConfiguracoesStore();
            store.carregarConfiguracoes();
            expect(store.diasInativacaoProcesso).toBe(10); // Mantém o padrão
            expect(console.error).toHaveBeenCalled();
        });

        it("deve lidar com erros de escrita no localStorage de forma graciosa", () => {
            vi.spyOn(localStorageMock, "setItem").mockImplementation(() => {
                throw new Error("Erro de escrita");
            });
            const store = useConfiguracoesStore();
            const result = store.salvarConfiguracoes();
            expect(result).toBe(false);
            expect(console.error).toHaveBeenCalled();
        });
    });

    describe("Lógica dos Setters", () => {
        it("não deve permitir valores menores que 1 para diasInativacaoProcesso", () => {
            const store = useConfiguracoesStore();
            store.definirDiasInativacaoProcesso(0);
            expect(store.diasInativacaoProcesso).toBe(10);
            store.definirDiasInativacaoProcesso(-5);
            expect(store.diasInativacaoProcesso).toBe(10);
        });

        it("não deve permitir valores menores que 1 para diasAlertaNovo", () => {
            const store = useConfiguracoesStore();
            store.definirDiasAlertaNovo(0);
            expect(store.diasAlertaNovo).toBe(7);
            store.definirDiasAlertaNovo(-5);
            expect(store.diasAlertaNovo).toBe(7);
        });

        it("deve definir o valor para diasInativacaoProcesso se for >= 1", () => {
            const store = useConfiguracoesStore();
            store.definirDiasInativacaoProcesso(30);
            expect(store.diasInativacaoProcesso).toBe(30);
        });

        it("deve definir o valor para diasAlertaNovo se for >= 1", () => {
            const store = useConfiguracoesStore();
            store.definirDiasAlertaNovo(20);
            expect(store.diasAlertaNovo).toBe(20);
        });
    });
});
