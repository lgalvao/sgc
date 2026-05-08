import {beforeEach, describe, expect, it, vi} from "vitest";
import {createPinia, setActivePinia} from "pinia";
import {useUnidadeStore} from "../unidade";
import * as unidadeService from "../../services/unidadeService";

vi.mock("../../services/unidadeService", () => ({
    buscarArvoreUnidade: vi.fn(),
    buscarArvoreComElegibilidade: vi.fn(),
    buscarReferenciaMapaVigente: vi.fn(),
}));

describe("useUnidadeStore", () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        vi.clearAllMocks();
    });

    it("deve buscar e cachear arvore com elegibilidade", async () => {
        const store = useUnidadeStore();
        const mockData = [{codigo: 1, filhas: [{codigo: 2, filhas: []}]}];
        vi.mocked(unidadeService.buscarArvoreComElegibilidade).mockResolvedValue(mockData as any);

        const res1 = await store.garantirArvoreElegibilidade("T1", 1);
        const res2 = await store.garantirArvoreElegibilidade("T1", 1);

        expect(res1).toEqual(mockData);
        expect(res2).toEqual(mockData);
        expect(unidadeService.buscarArvoreComElegibilidade).toHaveBeenCalledTimes(1);
    });

    it("deve propagar erro na busca da arvore", async () => {
        const store = useUnidadeStore();
        vi.mocked(unidadeService.buscarArvoreComElegibilidade).mockRejectedValue(new Error("Fail"));

        await expect(store.garantirArvoreElegibilidade("T1", 1)).rejects.toThrow("Fail");
    });

    it("deve deduplicar requisicoes paralelas", async () => {
        const store = useUnidadeStore();
        let resolvePromise: any;
        const promessa = new Promise(resolve => {
            resolvePromise = resolve;
        });
        vi.mocked(unidadeService.buscarArvoreComElegibilidade).mockReturnValue(promessa as any);

        const p1 = store.garantirArvoreElegibilidade("T1", 1);
        const p2 = store.garantirArvoreElegibilidade("T1", 1);

        resolvePromise([{codigo: 1}]);
        const [res1, res2] = await Promise.all([p1, p2]);

        expect(res1).toEqual([{codigo: 1}]);
        expect(res2).toEqual([{codigo: 1}]);
        expect(unidadeService.buscarArvoreComElegibilidade).toHaveBeenCalledTimes(1);
    });

    it("obterUnidade deve retornar null se servico retornar null", async () => {
        const store = useUnidadeStore();
        vi.mocked(unidadeService.buscarArvoreUnidade).mockResolvedValue(null as any);

        const res = await store.obterUnidade(1);
        expect(res).toBeNull();
    });

    it("obterUnidade deve retornar null se unidade nao tiver codigo", async () => {
        const store = useUnidadeStore();
        vi.mocked(unidadeService.buscarArvoreUnidade).mockResolvedValue({nome: "U"} as any);

        const res = await store.obterUnidade(1);
        expect(res).toBeNull();
    });

    it("deve reutilizar unidade ja mapeada pelo service", async () => {
        const store = useUnidadeStore();
        const unidade = {codigo: 1, nome: "U", filhas: []};
        vi.mocked(unidadeService.buscarArvoreUnidade).mockResolvedValue(unidade as any);

        const res = await store.obterUnidade(1);

        expect(res).toEqual(unidade);
        expect(store.cacheUnidades.get(1)).toEqual(unidade);
    });

    it("invalidarCache deve limpar tudo", () => {
        const store = useUnidadeStore();
        store.cacheUnidades.set(1, {codigo: 1} as any);
        store.invalidarCache();
        expect(store.cacheUnidades.size).toBe(0);
    });

    it("obterReferenciaMapaVigente deve usar cache", async () => {
        const store = useUnidadeStore();
        vi.mocked(unidadeService.buscarReferenciaMapaVigente).mockResolvedValue({codigo: 10} as any);

        await store.obterReferenciaMapaVigente(1);
        await store.obterReferenciaMapaVigente(1);

        expect(unidadeService.buscarReferenciaMapaVigente).toHaveBeenCalledTimes(1);
    });
});
