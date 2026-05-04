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

    it("deve lidar com erro no carregamento", async () => {
        const store = useHistoricoStore();
        vi.mocked(processoService.buscarProcessosFinalizados).mockRejectedValue(new Error("Erro"));

        await store.garantirDados();

        expect(store.processos).toEqual([]);
        expect(store.carregado).toBe(true); // O código define como true mesmo no erro
    });
});
