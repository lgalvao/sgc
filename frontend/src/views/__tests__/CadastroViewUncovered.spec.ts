import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import {SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import CadastroView from "../CadastroView.vue";
import * as useAcessoModule from "@/composables/useAcesso";
import * as useFluxoSubprocessoModule from "@/composables/useFluxoSubprocesso";

vi.mock("@/utils/logger", () => ({
    default: { error: vi.fn(), warn: vi.fn(), info: vi.fn(), debug: vi.fn() }
}));

const estadoContextoCadastro = ref({
    detalhes: {
        codigo: 123,
        situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        permissoes: { podeEditarCadastro: true }
    },
    mapa: {codigo: 100, subprocessoCodigo: 123},
    atividadesDisponiveis: [],
    unidade: {codigo: 1, sigla: "TESTE", nome: "Teste"}
});

vi.mock("@/stores/subprocesso", () => ({
    useSubprocessoStore: () => ({
        contextoCadastro: estadoContextoCadastro.value,
        buscarContextoCadastroAtividadesPorProcessoEUnidade: vi.fn(),
        buscarContextoCadastroAtividades: vi.fn(),
        garantirContextoCadastroAtividadesPorProcessoEUnidade: vi.fn(),
        garantirContextoCadastroAtividades: vi.fn(),
        atualizarStatusLocal: vi.fn(),
        erroIntegracaoContexto: null,
        limparErroIntegracao: vi.fn(),
    })
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

vi.mock("vue-router", () => ({
    useRoute: () => ({params: {codProcesso: "1", siglaUnidade: "TESTE"}, query: {}}),
    useRouter: () => ({push: vi.fn()}),
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
    CadastroAcoesHeader: {template: '<div><button @click="$emit(\'disponibilizar\')">Disp</button></div>'},
};

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

    it("cobre fluxos de cadastro", async () => {
        const wrapper = mount(CadastroView, {
            global: { plugins: [createTestingPinia({stubActions: true})], stubs },
            props: { codProcesso: "1", sigla: "TESTE" }
        });
        await flushPromises();
        const vm = wrapper.vm as any;
        vm.codigoSubprocesso = 123;

        // 1. Disponibilizar
        await vm.confirmarDisponibilizacao();
        expect(useFluxoSubprocessoModule.useFluxoSubprocesso().disponibilizarCadastro).toHaveBeenCalled();

        // 2. Importar
        await vm.handleImportAtividades({ atividadesAtualizadas: [], aviso: false });
        expect(vm.mostrarModalImportar).toBe(false);

        // 3. Analise
        await vm.confirmarValidacaoAnalise();
        expect(useFluxoSubprocessoModule.useFluxoSubprocesso().aceitarCadastro).toHaveBeenCalled();

        // 4. Devolução
        vm.observacaoDevolucao = "Justificativa";
        await vm.confirmarDevolucaoAnalise();
        expect(useFluxoSubprocessoModule.useFluxoSubprocesso().devolverCadastro).toHaveBeenCalled();
    });
});
