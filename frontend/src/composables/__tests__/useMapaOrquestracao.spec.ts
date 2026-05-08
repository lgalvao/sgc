import {beforeEach, describe, expect, it, vi} from "vitest";
import {useMapaOrquestracao} from "../useMapaOrquestracao";

const subprocessoStoreMock = {
    garantirContextoEdicao: vi.fn(),
    garantirContextoEdicaoPorProcessoEUnidade: vi.fn(),
};

const mapasStoreMock = {
    definirMapaCompleto: vi.fn(),
};

vi.mock("@/stores/subprocesso", () => ({
    useSubprocessoStore: () => subprocessoStoreMock,
}));

vi.mock("@/stores/mapas", () => ({
    useMapasStore: () => mapasStoreMock,
}));

describe("useMapaOrquestracao", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("deve carregar contexto do mapa por processo e unidade", async () => {
        const {carregarContextoInicial, codigoSubprocesso, unidade, carregandoInicial} = useMapaOrquestracao({
            codProcesso: 1,
            sigla: "TEST",
        });
        subprocessoStoreMock.garantirContextoEdicaoPorProcessoEUnidade.mockResolvedValue({
            codigo: 123,
            contexto: {
                detalhes: {codigo: 123},
                unidade: {sigla: "TEST", nome: "Unidade Teste"},
                mapa: {codigo: 77, competencias: []},
            },
        });

        const sucesso = await carregarContextoInicial();

        expect(sucesso).toBe(true);
        expect(codigoSubprocesso.value).toBe(123);
        expect(unidade.value).toEqual(expect.objectContaining({sigla: "TEST"}));
        expect(mapasStoreMock.definirMapaCompleto).toHaveBeenCalledWith(123, expect.objectContaining({codigo: 77}));
        expect(subprocessoStoreMock.garantirContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledWith(1, "TEST", false);
        expect(carregandoInicial.value).toBe(false);
    });

    it("deve carregar contexto do mapa por codSubprocesso em refresh direto", async () => {
        const {carregarContextoInicial, codigoSubprocesso} = useMapaOrquestracao({
            codProcesso: 1,
            sigla: "TEST",
            codSubprocesso: 456,
        });
        subprocessoStoreMock.garantirContextoEdicao.mockResolvedValue({
            detalhes: {codigo: 456},
            unidade: {sigla: "TEST", nome: "Unidade Teste"},
            mapa: {codigo: 88, competencias: []},
        });

        const sucesso = await carregarContextoInicial();

        expect(sucesso).toBe(true);
        expect(codigoSubprocesso.value).toBe(456);
        expect(subprocessoStoreMock.garantirContextoEdicao).toHaveBeenCalledWith(456, false);
        expect(subprocessoStoreMock.garantirContextoEdicaoPorProcessoEUnidade).not.toHaveBeenCalled();
    });

    it("deve retornar false quando não conseguir resolver o subprocesso", async () => {
        const {carregarContextoInicial, carregandoInicial} = useMapaOrquestracao({
            codProcesso: 1,
            sigla: "TEST",
        });
        subprocessoStoreMock.garantirContextoEdicaoPorProcessoEUnidade.mockResolvedValue(null);

        const sucesso = await carregarContextoInicial();

        expect(sucesso).toBe(false);
        expect(mapasStoreMock.definirMapaCompleto).not.toHaveBeenCalled();
        expect(carregandoInicial.value).toBe(false);
    });
});
