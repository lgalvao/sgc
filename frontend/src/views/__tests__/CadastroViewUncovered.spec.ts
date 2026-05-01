import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import type {ContextoCadastroAtividadesSubprocesso} from "@/types/tipos";
import {SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import CadastroView from "../CadastroView.vue";
import * as useAcessoModule from "@/composables/useAcesso";
import * as useFluxoSubprocessoModule from "@/composables/useFluxoSubprocesso";

vi.mock("@/utils/logger", () => ({
    default: { error: vi.fn(), warn: vi.fn(), info: vi.fn(), debug: vi.fn() }
}));

const estadoContextoCadastro = ref<ContextoCadastroAtividadesSubprocesso | null>(null);
const buscarContextoCadastroAtividadesPorProcessoEUnidadeMock = vi.fn();
const buscarContextoCadastroAtividadesMock = vi.fn();

const subprocessosMock = {
    get contextoCadastro() { return estadoContextoCadastro.value; },
    set contextoCadastro(v: ContextoCadastroAtividadesSubprocesso | null) { estadoContextoCadastro.value = v; },
    buscarContextoCadastroAtividadesPorProcessoEUnidade: buscarContextoCadastroAtividadesPorProcessoEUnidadeMock,
    buscarContextoCadastroAtividades: buscarContextoCadastroAtividadesMock,
    garantirContextoCadastroAtividadesPorProcessoEUnidade: buscarContextoCadastroAtividadesPorProcessoEUnidadeMock,
    garantirContextoCadastroAtividades: buscarContextoCadastroAtividadesMock,
    atualizarStatusLocal: vi.fn(),
    erroIntegracaoContexto: null,
    limparErroIntegracao: vi.fn(),
    get subprocessoDetalhe() { return estadoContextoCadastro.value?.detalhes ?? null; },
    set subprocessoDetalhe(v: any) {
        estadoContextoCadastro.value = v ? {
            ...(estadoContextoCadastro.value ?? {
                detalhes: v,
                mapa: {codigo: 100, subprocessoCodigo: 123},
                atividadesDisponiveis: [],
                unidade: {codigo: 1, sigla: "TESTE", nome: "Teste"}
            }),
            detalhes: v
        } : null;
    }
};

vi.mock("@/stores/subprocesso", () => ({
    useSubprocessoStore: () => subprocessosMock
}));

vi.mock("@/composables/useFluxoSubprocesso", () => ({
    useFluxoSubprocesso: vi.fn()
}));

vi.mock("@/composables/useValidacaoFormulario", () => ({
    useValidacaoFormulario: vi.fn(() => ({
        validarSubmissao: vi.fn(() => true),
        resetarValidacao: vi.fn(),
        focarPrimeiroErroInvalido: vi.fn()
    }))
}));

const {pushMock} = vi.hoisted(() => ({pushMock: vi.fn()}));
vi.mock("vue-router", () => ({
    useRoute: () => ({params: {codProcesso: "1", siglaUnidade: "TESTE"}, query: {}}),
    useRouter: () => ({push: pushMock}),
}));

vi.mock("@/services/analiseService", () => ({
    listarAnalisesCadastro: vi.fn().mockResolvedValue([]),
}));

const stubs = {
    LayoutPadrao: {template: '<div><slot /></div>'},
    PageHeader: { template: '<div><slot /><slot name="actions" /></div>', props: ['title'] },
    BButton: {template: '<button @click="$emit(\'click\')"><slot /></button>'},
    BFormCheckbox: { template: '<div><slot /></div>', props: ['modelValue'] },
    BSpinner: {template: '<div></div>'},
    BAlert: {template: '<div><slot /></div>', props: ['modelValue']},
    EmptyState: {template: '<div></div>'},
    LoadingButton: { template: '<button @click="$emit(\'click\')"><slot /></button>', props: ['loading', 'disabled'] },
    CadAtividadeForm: { template: '<div></div>', expose: ['inputRef'], data: () => ({ inputRef: { $el: { focus: vi.fn() } } }) },
    AtividadeItem: {template: '<div></div>', props: ['atividade']},
    ImportarAtividadesModal: {template: '<div v-if="mostrar"><button @click="$emit(\'fechar\')">Fechar</button></div>', props: ['mostrar']},
    ImpactoMapaModal: {template: '<div></div>', props: ['mostrar']},
    ConfirmacaoDisponibilizacaoModal: {template: '<div></div>', props: ['mostrar']},
    HistoricoAnaliseModal: {template: '<div></div>', props: ['mostrar']},
    ModalConfirmacao: {template: '<div v-if="modelValue"><button @click="$emit(\'confirmar\')">Confirmar</button></div>', props: ['modelValue']},
    ModalAceiteCadastro: {template: '<div v-if="modelValue"><button @click="$emit(\'confirmar\')">Confirmar</button></div>', props: ['modelValue']},
    ModalDevolucaoCadastro: {template: '<div v-if="modelValue"><button @click="$emit(\'confirmar\')">Confirmar</button></div>', props: ['modelValue']},
};

function createWrapper() {
    return mount(CadastroView, {
        global: { plugins: [createTestingPinia({stubActions: true})], stubs },
        props: { codProcesso: "1", sigla: "TESTE" }
    });
}

describe("CadastroView Uncovered Branches", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeEditarCadastro: ref(true),
            podeDisponibilizarCadastro: ref(true),
            podeVisualizarImpacto: ref(true),
            habilitarEditarCadastro: ref(true),
            habilitarDisponibilizarCadastro: ref(true),
            acaoPrincipalCadastro: ref({ codigo: 'ACEITAR', mostrar: true }),
        } as any);

        vi.mocked(useFluxoSubprocessoModule.useFluxoSubprocesso).mockReturnValue({
            iniciarRevisaoCadastro: vi.fn().mockResolvedValue(true),
            cancelarInicioRevisaoCadastro: vi.fn().mockResolvedValue(true),
            validarCadastro: vi.fn().mockResolvedValue({ valido: true }),
            disponibilizarCadastro: vi.fn().mockResolvedValue(true),
            aceitarCadastro: vi.fn().mockResolvedValue(true),
            devolverCadastro: vi.fn().mockResolvedValue(true),
            lastError: ref(null)
        } as any);
    });

    it("cobre confirmarDisponibilizacao", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;
        vm.codigoSubprocesso = 123;
        vm.subprocesso.situacao = SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO;
        vm.subprocesso.tipoProcesso = TipoProcesso.MAPEAMENTO;
        await vm.confirmarDisponibilizacao();
        expect(useFluxoSubprocessoModule.useFluxoSubprocesso().disponibilizarCadastro).toHaveBeenCalled();
    });

    it("cobre handleImportAtividades", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;
        await vm.handleImportAtividades({ atividadesAtualizadas: [], aviso: false });
        expect(vm.mostrarModalImportar).toBe(false);
    });

    it("cobre confirmarValidacaoAnalise e confirmarDevolucaoAnalise", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;
        vm.codigoSubprocesso = 123;

        await vm.confirmarValidacaoAnalise();
        expect(useFluxoSubprocessoModule.useFluxoSubprocesso().aceitarCadastro).toHaveBeenCalled();

        vm.observacaoDevolucao = "Justificativa";
        await vm.confirmarDevolucaoAnalise();
        expect(useFluxoSubprocessoModule.useFluxoSubprocesso().devolverCadastro).toHaveBeenCalled();
    });
});
