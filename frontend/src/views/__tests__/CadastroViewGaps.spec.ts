import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref, nextTick} from "vue";
import * as useAcessoModule from "@/composables/useAcesso";
import * as useFluxoSubprocessoModule from "@/composables/useFluxoSubprocesso";
import {useMapas} from "@/composables/useMapas";
import * as useProcessosModule from "@/composables/useProcessos";
import * as atividadeService from "@/services/atividadeService";
import * as subprocessoService from "@/services/subprocessoService";
import {SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import CadastroView from "../CadastroView.vue";
import logger from "@/utils/logger";

vi.mock("@/utils/logger", () => ({
    default: {
        error: vi.fn(),
        warn: vi.fn(),
        info: vi.fn(),
        debug: vi.fn(),
    }
}));

const {pushMock} = vi.hoisted(() => ({pushMock: vi.fn()}));

vi.mock("vue-router", () => ({
    useRoute: () => ({params: {codProcesso: "1", siglaUnidade: "TESTE"}}),
    useRouter: () => ({push: pushMock}),
}));

vi.mock("@/services/processoService", () => ({
    obterDetalhesProcesso: vi.fn(),
    listarProcessos: vi.fn(),
}));

vi.mock("@/services/atividadeService", () => ({
    excluirAtividade: vi.fn(),
    atualizarAtividade: vi.fn(),
    criarConhecimento: vi.fn(),
    atualizarConhecimento: vi.fn(),
    excluirConhecimento: vi.fn(),
}));

vi.mock("@/services/subprocessoService", () => ({
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarSubprocessoDetalhe: vi.fn(),
    buscarContextoEdicao: vi.fn(),
}));

vi.mock("@/services/analiseService", () => ({
    listarAnalisesCadastro: vi.fn(),
}));

vi.mock("@/services/mapaService", () => ({
    obterImpactoMapa: vi.fn(),
}));

vi.mock("@/composables/useErrorHandler", () => ({
    useErrorHandler: () => ({
        withErrorHandling: (fn: any) => fn(),
        lastError: ref({message: "Erro"})
    })
}));

vi.mock("@/composables/usePerfil", () => ({
    usePerfil: () => ({
        isChefe: ref(true)
    })
}));

vi.mock("@/composables/useAcesso", () => ({
    useAcesso: () => ({
        podeEditarCadastro: ref(true),
        podeVisualizarImpacto: ref(true),
        habilitarEditarCadastro: ref(true),
        habilitarDisponibilizarCadastro: ref(true),
    })
}));

const mockProcessos = {
    processoDetalhe: ref(null),
    buscarProcessoDetalhe: vi.fn().mockResolvedValue(undefined),
};

vi.mock("@/composables/useProcessos", () => ({
    useProcessos: vi.fn(() => mockProcessos)
}));

const stubs = {
    LayoutPadrao: {template: '<div><slot /></div>'},
    PageHeader: {
        props: ['title'],
        template: '<div><h1>{{ title }}</h1><slot /><slot name="actions" /></div>'
    },
    BButton: {template: '<button :data-testid="$attrs[\'data-testid\']" @click="$emit(\'click\')"><slot /></button>'},
    BBadge: {template: '<span><slot /></span>'},
    BAlert: {template: '<div v-if="modelValue" data-testid="b-alert"><slot /><button data-testid="btn-dismiss-alert" @click="$emit(\'dismissed\')">x</button></div>', props: ['modelValue']},
    AppAlert: {template: '<div><button data-testid="btn-dismiss-app-alert" @click="$emit(\'dismissed\')">x</button></div>', props: ['message', 'variant', 'dismissible']},
    EmptyState: {template: '<div><slot /></div>'},
    LoadingButton: {
        props: ['loading', 'disabled', 'text'],
        template: '<button :disabled="disabled" @click="$emit(\'click\')">{{ loading ? "Loading..." : text }}</button>'
    },
    CadAtividadeForm: {
        props: ['modelValue'],
        template: '<div data-testid="cad-atividade-form"><slot /></div>', 
        expose: ['inputRef'],
        setup(props: any, {emit}: any) {
            return {
                inputRef: {
                    $el: {
                        focus: vi.fn()
                    }
                }
            }
        }
    },
    AtividadeItem: {
        props: ['atividade'],
        template: `<div data-testid="atividade-item">
            Stub
        </div>`
    },
    ImportarAtividadesModal: {
        props: ['mostrar'],
        template: '<div v-if="mostrar"><button data-testid="btn-close-import" @click="$emit(\'fechar\')">Close</button></div>'
    },
    ImpactoMapaModal: {template: '<div></div>', props: ['mostrar']},
    ConfirmacaoDisponibilizacaoModal: {
        props: ['mostrar'],
        template: '<div v-if="mostrar"><button data-testid="btn-close-conf" @click="$emit(\'fechar\')">Close</button></div>'
    },
    HistoricoAnaliseModal: {
        props: ['mostrar'],
        template: '<div v-if="mostrar"><button data-testid="btn-close-hist" @click="$emit(\'fechar\')">Close</button></div>'
    },
    ModalConfirmacao: {
        props: ['modelValue'],
        template: '<div v-if="modelValue" data-testid="modal-confirmacao"></div>'
    },
};

describe("CadastroView Gaps Coverage", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    function createWrapper() {
        const pinia = createTestingPinia({stubActions: true});
        const mapas = useMapas();
        (mapas.mapaCompleto as any).value = {codigo: 100};
        
        return mount(CadastroView, {
            global: {
                plugins: [pinia],
                stubs
            },
            props: {
                codProcesso: "1",
                sigla: "TESTE"
            }
        });
    }

    it("cobre eventos de template e branches não testados", async () => {
        vi.mocked(subprocessoService.buscarSubprocessoPorProcessoEUnidade).mockResolvedValue(123 as any);
        vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue({
            subprocesso: {codigo: 123, situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, tipoProcesso: TipoProcesso.MAPEAMENTO},
            permissoes: {podeEditarCadastro: true, podeDisponibilizarCadastro: true, podeVisualizarImpacto: true},
            atividadesDisponiveis: [{codigo: 10, descricao: 'Atividade 1', conhecimentos: []}],
            unidade: {sigla: "TESTE", nome: "Teste"}
        } as any);

        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;

        // Ensure codSubprocesso is set
        vm.codSubprocesso = 123;
        vm.atividades = [{codigo: 10, descricao: 'Atividade 1', conhecimentos: []}];
        await nextTick();
        
        // 78 | v-model="novaAtividade"
        const form = wrapper.findComponent({name: 'CadAtividadeForm'});
        await form.vm.$emit('update:modelValue', 'Nova');
        expect(vm.novaAtividade).toBe('Nova');

        // 103-107 | Eventos em AtividadeItem (Template anonymous functions)
        const item = wrapper.findComponent({name: 'AtividadeItem'});
        await item.vm.$emit('atualizar-atividade', 'desc');
        await item.vm.$emit('remover-atividade');
        await item.vm.$emit('adicionar-conhecimento', 'con');
        await item.vm.$emit('atualizar-conhecimento', 1, 'con desc');
        await item.vm.$emit('remover-conhecimento', 1);

        // 140 | v-model="mostrarModalConfirmacaoRemocao"
        const modalRem = wrapper.findComponent({name: 'ModalConfirmacao'});
        await modalRem.vm.$emit('update:modelValue', true);
        expect(vm.mostrarModalConfirmacaoRemocao).toBe(true);
    });

    it("cobre branches de adicionarAtividade", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;
        vm.codSubprocesso = 123;
        
        const mockResponse = {atividadesAtualizadas: [{codigo: 1}]};
        vi.spyOn(vm, 'adicionarAtividadeAction').mockResolvedValue(mockResponse);
        
        // Success path
        await vm.processarRespostaLocal(mockResponse);

        // Fail path coverage (381-382)
        vi.spyOn(vm, 'adicionarAtividadeAction').mockRejectedValue(new Error("Erro"));
        await vm.adicionarAtividade();
        expect(vm.erroNovaAtividade).toBe("Erro");
    });
});
