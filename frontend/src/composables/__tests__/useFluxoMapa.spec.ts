import {beforeEach, describe, expect, it, vi} from "vitest";

vi.mock("@/services/subprocessoService", () => ({
    salvarMapaCompleto: vi.fn(),
    salvarMapaAjuste: vi.fn(),
    disponibilizarMapa: vi.fn(),
    adicionarCompetencia: vi.fn(),
    atualizarCompetencia: vi.fn(),
    removerCompetencia: vi.fn(),
}));

const mapasStoreMock = {
    mapaCompleto: null as any,
};

vi.mock("@/stores/mapas", () => ({
    useMapasStore: () => mapasStoreMock,
}));

describe("useFluxoMapa", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        mapasStoreMock.mapaCompleto = null;
    });

    it("deve adicionar competencia e atualizar mapaCompleto", async () => {
        const {useFluxoMapa} = await import("../useFluxoMapa");
        const fluxoMapa = useFluxoMapa();
        const service = await import("@/services/subprocessoService");
        const resposta = {codigo: 1, competencias: [{codigo: 2}]} as any;
        vi.mocked(service.adicionarCompetencia).mockResolvedValue(resposta);

        await fluxoMapa.adicionarCompetencia(10, {descricao: "Comp", atividadesIds: []});

        expect(service.adicionarCompetencia).toHaveBeenCalledWith(10, {descricao: "Comp", atividadesIds: []});
        expect(mapasStoreMock.mapaCompleto).toEqual(resposta);
    });

    it("deve atualizar competencia e atualizar mapaCompleto", async () => {
        const {useFluxoMapa} = await import("../useFluxoMapa");
        const fluxoMapa = useFluxoMapa();
        const service = await import("@/services/subprocessoService");
        const resposta = {codigo: 1, competencias: [{codigo: 3}]} as any;
        vi.mocked(service.atualizarCompetencia).mockResolvedValue(resposta);

        await fluxoMapa.atualizarCompetencia(10, 20, {descricao: "Comp", atividadesIds: [1]});

        expect(service.atualizarCompetencia).toHaveBeenCalledWith(10, 20, {descricao: "Comp", atividadesIds: [1]});
        expect(mapasStoreMock.mapaCompleto).toEqual(resposta);
    });

    it("deve remover competencia e atualizar mapaCompleto", async () => {
        const {useFluxoMapa} = await import("../useFluxoMapa");
        const fluxoMapa = useFluxoMapa();
        const service = await import("@/services/subprocessoService");
        const resposta = {codigo: 1, competencias: []} as any;
        vi.mocked(service.removerCompetencia).mockResolvedValue(resposta);

        await fluxoMapa.removerCompetencia(10, 20);

        expect(service.removerCompetencia).toHaveBeenCalledWith(10, 20);
        expect(mapasStoreMock.mapaCompleto).toEqual(resposta);
    });

    it("deve disponibilizar mapa", async () => {
        const {useFluxoMapa} = await import("../useFluxoMapa");
        const fluxoMapa = useFluxoMapa();
        const service = await import("@/services/subprocessoService");
        vi.mocked(service.disponibilizarMapa).mockResolvedValue(undefined);

        await fluxoMapa.disponibilizarMapa(10, {dataLimite: "2026-05-01", observacoes: "obs"});

        expect(service.disponibilizarMapa).toHaveBeenCalledWith(10, {dataLimite: "2026-05-01", observacoes: "obs"});
    });
});
