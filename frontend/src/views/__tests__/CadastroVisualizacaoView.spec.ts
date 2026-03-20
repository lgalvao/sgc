import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import * as useAcessoModule from "@/composables/useAcesso";
import * as subprocessoService from "@/services/subprocessoService";
import * as analiseService from "@/services/analiseService";
import {useProcessosStore} from "@/stores/processos";
import {useSubprocessosStore} from "@/stores/subprocessos";
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
    ModalConfirmacao: {template: '<div v-if="modelValue"> <button :data-testid="$attrs[\'test-id-confirmar\']" @click="$emit(\'confirmar\')">Confirmar</button> </div>', props: ['modelValue']},
};

describe("CadastroVisualizacaoView coverage", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        vi.mocked(subprocessoService.buscarSubprocessoPorProcessoEUnidade).mockResolvedValue(123 as any);
        vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue({
            atividadesDisponiveis: [{codigo: 1, descricao: "Ativ 1", conhecimentos: []}],
            unidade: {sigla: "TESTE", nome: "Teste"}
        } as any);
    });

    function createWrapper(accessOverrides = {}, processoDetalheOverride?: any) {
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
                    initialState: {
                        processos: {
                            processoDetalhe: processoDetalheOverride !== undefined ? processoDetalheOverride : {
                                codigo: 1,
                                tipo: "MAPEAMENTO",
                                unidades: [{sigla: "TESTE", codSubprocesso: 123}]
                            }
                        }
                    }
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

        const store = useSubprocessosStore();
        (store.aceitarCadastro as any).mockResolvedValue(true);
        (store.devolverCadastro as any).mockResolvedValue(true);
        (store.homologarCadastro as any).mockResolvedValue(true);
        (store.homologarRevisaoCadastro as any).mockResolvedValue(true);
        (store.aceitarRevisaoCadastro as any).mockResolvedValue(true);
        (store.devolverRevisaoCadastro as any).mockResolvedValue(true);

        const procStore = useProcessosStore();
        (procStore.buscarProcessoDetalhe as any).mockResolvedValue(null);

        vi.mocked(analiseService.listarAnalisesCadastro).mockResolvedValue([]);
        await wrapper.find('[data-testid="btn-vis-atividades-historico"]').trigger("click");
        await flushPromises();
        expect(analiseService.listarAnalisesCadastro).toHaveBeenCalledWith(123);

        await wrapper.find('[data-testid="btn-acao-devolver"]').trigger("click");
        (wrapper.vm as any).observacaoDevolucao = "Obs devolução";
        await wrapper.find('[data-testid="btn-devolucao-cadastro-confirmar"]').trigger("click");
        await flushPromises();
        expect(store.devolverCadastro).toHaveBeenCalled();

        (wrapper.vm as any).podeHomologarCadastro = false;
        await wrapper.find('[data-testid="btn-acao-analisar-principal"]').trigger("click");
        await wrapper.find('[data-testid="btn-aceite-cadastro-confirmar"]').trigger("click");
        await flushPromises();
        expect(store.aceitarCadastro).toHaveBeenCalled();

        (wrapper.vm as any).podeHomologarCadastro = true;
        await wrapper.find('[data-testid="btn-acao-analisar-principal"]').trigger("click");
        await wrapper.find('[data-testid="btn-aceite-cadastro-confirmar"]').trigger("click");
        await flushPromises();
        expect(store.homologarCadastro).toHaveBeenCalled();

        (procStore.processoDetalhe as any).tipo = "REVISAO";
        await wrapper.vm.$nextTick();

        (wrapper.vm as any).podeHomologarCadastro = true;
        await (wrapper.vm as any).confirmarValidacao();
        expect(store.homologarRevisaoCadastro).toHaveBeenCalled();

        // Aceitar Revisão
        (wrapper.vm as any).podeHomologarCadastro = false;
        await (wrapper.vm as any).confirmarValidacao();
        expect(store.aceitarRevisaoCadastro).toHaveBeenCalled();

        (wrapper.vm as any).observacaoDevolucao = "Rev";
        await (wrapper.vm as any).confirmarDevolucao();
        expect(store.devolverRevisaoCadastro).toHaveBeenCalled();

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
        const procStore = useProcessosStore();
        (procStore as any).processoDetalhe = null;
        expect(vm.subprocesso).toBeNull();

        // Testar subprocesso computed com arvore de unidades aninhadas
        (procStore as any).processoDetalhe = {
            unidades: [
                { sigla: "OUTRA" },
                { sigla: "PAI", filhos: [{ sigla: "TESTE", codSubprocesso: 123 }] }
            ]
        };
        expect(vm.subprocesso.codSubprocesso).toBe(123);
        (procStore as any).processoDetalhe = { unidades: [{ sigla: "OUTRA" }] };
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
        const store = useSubprocessosStore();
        expect(store.buscarSubprocessoPorProcessoEUnidade).toHaveBeenCalledWith(1, "TESTE");
    });

    it("deve tratar falhas de sucesso nas validações e não fechar modais", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;
        const store = useSubprocessosStore();

        // Falha no aceite
        (store.aceitarCadastro as any).mockResolvedValue(false);
        vm.podeHomologarCadastro = false;
        vm.mostrarModalValidar = true;
        vm.observacaoValidacao = "Teste falha";
        await vm.confirmarValidacao();
        expect(vm.mostrarModalValidar).toBe(true); // Permanece aberto

        // Falha na homologação
        (store.homologarCadastro as any).mockResolvedValue(false);
        vm.podeHomologarCadastro = true;
        await vm.confirmarValidacao();
        expect(vm.mostrarModalValidar).toBe(true);

        // Falha na devolução
        (store.devolverCadastro as any).mockResolvedValue(false);
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
