import {describe, expect, it, vi, beforeEach} from "vitest";
import {setActivePinia, createPinia} from "pinia";
import {useRelatoriosStore} from "@/stores/relatorios";
import {relatoriosService} from "@/services/relatoriosService";

vi.mock("@/services/relatoriosService", () => ({
  relatoriosService: {
    obterRelatorioAndamento: vi.fn(),
    downloadRelatorioAndamentoPdf: vi.fn(),
    downloadRelatorioMapasPdf: vi.fn(),
  },
}));

describe("Relatorios Store", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it("deve buscar relatorio de andamento com sucesso", async () => {
    const store = useRelatoriosStore();
    const mockData = [{siglaUnidade: "UNIT1", nomeUnidade: "Unit 1", situacaoAtual: "OK"}];
    vi.mocked(relatoriosService.obterRelatorioAndamento).mockResolvedValue(mockData as any);

    await store.buscarRelatorioAndamento(123);

    expect(relatoriosService.obterRelatorioAndamento).toHaveBeenCalledWith(123);
    expect(store.relatorioAndamento).toEqual(mockData);
    expect(store.lastError).toBeNull();
  });

  it("deve tratar erro ao buscar relatorio de andamento", async () => {
    const store = useRelatoriosStore();
    vi.mocked(relatoriosService.obterRelatorioAndamento).mockRejectedValue(new Error("Erro API"));

    await expect(store.buscarRelatorioAndamento(123)).rejects.toThrow("Erro API");

    expect(store.relatorioAndamento).toEqual([]);
    expect(store.lastError).not.toBeNull();
  });

  it("deve exportar andamento PDF com sucesso", async () => {
    const store = useRelatoriosStore();
    vi.mocked(relatoriosService.downloadRelatorioAndamentoPdf).mockResolvedValue(undefined);

    await store.exportarAndamentoPdf(123);

    expect(relatoriosService.downloadRelatorioAndamentoPdf).toHaveBeenCalledWith(123);
    expect(store.lastError).toBeNull();
  });

  it("deve exportar mapas PDF com sucesso", async () => {
    const store = useRelatoriosStore();
    vi.mocked(relatoriosService.downloadRelatorioMapasPdf).mockResolvedValue(undefined);

    await store.exportarMapasPdf(123, 456);

    expect(relatoriosService.downloadRelatorioMapasPdf).toHaveBeenCalledWith(123, 456);
    expect(store.lastError).toBeNull();
  });

  it("deve limpar relatorio", () => {
    const store = useRelatoriosStore();
    store.relatorioAndamento = [{siglaUnidade: "UNIT1"}] as any;

    store.limparRelatorio();

    expect(store.relatorioAndamento).toEqual([]);
    expect(store.lastError).toBeNull();
  });
});
