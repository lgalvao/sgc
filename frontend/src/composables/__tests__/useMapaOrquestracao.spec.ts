import {beforeEach, describe, expect, it, vi} from "vitest";
import {defineComponent, h} from "vue";
import {flushPromises, mount} from "@vue/test-utils";
import {useMapaOrquestracao} from "../useMapaOrquestracao";

const subprocessoStoreMock = {
    obterContextoEdicao: vi.fn(),
    obterContextoEdicaoPorProcessoEUnidade: vi.fn(),
    dadosEdicaoValidos: vi.fn(),
};

const mapasStoreMock = {
    sincronizarMapa: vi.fn(),
};

vi.mock("@/stores/subprocesso", () => ({
    useSubprocessoStore: () => subprocessoStoreMock,
}));

vi.mock("@/stores/mapas", () => ({
    useMapasStore: () => mapasStoreMock,
}));

describe("useMapaOrquestracao", () => {
    const criarComponenteTeste = (props: {codProcesso: number; sigla: string; codSubprocesso?: number}) => defineComponent({
        setup() {
            return useMapaOrquestracao(props);
        },
        render() {
            return h("div");
        },
    });

    beforeEach(() => {
        vi.clearAllMocks();
        subprocessoStoreMock.dadosEdicaoValidos.mockReturnValue(false);
    });

    it("deve carregar contexto do mapa por processo e unidade", async () => {
        const {carregarContextoInicial, codigoSubprocesso, unidade, carregandoInicial} = useMapaOrquestracao({
            codProcesso: 1,
            sigla: "TEST",
        });
        subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade.mockResolvedValue({
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
        expect(mapasStoreMock.sincronizarMapa).toHaveBeenCalledWith(123, expect.objectContaining({codigo: 77}));
        expect(subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledWith(1, "TEST");
        expect(carregandoInicial.value).toBe(false);
    });

    it("deve carregar contexto do mapa por codSubprocesso em refresh direto", async () => {
        const {carregarContextoInicial, codigoSubprocesso} = useMapaOrquestracao({
            codProcesso: 1,
            sigla: "TEST",
            codSubprocesso: 456,
        });
        subprocessoStoreMock.obterContextoEdicao.mockResolvedValue({
            detalhes: {codigo: 456},
            unidade: {sigla: "TEST", nome: "Unidade Teste"},
            mapa: {codigo: 88, competencias: []},
        });

        const sucesso = await carregarContextoInicial();

        expect(sucesso).toBe(true);
        expect(codigoSubprocesso.value).toBe(456);
        expect(subprocessoStoreMock.obterContextoEdicao).toHaveBeenCalledWith(456);
        expect(subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade).not.toHaveBeenCalled();
    });

    it("deve retornar false quando não conseguir resolver o subprocesso", async () => {
        const {carregarContextoInicial, carregandoInicial} = useMapaOrquestracao({
            codProcesso: 1,
            sigla: "TEST",
        });
        subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade.mockResolvedValue(null);

        const sucesso = await carregarContextoInicial();

        expect(sucesso).toBe(false);
        expect(mapasStoreMock.sincronizarMapa).not.toHaveBeenCalled();
        expect(carregandoInicial.value).toBe(false);
    });

    it("deve recarregar ao reativar quando o contexto estiver inválido", async () => {
        subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade
            .mockResolvedValue({codigo: 123, contexto: {detalhes: {codigo: 123}, unidade: {sigla: "TEST"}, mapa: {codigo: 77}} as any});

        const wrapper = mount(criarComponenteTeste({codProcesso: 1, sigla: "TEST"}));
        await wrapper.vm.carregarContextoInicial();
        await flushPromises();

        subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade.mockClear();
        subprocessoStoreMock.dadosEdicaoValidos.mockReturnValue(false);
        // @ts-expect-error acesso interno para simular ativação keepAlive
        await wrapper.vm.$.a?.[0]();

        expect(subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledWith(1, "TEST");
    });

    it("não deve recarregar ao reativar quando o contexto ainda for válido", async () => {
        subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade
            .mockResolvedValue({codigo: 123, contexto: {detalhes: {codigo: 123}, unidade: {sigla: "TEST"}, mapa: {codigo: 77}} as any});

        const wrapper = mount(criarComponenteTeste({codProcesso: 1, sigla: "TEST"}));
        await wrapper.vm.carregarContextoInicial();
        await flushPromises();

        subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade.mockClear();
        subprocessoStoreMock.dadosEdicaoValidos.mockReturnValue(true);
        // @ts-expect-error acesso interno para simular ativação keepAlive
        await wrapper.vm.$.a?.[0]();

        expect(subprocessoStoreMock.obterContextoEdicaoPorProcessoEUnidade).not.toHaveBeenCalled();
    });
});
