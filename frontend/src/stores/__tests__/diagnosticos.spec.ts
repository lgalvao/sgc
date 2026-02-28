import {beforeEach, describe, expect, it, vi} from "vitest";
import {createPinia, setActivePinia} from "pinia";
import {useDiagnosticosStore} from "@/stores/diagnosticos";
import {diagnosticoService} from "@/services/diagnosticoService";

vi.mock("@/services/diagnosticoService", () => ({
    diagnosticoService: {
        buscarDiagnostico: vi.fn(),
        buscarMinhasAvaliacoes: vi.fn(),
        salvarAvaliacao: vi.fn(),
        concluirAutoavaliacao: vi.fn(),
        concluirDiagnostico: vi.fn(),
        buscarOcupacoes: vi.fn(),
        salvarOcupacao: vi.fn(),
    }
}));

describe("Diagnosticos Store", () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        vi.clearAllMocks();
    });

    it("buscarDiagnostico com sucesso", async () => {
        const store = useDiagnosticosStore();
        const mockData = {codigo: 1, situacao: 'EM_ANDAMENTO'};
        vi.mocked(diagnosticoService.buscarDiagnostico).mockResolvedValue(mockData as any);

        await store.buscarDiagnostico(1);

        expect(diagnosticoService.buscarDiagnostico).toHaveBeenCalledWith(1);
        expect(store.diagnostico).toEqual(mockData);
    });

    it("buscarDiagnostico com erro limpa diagnostico", async () => {
        const store = useDiagnosticosStore();
        store.diagnostico = {codigo: 1} as any;
        vi.mocked(diagnosticoService.buscarDiagnostico).mockRejectedValue(new Error("Erro"));

        try {
            await store.buscarDiagnostico(1);
        } catch {
            // expected
        }

        expect(store.diagnostico).toBeNull();
    });

    it("buscarMinhasAvaliacoes com sucesso", async () => {
        const store = useDiagnosticosStore();
        const mockData = [{competenciaCodigo: 1, dominio: 'ALTO'}];
        vi.mocked(diagnosticoService.buscarMinhasAvaliacoes).mockResolvedValue(mockData as any);

        await store.buscarMinhasAvaliacoes(1, "123");

        expect(diagnosticoService.buscarMinhasAvaliacoes).toHaveBeenCalledWith(1, "123");
        expect(store.avaliacoes).toEqual(mockData);
    });

    it("salvarAvaliacao adiciona nova se não existir", async () => {
        const store = useDiagnosticosStore();
        const novaAvaliacao = {competenciaCodigo: 1, dominio: 'ALTO'};
        vi.mocked(diagnosticoService.salvarAvaliacao).mockResolvedValue(novaAvaliacao as any);

        await store.salvarAvaliacao(1, 1, 'MUITO', 'ALTO');

        expect(store.avaliacoes).toContainEqual(novaAvaliacao);
    });

    it("salvarAvaliacao atualiza se já existir", async () => {
        const store = useDiagnosticosStore();
        store.avaliacoes = [{competenciaCodigo: 1, dominio: 'BAIXO'} as any];
        const novaAvaliacao = {competenciaCodigo: 1, dominio: 'ALTO'};
        vi.mocked(diagnosticoService.salvarAvaliacao).mockResolvedValue(novaAvaliacao as any);

        await store.salvarAvaliacao(1, 1, 'MUITO', 'ALTO');

        expect(store.avaliacoes.length).toBe(1);
        expect(store.avaliacoes[0].dominio).toBe('ALTO');
    });

    it("concluirAutoavaliacao", async () => {
        const store = useDiagnosticosStore();
        vi.mocked(diagnosticoService.concluirAutoavaliacao).mockResolvedValue(undefined);

        await store.concluirAutoavaliacao(1, "atraso");

        expect(diagnosticoService.concluirAutoavaliacao).toHaveBeenCalledWith(1, "atraso");
    });

    it("concluirDiagnostico", async () => {
        const store = useDiagnosticosStore();
        const mockResult = {codigo: 1, situacao: 'CONCLUIDO'};
        vi.mocked(diagnosticoService.concluirDiagnostico).mockResolvedValue(mockResult as any);

        await store.concluirDiagnostico(1, "justificativa");

        expect(store.diagnostico).toEqual(mockResult);
    });

    it("buscarOcupacoes", async () => {
        const store = useDiagnosticosStore();
        const mockData = [{competenciaCodigo: 1}];
        vi.mocked(diagnosticoService.buscarOcupacoes).mockResolvedValue(mockData as any);

        await store.buscarOcupacoes(1);

        expect(store.ocupacoes).toEqual(mockData);
    });

    it("salvarOcupacao adiciona ou atualiza", async () => {
        const store = useDiagnosticosStore();
        const novaOcupacao = {competenciaCodigo: 1, situacao: 'OK'};
        vi.mocked(diagnosticoService.salvarOcupacao).mockResolvedValue(novaOcupacao as any);

        // Adiciona
        await store.salvarOcupacao(1, "tit", 1, "OK");
        expect(store.ocupacoes).toContainEqual(novaOcupacao);

        // Atualiza
        const atualizada = {competenciaCodigo: 1, situacao: 'NOK'};
        vi.mocked(diagnosticoService.salvarOcupacao).mockResolvedValue(atualizada as any);
        await store.salvarOcupacao(1, "tit", 1, "NOK");

        expect(store.ocupacoes.length).toBe(1);
        expect(store.ocupacoes[0].situacao).toBe('NOK');
    });
});
