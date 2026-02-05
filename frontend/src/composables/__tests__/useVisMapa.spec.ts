import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import {defineComponent, nextTick, reactive} from "vue";
import {createTestingPinia} from "@pinia/testing";
import {useVisMapa} from "@/composables/useVisMapa";
import {useProcessosStore} from "@/stores/processos";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useMapasStore} from "@/stores/mapas";
import {useUnidadesStore} from "@/stores/unidades";
import {useAnalisesStore} from "@/stores/analises";
import {useFeedbackStore} from "@/stores/feedback";
import {useRoute, useRouter} from "vue-router";
import {TipoProcesso} from "@/types/tipos";
import {usePerfil} from "@/composables/usePerfil";

vi.mock("vue-router", async (importOriginal) => {
    const actual = await importOriginal<typeof import("vue-router")>();
    return {
        ...actual,
        useRoute: vi.fn(),
        useRouter: vi.fn(() => ({
            push: vi.fn(),
        })),
    };
});

vi.mock("@/composables/usePerfil", () => ({
    usePerfil: vi.fn(),
}));

const TestComponent = defineComponent({
    setup() {
        const visMapa = useVisMapa();
        return { ...visMapa };
    },
    template: "<div></div>",
});

describe("useVisMapa", () => {
    let wrapper: any;
    let processosStore: any;
    let subprocessosStore: any;
    let feedbackStore: any;
    let router: any;

    const setup = (initialState = {}, routeParams = { siglaUnidade: "TEST", codProcesso: "1" }, perfil = "GESTOR") => {
        vi.mocked(useRoute).mockReturnValue({
            params: reactive(routeParams)
        } as any);

        router = { push: vi.fn() };
        vi.mocked(useRouter).mockReturnValue(router as any);

        vi.mocked(usePerfil).mockReturnValue({
            perfilSelecionado: { value: perfil }
        } as any);

        const pinia = createTestingPinia({
            createSpy: vi.fn,
            stubActions: true,
        });

        processosStore = useProcessosStore(pinia);
        subprocessosStore = useSubprocessosStore(pinia);
        feedbackStore = useFeedbackStore(pinia);

        processosStore.processoDetalhe = {
            tipo: TipoProcesso.MAPEAMENTO,
            unidades: [
                { sigla: "TEST", codSubprocesso: 10 }
            ]
        };

        if (initialState['processos']?.processoDetalhe) {
            processosStore.processoDetalhe = initialState['processos'].processoDetalhe;
        }

        wrapper = mount(TestComponent, {
            global: {
                plugins: [pinia]
            }
        });
    };

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("inicializa dados no onMounted", async () => {
        setup();
        const unidadesStore = useUnidadesStore();
        const mapasStore = useMapasStore();

        await flushPromises();
        expect(unidadesStore.buscarUnidade).toHaveBeenCalledWith("TEST");
        expect(processosStore.buscarProcessoDetalhe).toHaveBeenCalledWith(1);
        expect(subprocessosStore.buscarSubprocessoDetalhe).toHaveBeenCalledWith(10);
        expect(mapasStore.buscarMapaVisualizacao).toHaveBeenCalledWith(10);
    });

    it("confirmarSugestoes com sucesso", async () => {
        setup();
        processosStore.apresentarSugestoes.mockResolvedValue({});

        wrapper.vm.sugestoes = "Novas sugestoes";
        await wrapper.vm.confirmarSugestoes();

        expect(processosStore.apresentarSugestoes).toHaveBeenCalledWith(10, { sugestoes: "Novas sugestoes" });
        expect(feedbackStore.show).toHaveBeenCalledWith(expect.any(String), expect.any(String), "success");
        expect(router.push).toHaveBeenCalledWith({ name: "Painel" });
    });

    it("confirmarValidacao com sucesso", async () => {
        setup();
        processosStore.validarMapa.mockResolvedValue({});

        await wrapper.vm.confirmarValidacao();

        expect(processosStore.validarMapa).toHaveBeenCalledWith(10);
        expect(feedbackStore.show).toHaveBeenCalledWith(expect.any(String), expect.any(String), "success");
        expect(router.push).toHaveBeenCalledWith({ name: "Painel" });
    });

    it("confirmarAceitacao como gestor (aceitarValidacao)", async () => {
        setup();
        processosStore.aceitarValidacao.mockResolvedValue({});

        await wrapper.vm.confirmarAceitacao("Obs");

        expect(processosStore.aceitarValidacao).toHaveBeenCalledWith(10, { observacoes: "Obs" });
        expect(router.push).toHaveBeenCalledWith({ name: "Painel" });
    });

    it("confirmarAceitacao como admin em processo de revisao (homologarRevisaoCadastro)", async () => {
        setup({
            processos: {
                processoDetalhe: {
                    tipo: TipoProcesso.REVISAO,
                    unidades: [{ sigla: "TEST", codSubprocesso: 10 }]
                }
            }
        }, { siglaUnidade: "TEST", codProcesso: "1" }, "ADMIN");

        subprocessosStore.homologarRevisaoCadastro.mockResolvedValue({});

        await wrapper.vm.confirmarAceitacao("Obs Admin");

        expect(subprocessosStore.homologarRevisaoCadastro).toHaveBeenCalledWith(10, { observacoes: "Obs Admin" });
        expect(router.push).toHaveBeenCalledWith({ name: "Painel" });
    });

    it("confirmarAceitacao como admin em processo de mapeamento (homologarValidacao)", async () => {
        setup({}, { siglaUnidade: "TEST", codProcesso: "1" }, "ADMIN");
        processosStore.homologarValidacao.mockResolvedValue({});

        await wrapper.vm.confirmarAceitacao();

        expect(processosStore.homologarValidacao).toHaveBeenCalledWith(10);
        expect(router.push).toHaveBeenCalledWith({ name: "Painel" });
    });

    it("confirmarDevolucao com sucesso", async () => {
        setup();
        subprocessosStore.devolverRevisaoCadastro.mockResolvedValue({});

        wrapper.vm.observacaoDevolucao = "Volta tudo";
        await wrapper.vm.confirmarDevolucao();

        expect(subprocessosStore.devolverRevisaoCadastro).toHaveBeenCalledWith(10, { observacoes: "Volta tudo" });
        expect(router.push).toHaveBeenCalledWith({ name: "Painel" });
    });

    it("gerencia modais corretamente", () => {
        setup();

        wrapper.vm.abrirModalAceitar();
        expect(wrapper.vm.mostrarModalAceitar).toBe(true);
        wrapper.vm.fecharModalAceitar();
        expect(wrapper.vm.mostrarModalAceitar).toBe(false);

        wrapper.vm.abrirModalSugestoes();
        wrapper.vm.sugestoes = "abc";
        expect(wrapper.vm.mostrarModalSugestoes).toBe(true);
        wrapper.vm.fecharModalSugestoes();
        expect(wrapper.vm.mostrarModalSugestoes).toBe(false);
        expect(wrapper.vm.sugestoes).toBe("");

        wrapper.vm.verSugestoes();
        expect(wrapper.vm.mostrarModalVerSugestoes).toBe(true);
        wrapper.vm.fecharModalVerSugestoes();
        expect(wrapper.vm.mostrarModalVerSugestoes).toBe(false);

        wrapper.vm.abrirModalValidar();
        expect(wrapper.vm.mostrarModalValidar).toBe(true);
        wrapper.vm.fecharModalValidar();
        expect(wrapper.vm.mostrarModalValidar).toBe(false);

        wrapper.vm.abrirModalDevolucao();
        wrapper.vm.observacaoDevolucao = "obs";
        expect(wrapper.vm.mostrarModalDevolucao).toBe(true);
        wrapper.vm.fecharModalDevolucao();
        expect(wrapper.vm.mostrarModalDevolucao).toBe(false);
        expect(wrapper.vm.observacaoDevolucao).toBe("");
    });

    it("abrirModalHistorico busca analises", async () => {
        setup();
        const analisesStore = useAnalisesStore();

        await wrapper.vm.abrirModalHistorico();
        expect(analisesStore.buscarAnalisesCadastro).toHaveBeenCalledWith(10);
        expect(wrapper.vm.mostrarModalHistorico).toBe(true);

        wrapper.vm.fecharModalHistorico();
        expect(wrapper.vm.mostrarModalHistorico).toBe(false);
    });

    it("computa historicoAnalise corretamente", async () => {
        const mockAnalises = [{ codigo: 1, descricao: "Analise 1" }];
        setup();
        const analisesStore = useAnalisesStore();
        vi.mocked(analisesStore.obterAnalisesPorSubprocesso).mockReturnValue(mockAnalises);

        await nextTick();
        expect(wrapper.vm.historicoAnalise).toEqual(mockAnalises);
        expect(wrapper.vm.temHistoricoAnalise).toBe(true);
    });

    it("computa permissoes corretamente", async () => {
        const permissoes = { podeValidarMapa: true };
        setup();

        subprocessosStore.subprocessoDetalhe = { permissoes };

        await nextTick();
        expect(wrapper.vm.permissoes).toEqual(permissoes);
        expect(wrapper.vm.podeValidar).toBe(true);
    });

    it("lidar com erro em confirmarSugestoes", async () => {
        setup();
        processosStore.apresentarSugestoes.mockRejectedValue(new Error("Erro"));
        await wrapper.vm.confirmarSugestoes();
        expect(feedbackStore.show).toHaveBeenCalledWith(expect.any(String), expect.any(String), "danger");
    });

    it("lidar com erro em confirmarValidacao", async () => {
        setup();
        processosStore.validarMapa.mockRejectedValue(new Error("Erro"));
        await wrapper.vm.confirmarValidacao();
        expect(feedbackStore.show).toHaveBeenCalledWith(expect.any(String), expect.any(String), "danger");
    });

    it("lidar com erro em confirmarAceitacao", async () => {
        setup();
        processosStore.aceitarValidacao.mockRejectedValue(new Error("Erro"));
        await wrapper.vm.confirmarAceitacao();
        expect(feedbackStore.show).toHaveBeenCalledWith("Erro", "Erro ao realizar a operação.", "danger");
    });

    it("lidar com erro em confirmarDevolucao", async () => {
        setup();
        subprocessosStore.devolverRevisaoCadastro.mockRejectedValue(new Error("Erro"));
        await wrapper.vm.confirmarDevolucao();
        expect(feedbackStore.show).toHaveBeenCalledWith("Erro", "Erro ao devolver.", "danger");
    });
});
