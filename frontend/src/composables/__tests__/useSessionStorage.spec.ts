import {nextTick} from "vue";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {useSessionStorage} from "@/composables/useSessionStorage";

describe("useSessionStorage", () => {
    beforeEach(() => {
        sessionStorage.clear();
        vi.clearAllMocks();
    });

    it("deve retornar o valor padrão quando a chave não existir", () => {
        const valor = useSessionStorage("sessao-chave", "padrao");

        expect(valor.value).toBe("padrao");
    });

    it("deve retornar o valor persistido quando existir JSON válido", () => {
        sessionStorage.setItem("sessao-chave", JSON.stringify({nome: "SGC"}));

        const valor = useSessionStorage("sessao-chave", {nome: ""});

        expect(valor.value).toEqual({nome: "SGC"});
    });

    it("deve tratar valor simples quando o conteúdo não for JSON válido", () => {
        sessionStorage.setItem("sessao-chave", "texto-livre");

        const valor = useSessionStorage("sessao-chave", "padrao");

        expect(valor.value).toBe("texto-livre");
    });

    it("deve persistir alterações no sessionStorage", async () => {
        const valor = useSessionStorage("sessao-chave", "padrao");

        valor.value = "novo-valor";
        await nextTick();

        expect(sessionStorage.getItem("sessao-chave")).toBe(JSON.stringify("novo-valor"));
    });

    it("deve remover a chave quando o valor for nulo", async () => {
        sessionStorage.setItem("sessao-chave", JSON.stringify("existente"));
        const valor = useSessionStorage<string | null>("sessao-chave", "padrao");

        valor.value = null;
        await nextTick();

        expect(sessionStorage.getItem("sessao-chave")).toBeNull();
    });

    it("deve persistir mudanças profundas em objetos", async () => {
        const valor = useSessionStorage("sessao-chave", {filtros: {ativo: false}});

        valor.value.filtros.ativo = true;
        await nextTick();

        expect(sessionStorage.getItem("sessao-chave")).toBe(JSON.stringify({filtros: {ativo: true}}));
    });
});
