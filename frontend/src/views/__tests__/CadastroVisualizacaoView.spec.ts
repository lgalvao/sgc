import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import * as useAcessoModule from "@/composables/useAcesso";
import * as useFluxoSubprocessoModule from "@/composables/useFluxoSubprocesso";
import * as useSubprocessosModule from "@/composables/useSubprocessos";
import * as subprocessoService from "@/services/subprocessoService";
import * as analiseService from "@/services/analiseService";
import CadastroVisualizacaoView from "../CadastroVisualizacaoView.vue";

const {pushMock} = vi.hoisted(() => ({pushMock: vi.fn()}));

vi.mock("vue-router", () => ({
    useRoute: () => ({params: {codProcesso: "1", siglaUnidade: "TESTE"}}),
    useRouter: () => ({push: pushMock}),
}));

vi.mock("@/services/analiseService", () => ({
    listarAnalisesCadastro: vi.fn(),
}));

vi.mock("@/services/subprocessoService", () => ({
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarContextoEdicao: vi.fn(),
}));

vi.mock("@/composables/useSubprocessos", () => ({useSubprocessos: vi.fn()}));
vi.mock("@/composables/useFluxoSubprocesso", () => ({useFluxoSubprocesso: vi.fn()}));
const processosMock = {
    processoDetalhe: ref<any>(null),
    buscarProcessoDetalhe: vi.fn(),
};

vi.mock("@/composables/useProcessos", () => ({
    useProcessos: () => processosMock
}));

const stubs = {
    LayoutPadrao: {template: '<div><slot /></div>'},
    PageHeader: {
        props: ['title'],
        template: '<div><h1>{{ title }}</h1><slot /><slot name="actions" /></div>'
    },
    BButton: {
        props: ['disabled'],
        template: '<button :data-testid="$attrs[\'data-testid\']" :disabled="disabled" @click="$emit(\'click\')"><slot /></button>'
    },
    BCard: {template: '<div><slot /></div>'},
    BCardBody: {template: '<div><slot /></div>'},
    BCardTitle: {template: '<div><slot /></div>'},
    BFormGroup: {template: '<div><label><slot name="label" /></label><slot /></div>'},
    BFormTextarea: {
        props: ['modelValue'],
        template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)"></textarea>'
    },
    BFormInvalidFeedback: {template: '<div><slot /></div>'},
    ImpactoMapaModal: {template: '<div></div>', props: ['mostrar']},
    HistoricoAnaliseModal: {template: '<div></div>', props: ['mostrar']},
    ModalConfirmacao: {template: '<div v-if="modelValue"> <button :data-testid="$attrs[\'test-codigo-confirmar\']" @click="$emit(\'confirmar\')">Confirmar</button> </div>', props: ['modelValue']},
};

