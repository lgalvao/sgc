import {nextTick} from "vue";
import {beforeEach, describe, expect, it} from "vitest";
import {useWebStorage} from "@/composables/useWebStorage";

type ArmazenamentoFake = {
    getItem: (chave: string) => string | null;
    setItem: (chave: string, valor: string) => void;
    removeItem: (chave: string) => void;
    dump: () => Record<string, string>;
};

function criarArmazenamentoFake(seed: Record<string, string> = {}): ArmazenamentoFake {
    const dados = new Map<string, string>(Object.entries(seed));

    return {
        getItem: (chave) => dados.get(chave) ?? null,
        setItem: (chave, valor) => {
            dados.set(chave, valor);
        },
        removeItem: (chave) => {
            dados.delete(chave);
        },
        dump: () => Object.fromEntries(dados.entries()),
    };
}

describe("armazenamento web composables", () => {
    beforeEach(() => {
        localStorage.clear();
        sessionStorage.clear();
    });

    describe("useWebStorage", () => {
        it("deve usar valor padrão quando não houver chave persistida", () => {
            const armazenamento = criarArmazenamentoFake();

            const valor = useWebStorage(armazenamento, "chave", "padrao");

            expect(valor.value).toBe("padrao");
        });

        it("deve ler JSON válido persistido", () => {
            const armazenamento = criarArmazenamentoFake({
                chave: JSON.stringify({filtro: {ativo: true}}),
            });

            const valor = useWebStorage(armazenamento, "chave", {filtro: {ativo: false}});

            expect(valor.value).toEqual({filtro: {ativo: true}});
        });

        it("deve tratar valor não-JSON como texto puro", () => {
            const armazenamento = criarArmazenamentoFake({chave: "texto-livre"});

            const valor = useWebStorage(armazenamento, "chave", "padrao");

            expect(valor.value).toBe("texto-livre");
        });

        it("deve persistir atualizações e remover quando valor for nulo", async () => {
            const armazenamento = criarArmazenamentoFake({chave: JSON.stringify("inicial")});
            const valor = useWebStorage<string | null>(armazenamento, "chave", "padrao");

            valor.value = "atualizado";
            await nextTick();
            expect(armazenamento.dump()).toEqual({chave: JSON.stringify("atualizado")});

            valor.value = null;
            await nextTick();
            expect(armazenamento.dump()).toEqual({});
        });

        it("deve persistir mutações profundas", async () => {
            const armazenamento = criarArmazenamentoFake();
            const valor = useWebStorage(armazenamento, "chave", {filtros: {ativo: false}});

            valor.value.filtros.ativo = true;
            await nextTick();

            expect(armazenamento.dump()).toEqual({
                chave: JSON.stringify({filtros: {ativo: true}}),
            });
        });
    });

    describe("com localStorage e sessionStorage reais", () => {
        it("deve persistir no localStorage sem tocar sessionStorage", async () => {
            const valor = useWebStorage(localStorage, "chave-local", "padrao");

            valor.value = "novo-valor";
            await nextTick();

            expect(localStorage.getItem("chave-local")).toBe(JSON.stringify("novo-valor"));
            expect(sessionStorage.getItem("chave-local")).toBeNull();
        });

        it("deve persistir no sessionStorage sem tocar localStorage", async () => {
            const valor = useWebStorage(sessionStorage, "chave-sessao", "padrao");

            valor.value = "novo-valor";
            await nextTick();

            expect(sessionStorage.getItem("chave-sessao")).toBe(JSON.stringify("novo-valor"));
            expect(localStorage.getItem("chave-sessao")).toBeNull();
        });
    });
});
