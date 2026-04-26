import {beforeEach, describe, expect, it, vi} from "vitest";
import {createPinia, setActivePinia} from "pinia";
import {useSubprocessoStore} from "../subprocesso";
import * as subprocessoService from "@/services/subprocessoService";

vi.mock("@/services/subprocessoService", () => ({
    buscarSubprocessoDetalhe: vi.fn(),
    buscarContextoEdicao: vi.fn(),
    buscarContextoCadastroAtividades: vi.fn(),
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarContextoEdicaoPorProcessoEUnidade: vi.fn(),
    buscarContextoCadastroAtividadesPorProcessoEUnidade: vi.fn(),
}));

describe("subprocesso store (cache e dedupe)", () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        vi.clearAllMocks();
    });

    it("deve inicializar com estado vazio", () => {
        const store = useSubprocessoStore();
        expect(store.contextoEdicao).toBeNull();
        expect(store.contextoCadastro).toBeNull();
        expect(store.erroIntegracaoContexto).toBeNull();
    });

    describe("deduplicação e sincronização", () => {
        it("deve deduplicar requisições em paralelo para o mesmo código", async () => {
            const store = useSubprocessoStore();
            let resolver!: (valor: any) => void;
            const promessa = new Promise<any>((resolve) => { resolver = resolve; });
            vi.mocked(subprocessoService.buscarContextoEdicao).mockReturnValue(promessa);

            const req1 = store.garantirContextoEdicao(10);
            const req2 = store.garantirContextoEdicao(10);

            expect(subprocessoService.buscarContextoEdicao).toHaveBeenCalledTimes(1);
            resolver({ detalhes: { codigo: 10, situacao: "TESTE" }, mapa: null });

            const res1 = await req1;
            const res2 = await req2;
            expect(res1).toBe(res2);
            expect(store.contextoEdicao?.detalhes.codigo).toBe(10);
        });

        it("deve sincronizar detalhes ao carregar contexto de cadastro", async () => {
            const store = useSubprocessoStore();
            const mockCadastro = { detalhes: { codigo: 10, situacao: "CADASTRO" }, atividades: [] };
            vi.mocked(subprocessoService.buscarContextoCadastroAtividades).mockResolvedValue(mockCadastro as any);

            await store.garantirContextoCadastroAtividades(10);

            expect(store.contextoCadastro?.detalhes.codigo).toBe(10);
            expect(store.contextoCadastro?.detalhes.situacao).toBe("CADASTRO");
            expect(store.contextoCadastro).toEqual(mockCadastro);
        });

        it("deve sincronizar detalhes ao carregar contexto de edicao", async () => {
            const store = useSubprocessoStore();
            const mockMapa = { detalhes: { codigo: 10, situacao: "MAPA" }, mapa: {} };
            vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue(mockMapa as any);

            await store.garantirContextoEdicao(10);

            expect(store.contextoEdicao?.detalhes.codigo).toBe(10);
            expect(store.contextoEdicao?.detalhes.situacao).toBe("MAPA");
            expect(store.contextoEdicao).toEqual(mockMapa);
        });
    });

    describe("invalidação", () => {
        it("deve limpar todo o estado ao invalidar", () => {
            const store = useSubprocessoStore();
            store.contextoEdicao = { detalhes: { codigo: 1 } } as any;
            store.contextoCadastro = { detalhes: { codigo: 2 }, atividades: [] } as any;
            store.invalidar();
            expect(store.contextoEdicao).toBeNull();
            expect(store.contextoCadastro).toBeNull();
            expect(store.erroIntegracaoContexto).toBeNull();
        });
    });
});