describe("CadastroVisualizacaoView coverage", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        vi.mocked(useSubprocessosModule.useSubprocessos).mockReturnValue({
            subprocessoDetalhe: null,
            buscarSubprocessoPorProcessoEUnidade: vi.fn().mockResolvedValue(123),
            buscarContextoEdicao: vi.fn().mockResolvedValue({
                atividadesDisponiveis: [{codigo: 1, descricao: "Ativ 1", conhecimentos: []}],
                unidade: {sigla: "TESTE", nome: "Teste"}
            }),
            buscarSubprocessoDetalhe: vi.fn(),
            atualizarStatusLocal: vi.fn(),
            lastError: null,
            clearError: vi.fn(),
        } as any);
        vi.mocked(useFluxoSubprocessoModule.useFluxoSubprocesso).mockReturnValue({
            aceitarCadastro: vi.fn().mockResolvedValue(true),
            devolverCadastro: vi.fn().mockResolvedValue(true),
            homologarCadastro: vi.fn().mockResolvedValue(true),
            homologarRevisaoCadastro: vi.fn().mockResolvedValue(true),
            aceitarRevisaoCadastro: vi.fn().mockResolvedValue(true),
            devolverRevisaoCadastro: vi.fn().mockResolvedValue(true),
        } as any);
        processosMock.processoDetalhe.value = null;
        vi.mocked(subprocessoService.buscarSubprocessoPorProcessoEUnidade).mockResolvedValue(123 as any);
        vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue({
            atividadesDisponiveis: [{codigo: 1, descricao: "Ativ 1", conhecimentos: []}],
            unidade: {sigla: "TESTE", nome: "Teste"}
        } as any);
    });

    function createWrapper(accessOverrides = {}, processoDetalheOverride?: any) {
        processosMock.processoDetalhe.value = processoDetalheOverride !== undefined ? processoDetalheOverride : {
            codigo: 1,
            tipo: "MAPEAMENTO",
            unidades: [{sigla: "TESTE", codSubprocesso: 123}]
        };

        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeHomologarCadastro: ref(true),
            podeAceitarCadastro: ref(true),
            podeDevolverCadastro: ref(true),
            podeVisualizarImpacto: ref(true),
            habilitarHomologarCadastro: ref(true),
            habilitarAceitarCadastro: ref(true),
            habilitarDevolverCadastro: ref(true),
            ...accessOverrides
        } as any);

        return mount(CadastroVisualizacaoView, {
            global: {
                plugins: [createTestingPinia({
                    stubActions: true,
                    initialState: {}
                })],
                stubs
            },
            props: {
                codProcesso: "1",
                sigla: "TESTE"
            }
        });
    }

    it("cobre fluxos de análise e modais", async () => {
        const wrapper = createWrapper();
        await flushPromises();

        const fluxoSubprocesso = useFluxoSubprocessoModule.useFluxoSubprocesso() as any;

        vi.mocked(analiseService.listarAnalisesCadastro).mockResolvedValue([]);
        await wrapper.find('[data-testid="btn-vis-atividades-historico"]').trigger("click");
        await flushPromises();
        expect(analiseService.listarAnalisesCadastro).toHaveBeenCalledWith(123);

        await wrapper.find('[data-testid="btn-acao-devolver"]').trigger("click");
        (wrapper.vm as any).observacaoDevolucao = "Obs devolução";
        await wrapper.find('[data-testid="btn-devolucao-cadastro-confirmar"]').trigger("click");
        await flushPromises();
        expect(fluxoSubprocesso.devolverCadastro).toHaveBeenCalled();

        (wrapper.vm as any).podeHomologarCadastro = false;
        await wrapper.find('[data-testid="btn-acao-analisar-principal"]').trigger("click");
        await wrapper.find('[data-testid="btn-aceite-cadastro-confirmar"]').trigger("click");
        await flushPromises();
        expect(fluxoSubprocesso.aceitarCadastro).toHaveBeenCalled();

        (wrapper.vm as any).podeHomologarCadastro = true;
        await wrapper.find('[data-testid="btn-acao-analisar-principal"]').trigger("click");
        await wrapper.find('[data-testid="btn-aceite-cadastro-confirmar"]').trigger("click");
        await flushPromises();
        expect(fluxoSubprocesso.homologarCadastro).toHaveBeenCalled();

        (processosMock.processoDetalhe.value as any).tipo = "REVISAO";
        await wrapper.vm.$nextTick();

        (wrapper.vm as any).podeHomologarCadastro = true;
        await (wrapper.vm as any).confirmarValidacao();
        expect(fluxoSubprocesso.homologarRevisaoCadastro).toHaveBeenCalled();

        // Aceitar Revisão
        (wrapper.vm as any).podeHomologarCadastro = false;
        await (wrapper.vm as any).confirmarValidacao();
        expect(fluxoSubprocesso.aceitarRevisaoCadastro).toHaveBeenCalled();

        (wrapper.vm as any).observacaoDevolucao = "Rev";
        await (wrapper.vm as any).confirmarDevolucao();
        expect(fluxoSubprocesso.devolverRevisaoCadastro).toHaveBeenCalled();

        const mapsStore = (wrapper.vm as any).mapasStore;
        mapsStore.buscarImpactoMapa = vi.fn().mockResolvedValue(null);
        await (wrapper.vm as any).abrirModalImpacto();
        expect(mapsStore.buscarImpactoMapa).toHaveBeenCalledWith(123);
    });

    it("cobre ramos condicionais adicionais", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;

        // Testar computed nomeUnidade
        vm.unidade = { sigla: "TESTE", nome: "Unidade de Teste" };
        expect(vm.nomeUnidade).toBe("Unidade de Teste");
        vm.unidade = { sigla: "TESTE", nome: null };
        expect(vm.nomeUnidade).toBe("");

        // Testar subprocesso computed com processoDetalhe nulo
        processosMock.processoDetalhe.value = null;
        expect(vm.subprocesso).toBeNull();

        // Testar subprocesso computed com arvore de unidades aninhadas
        processosMock.processoDetalhe.value = {
            unidades: [
                { sigla: "OUTRA" },
                { sigla: "PAI", filhos: [{ sigla: "TESTE", codSubprocesso: 123 }] }
            ]
        };
        expect(vm.subprocesso.codSubprocesso).toBe(123);
        processosMock.processoDetalhe.value = { unidades: [{ sigla: "OUTRA" }] };
        expect(vm.subprocesso).toBeUndefined();

        // Testar computed estadoObservacaoDevolucao
        vm.validacaoDevolucaoSubmetida = true;
        vm.observacaoDevolucao = "   ";
        expect(vm.estadoObservacaoDevolucao).toBe(false);
        vm.observacaoDevolucao = "texto";
        expect(vm.estadoObservacaoDevolucao).toBeNull();
        
        // Testar early returns
        vm.mostrarModalImpacto = true;
        vm.fecharModalImpacto();
        expect(vm.mostrarModalImpacto).toBe(false);

        vm.mostrarModalHistoricoAnalise = true;
        vm.fecharModalHistoricoAnalise();
        expect(vm.mostrarModalHistoricoAnalise).toBe(false);

        // Retornos antecipados (early return) nas funcoes assincronas
        Object.defineProperty(vm, 'codSubprocesso', { get: () => null });
        await vm.confirmarValidacao();
        
        vm.validacaoDevolucaoSubmetida = true;
        vm.observacaoDevolucao = "";
        await vm.confirmarDevolucao();
        
        // Renderização do v-for de atividades e modais via v-model não precisam de interações específicas
        // desde que as variáveis sejam preenchidas e lidas pelo wrapper.
        vm.atividades = [
            { codigo: 1, descricao: "A1", conhecimentos: [{ codigo: 1, descricao: "C1" }] }
        ];
        vm.mostrarModalValidar = true;
        vm.mostrarModalDevolver = true;
        await wrapper.vm.$nextTick();
    });

    it("deve lidar com onMounted quando codSubprocesso está ausente mas subprocesso existe", async () => {
        createWrapper({}, {
            codigo: 1,
            tipo: "MAPEAMENTO",
            unidades: [{ sigla: "TESTE" }] // codSubprocesso undefined
        });
        
        await flushPromises();
        const store = useSubprocessosModule.useSubprocessos() as any;
        expect(store.buscarSubprocessoPorProcessoEUnidade).toHaveBeenCalledWith(1, "TESTE");
    });

    it("deve tratar falhas de sucesso nas validações e não fechar modais", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;
        const fluxoSubprocesso = useFluxoSubprocessoModule.useFluxoSubprocesso() as any;

        // Falha no aceite
        fluxoSubprocesso.aceitarCadastro.mockResolvedValue(false);
        vm.podeHomologarCadastro = false;
        vm.mostrarModalValidar = true;
        vm.observacaoValidacao = "Teste falha";
        await vm.confirmarValidacao();
        expect(vm.mostrarModalValidar).toBe(true); // Permanece aberto

        // Falha na homologação
        fluxoSubprocesso.homologarCadastro.mockResolvedValue(false);
        vm.podeHomologarCadastro = true;
        await vm.confirmarValidacao();
        expect(vm.mostrarModalValidar).toBe(true);

        // Falha na devolução
        fluxoSubprocesso.devolverCadastro.mockResolvedValue(false);
        vm.mostrarModalDevolver = true;
        vm.observacaoDevolucao = "Obs";
        await vm.confirmarDevolucao();
        expect(vm.mostrarModalDevolver).toBe(true);
        
        // Fechamento manual dos modais
        vm.fecharModalValidar();
        expect(vm.mostrarModalValidar).toBe(false);
        expect(vm.observacaoValidacao).toBe("");

        vm.fecharModalDevolver();
        expect(vm.mostrarModalDevolver).toBe(false);
        expect(vm.observacaoDevolucao).toBe("");
    });

    it("deve manter devolucao e homologacao visiveis, porem desabilitadas, fora da localizacao permitida", async () => {
        const wrapper = createWrapper({
            podeHomologarCadastro: ref(true),
            podeAceitarCadastro: ref(false),
            podeDevolverCadastro: ref(true),
            habilitarHomologarCadastro: ref(false),
            habilitarAceitarCadastro: ref(false),
            habilitarDevolverCadastro: ref(false),
        });
        await flushPromises();

        expect(wrapper.find('[data-testid="btn-acao-devolver"]').attributes('disabled')).toBeDefined();
        expect(wrapper.find('[data-testid="btn-acao-analisar-principal"]').attributes('disabled')).toBeDefined();
    });
});
