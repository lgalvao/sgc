import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import ProcessoCadastroView from "../ProcessoCadastroView.vue";
import {createTestingPinia} from "@pinia/testing";
import * as processoService from "@/services/processo";
import {useUnidadeStore} from "@/stores/unidade";
import {useProcessoForm} from "@/composables/useProcessoForm";
import {ref} from "vue";
import {logger} from "@/utils";
import {TEXTOS} from "@/constants/textos";

const mockRoute = { query: {} };
const mockRouter = { push: vi.fn() };

vi.mock("vue-router", () => ({
    useRoute: () => mockRoute,
    useRouter: () => mockRouter,
}));

vi.mock("@/services/processo", () => ({
    criarProcesso: vi.fn().mockResolvedValue({codigo: 1}),
    iniciarProcesso: vi.fn().mockResolvedValue({}),
    excluirProcesso: vi.fn().mockResolvedValue({}),
    obterDetalhesProcesso: vi.fn()
}));

vi.mock("@/composables/useProcessoForm", async (importOriginal) => {
    const actual = await importOriginal() as any;
    return {
        ...actual,
        useProcessoForm: vi.fn(actual.useProcessoForm),
    };
});

const stubs = {
    LayoutPadrao: {template: '<div><slot></slot></div>'},
    PageHeader: {template: '<div><slot name="actions"></slot></div>'},
    BButton: {template: '<button :data-testid="$attrs[\'data-testid\']" @click="$emit(\'click\')"><slot /></button>'},
    LoadingButton: {template: '<button :data-testid="$attrs[\'data-testid\']" @click="$emit(\'click\')"><slot /></button>'},
    BAlert: {template: '<div class="b-alert"><slot /></div>'},
    AppAlert: {template: '<div class="app-alert"></div>'},
    ModalConfirmacao: {template: '<div><slot /></div>'},
    ProcessoFormFields: {template: '<div></div>'},
    BForm: {template: '<form @submit.prevent><slot /></form>'},
};

describe("ProcessoCadastroView Uncovered Branches", () => {
    let pinia: any;

    beforeEach(() => {
        vi.clearAllMocks();
        mockRoute.query = {};
        pinia = createTestingPinia({
            stubActions: false,
            initialState: {
                perfil: {
                    perfilSelecionado: 'ADMIN'
                }
            }
        });
    });

    it("cobre AppAlert clear e ModalConfirmacao v-model", async () => {
        const wrapper = mount(ProcessoCadastroView, {
            global: { plugins: [pinia], stubs }
        });
        await flushPromises();
        const vm = wrapper.vm as any;

        // Trigger notify to show AppAlert
        vm.notify("Mensagem", "success");
        await flushPromises();

        const appAlert = wrapper.findComponent({name: 'AppAlert'});
        if (appAlert.exists()) {
            await appAlert.vm.$emit("dismissed");
            expect(vm.notificacao).toBeNull();
        }

        // Trigger abrirModalRemocao to show modal and test v-model
        vm.abrirModalRemocao();
        expect(vm.mostrarModalRemocao).toBe(true);
    });

    it("cobre dispensarAlertaDiagnostico", async () => {
        const wrapper = mount(ProcessoCadastroView, {
            global: { plugins: [pinia], stubs }
        });
        await flushPromises();
        const vm = wrapper.vm as any;
        
        vm.dispensarAlertaDiagnostico();
        expect(vm.alertaDiagnosticoDispensado).toBe(true);
    });

    it("cobre erro ao buscar unidades", async () => {
        const loggerErrorSpy = vi.spyOn(logger, 'error').mockImplementation(() => {});
        const unidadeStore = useUnidadeStore(pinia);
        unidadeStore.garantirArvoreElegibilidade = vi.fn().mockRejectedValue(new Error("Erro de busca"));

        const wrapper = mount(ProcessoCadastroView, {
            global: { plugins: [pinia], stubs }
        });
        await flushPromises();
        
        const vm = wrapper.vm as any;
        await vm.buscarUnidadesParaProcesso("MAPEAMENTO");
        
        expect(loggerErrorSpy).toHaveBeenCalledWith("Erro ao buscar unidades:", expect.any(Error));
        loggerErrorSpy.mockRestore();
    });

    it("cobre onMounted carregar unidades se tipo ja definido", async () => {
        vi.mocked(useProcessoForm).mockReturnValueOnce({
            tipo: ref("MAPEAMENTO"),
            descricao: ref(""),
            dataLimite: ref(""),
            unidadesSelecionadas: ref([]),
            fieldErrors: ref({}),
            isFormInvalid: ref(false),
            setFromErroNormalizado: vi.fn(),
            clearErrors: vi.fn(),
            hasErrors: vi.fn().mockReturnValue(false),
            construirCriarRequest: vi.fn(),
            construirAtualizarRequest: vi.fn(),
            limpar: vi.fn(),
        } as any);

        const unidadeStore = useUnidadeStore(pinia);
        unidadeStore.garantirArvoreElegibilidade = vi.fn().mockResolvedValue([]);

        mount(ProcessoCadastroView, {
            global: { plugins: [pinia], stubs }
        });
        await flushPromises();

        expect(unidadeStore.garantirArvoreElegibilidade).toHaveBeenCalled();
    });

    it("cobre confirmarIniciarProcesso sem tipo", async () => {
        const wrapper = mount(ProcessoCadastroView, {
            global: { plugins: [pinia], stubs }
        });
        await flushPromises();
        const vm = wrapper.vm as any;

        // Force tipo to be falsy inside the component
        vm.formData = { ...vm.formData, tipo: null };

        vm.abrirModalConfirmacao();
        await vm.confirmarIniciarProcesso();
        
        expect(vm.mostrarModalConfirmacao).toBe(false);
        expect(vm.notificacao.mensagem).toBe(TEXTOS.processo.cadastro.ERRO_INICIAR_PROCESSO);
    });

    it("cobre confirmarRemocao limpando campos", async () => {
        const limparMock = vi.fn();
        vi.mocked(useProcessoForm).mockReturnValueOnce({
            tipo: ref("MAPEAMENTO"),
            descricao: ref(""),
            dataLimite: ref(""),
            unidadesSelecionadas: ref([]),
            fieldErrors: ref({}),
            isFormInvalid: ref(false),
            setFromErroNormalizado: vi.fn(),
            clearErrors: vi.fn(),
            hasErrors: vi.fn().mockReturnValue(false),
            construirCriarRequest: vi.fn(),
            construirAtualizarRequest: vi.fn(),
            limpar: limparMock,
        } as any);

        const wrapper = mount(ProcessoCadastroView, {
            global: { plugins: [pinia], stubs }
        });
        await flushPromises();
        const vm = wrapper.vm as any;

        vm.processoEditando = { codigo: 1, descricao: "Teste" };
        
        vi.mocked(processoService.excluirProcesso).mockImplementation(async () => {
            vm.processoEditando = null; // simulate the service deleting it and we clearing it concurrently?
            return {} as any;
        });

        await vm.confirmarRemocao();
        
        expect(limparMock).toHaveBeenCalled();
    });
});
