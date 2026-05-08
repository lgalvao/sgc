import {beforeEach, describe, expect, it, vi} from "vitest";
import {createPinia, setActivePinia} from "pinia";
import {useHistoricoStore} from "../historico";
import * as processoService from "@/services/processo";

vi.mock("@/services/processo", () => ({
    buscarProcessosFinalizados: vi.fn()
}));

describe("useHistoricoStore", () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        vi.clearAllMocks();
    });

    it("deve iniciar com estado vazio", () => {
        const store = useHistoricoStore();
        expect(store.processos).toEqual([]);
        expect(store.carregado).toBe(false);
    });

    it("deve definir dados manualmente", () => {
        const store = useHistoricoStore();
        const processos = [{codigo: 1} as any];
        store.definirDados(processos);
        expect(store.processos).toEqual(processos);
        expect(store.carregado).toBe(true);
    });

    it("deve invalidar dados", () => {
        const store = useHistoricoStore();
        store.definirDados([]);
        store.invalidar();
        expect(store.carregado).toBe(false);
        expect(store.processos).toEqual([]);
    });

    it("resetar deve limpar completamente o estado", () => {
        const store = useHistoricoStore();
        const processos = [{codigo: 1} as any];
        store.definirDados(processos);

        store.resetar();

        expect(store.processos).toEqual([]);
        expect(store.carregado).toBe(false);
        expect(store.dadosValidos()).toBe(false);
    });

    it("deve garantir dados carregando do serviço", async () => {
        const store = useHistoricoStore();
        const processos = [{codigo: 1} as any];
        vi.mocked(processoService.buscarProcessosFinalizados).mockResolvedValue(processos);

        await store.garantirDados();

        expect(store.processos).toEqual(processos);
        expect(store.carregado).toBe(true);
        expect(processoService.buscarProcessosFinalizados).toHaveBeenCalledTimes(1);
    });

    it("não deve carregar novamente se já estiver carregado e não forçado", async () => {
        const store = useHistoricoStore();
        store.definirDados([]);

        await store.garantirDados();

        expect(processoService.buscarProcessosFinalizados).not.toHaveBeenCalled();
    });

    it("deve carregar novamente se forçado mesmo se já estiver carregado", async () => {
        const store = useHistoricoStore();
        store.definirDados([]);
        vi.mocked(processoService.buscarProcessosFinalizados).mockResolvedValue([]);

        await store.garantirDados(true);

        expect(processoService.buscarProcessosFinalizados).toHaveBeenCalledTimes(1);
    });

    it("deve deduplicar chamadas concorrentes a garantirDados", async () => {
        const store = useHistoricoStore();
        let resolver: (v: any) => void;
        const promessa = new Promise<any>(r => { resolver = r; });
        vi.mocked(processoService.buscarProcessosFinalizados).mockReturnValue(promessa);

        const p1 = store.garantirDados();
        const p2 = store.garantirDados();
        resolver!([{codigo: 1}]);
        await Promise.all([p1, p2]);

        expect(processoService.buscarProcessosFinalizados).toHaveBeenCalledTimes(1);
        expect(store.processos).toEqual([{codigo: 1}]);
    });

    it("deve propagar erro no carregamento", async () => {
        const store = useHistoricoStore();
        vi.mocked(processoService.buscarProcessosFinalizados).mockRejectedValue(new Error("Erro"));

        await expect(store.garantirDados()).rejects.toThrow("Erro");

        expect(store.processos).toEqual([]);
        expect(store.carregado).toBe(false);
    });
});
