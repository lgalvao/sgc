import {createTestingPinia} from "@pinia/testing";
import {flushPromises, mount} from "@vue/test-utils";
import {beforeEach, describe, expect, it, vi} from "vitest";
import {nextTick, ref} from "vue";
import type {ContextoCadastroAtividadesSubprocesso} from "@/types/tipos";
import {SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import CadastroView from "../CadastroView.vue";
import * as useAcessoModule from "@/composables/useAcesso";
import * as useFluxoSubprocessoModule from "@/composables/useFluxoSubprocesso";
import logger from "@/utils/logger";

vi.mock("@/utils/logger", () => ({
    default: {
        error: vi.fn(),
        warn: vi.fn(),
        info: vi.fn(),
        debug: vi.fn(),
    }
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
    atualizarStatusLocal: vi.fn((status: any) => {
        if (estadoContextoCadastro.value) {
            estadoContextoCadastro.value = {
                ...estadoContextoCadastro.value,
                detalhes: {
                    ...estadoContextoCadastro.value.detalhes,
                    ...status
                }
            };
        }
    }),
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

const {pushMock} = vi.hoisted(() => ({pushMock: vi.fn()}));

vi.mock("vue-router", () => ({
    useRoute: () => ({params: {codProcesso: "1", siglaUnidade: "TESTE"}, query: {}}),
    useRouter: () => ({push: pushMock}),
}));

vi.mock("@/services/analiseService", () => ({
    listarAnalisesCadastro: vi.fn().mockResolvedValue([]),
}));

vi.mock("@/services/atividadeService", () => ({
    excluirAtividade: vi.fn(),
    atualizarAtividade: vi.fn(),
    criarConhecimento: vi.fn(),
    atualizarConhecimento: vi.fn(),
    excluirConhecimento: vi.fn(),
    adicionarAtividade: vi.fn(),
}));

const stubs = {
    LayoutPadrao: {template: '<div><slot /></div>'},
    PageHeader: {
        props: ['title'],
        template: '<div><h1>{{ title }}</h1><slot /><slot name="actions" /></div>'
    },
    BButton: {template: '<button :data-testid="$attrs[\'data-testid\']" @click="$emit(\'click\')"><slot /></button>'},
    BFormCheckbox: {
        props: ['modelValue'],
        template: '<div><slot /><input type="checkbox" :data-testid="$attrs[\'data-testid\']" :checked="modelValue" @change="$emit(' + "'update:modelValue'" + ', $event.target.checked)" /></div>'
    },
    BSpinner: {template: '<div class="spinner"></div>'},
    BAlert: {template: '<div><slot /></div>', props: ['modelValue']},
    EmptyState: {template: '<div><slot /></div>'},
    LoadingButton: {
        props: ['loading', 'disabled'],
        template: '<button :disabled="disabled" @click="$emit(\'click\')">{{ loading ? "Loading..." : "Action" }}</button>'
    },
    CadAtividadeForm: {
        template: '<div class="cad-atividade-form"></div>',
        expose: ['inputRef'],
        data() {
            return {
                inputRef: {
                    $el: {
                        focus: vi.fn()
                    }
                }
            }
        }
    },
    AtividadeItem: {template: '<div></div>', props: ['atividade']},
    ImportarAtividadesModal: {template: '<div v-if="mostrar" class="modal-importar"><button @click="$emit(\'fechar\')">Fechar</button></div>', props: ['mostrar']},
    ImpactoMapaModal: {template: '<div></div>', props: ['mostrar']},
    ConfirmacaoDisponibilizacaoModal: {template: '<div v-if="mostrar" class="modal-confirmar"><button @click="$emit(\'fechar\')">Fechar</button></div>', props: ['mostrar']},
    HistoricoAnaliseModal: {template: '<div v-if="mostrar" class="modal-historico"><button @click="$emit(\'fechar\')">Fechar</button></div>', props: ['mostrar']},
    ModalConfirmacao: {template: '<div v-if="modelValue" class="modal-confirmacao"><button @click="$emit(\'confirmar\')">Confirmar</button></div>', props: ['modelValue', 'loading']},
};

function createWrapper(options = {}) {
    const pinia = createTestingPinia({stubActions: true});
    
    return mount(CadastroView, {
        global: {
            plugins: [pinia],
            stubs
        },
        props: {
            codProcesso: "1",
            sigla: "TESTE"
        },
        ...options
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
        } as any);

        subprocessosMock.subprocessoDetalhe = {
            codigo: 123,
            situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
            tipoProcesso: TipoProcesso.MAPEAMENTO,
            permissoes: { podeEditarCadastro: true }
        };

        const store = subprocessosMock;
        (store.buscarContextoCadastroAtividadesPorProcessoEUnidade as any).mockResolvedValue({
            detalhes: {
                codigo: 123,
                situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                tipoProcesso: TipoProcesso.MAPEAMENTO,
                permissoes: { podeEditarCadastro: true }
            },
            mapa: { codigo: 100 },
            atividadesDisponiveis: [],
            unidade: { sigla: 'TESTE' }
        });
        
        vi.mocked(useFluxoSubprocessoModule.useFluxoSubprocesso).mockReturnValue({
            iniciarRevisaoCadastro: vi.fn().mockResolvedValue(true),
            cancelarInicioRevisaoCadastro: vi.fn().mockResolvedValue(true),
            validarCadastro: vi.fn().mockResolvedValue({ valido: true }),
            disponibilizarCadastro: vi.fn().mockResolvedValue(true),
            disponibilizarRevisaoCadastro: vi.fn().mockResolvedValue(true),
        } as any);
    });

    it("cobre fechamento de modais via evento @fechar", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;

        // ImportarAtividadesModal @fechar
        vm.mostrarModalImportar = true;
        await nextTick();
        const modalImportar = wrapper.find('.modal-importar');
        await modalImportar.find('button').trigger('click');
        expect(vm.mostrarModalImportar).toBe(false);

        // ConfirmacaoDisponibilizacaoModal @fechar
        vm.mostrarModalConfirmacao = true;
        await nextTick();
        const modalConfirmar = wrapper.find('.modal-confirmar');
        await modalConfirmar.find('button').trigger('click');
        expect(vm.mostrarModalConfirmacao).toBe(false);

        // HistoricoAnaliseModal @fechar
        vm.mostrarModalHistorico = true;
        await nextTick();
        const modalHistorico = wrapper.find('.modal-historico');
        await modalHistorico.find('button').trigger('click');
        expect(vm.mostrarModalHistorico).toBe(false);
    });

    it("cobre sort em calcularAssinaturaCadastro", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;

        // Precisamos de pelo menos duas atividades com descrições diferentes para entrar no sort
        vm.atividades = [
            { codigo: 1, descricao: "B", conhecimentos: [] },
            { codigo: 2, descricao: "A", conhecimentos: [] }
        ];
        
        // A assinatura deve ser a mesma independentemente da ordem inicial se o sort funcionar
        const assinatura1 = vm.calcularAssinaturaCadastro(vm.atividades);
        
        vm.atividades = [
            { codigo: 2, descricao: "A", conhecimentos: [] },
            { codigo: 1, descricao: "B", conhecimentos: [] }
        ];
        const assinatura2 = vm.calcularAssinaturaCadastro(vm.atividades);
        
        expect(assinatura1).toBe(assinatura2);
    });

    it("cobre iniciarRevisaoSeNecessario falha e guards", async () => {
        const store = subprocessosMock;
        store.subprocessoDetalhe = {
            codigo: 123,
            situacao: SituacaoSubprocesso.NAO_INICIADO,
            tipoProcesso: TipoProcesso.REVISAO,
            permissoes: { podeEditarCadastro: true }
        };

        (store.buscarContextoCadastroAtividadesPorProcessoEUnidade as any).mockResolvedValue({
            detalhes: {
                codigo: 123,
                situacao: SituacaoSubprocesso.NAO_INICIADO,
                tipoProcesso: TipoProcesso.REVISAO,
                permissoes: { podeEditarCadastro: true }
            },
            mapa: { codigo: 100 },
            atividadesDisponiveis: [],
            unidade: { sigla: 'TESTE' }
        });

        const fluxo = {
            iniciarRevisaoCadastro: vi.fn().mockResolvedValue(false),
        };
        vi.mocked(useFluxoSubprocessoModule.useFluxoSubprocesso).mockReturnValue(fluxo as any);

        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;

        // Trigger watcher on disponibilizacaoSemMudancas to call iniciarRevisaoSeNecessario
        vm.disponibilizacaoSemMudancas = true;
        await flushPromises();

        expect(fluxo.iniciarRevisaoCadastro).toHaveBeenCalledWith(123);
        expect(logger.error).toHaveBeenCalledWith('Falha ao iniciar revisão do cadastro');
        
        // Guard check: call again while loading or already started
        vm.loadingInicioRevisao = true;
        await vm.iniciarRevisaoSeNecessario();
        expect(fluxo.iniciarRevisaoCadastro).toHaveBeenCalledTimes(1); // Not called again
    });

    it("cobre cancelarInicioRevisaoSeNecessario falha e guards", async () => {
        const store = subprocessosMock;
        store.subprocessoDetalhe = {
            codigo: 123,
            situacao: SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO,
            tipoProcesso: TipoProcesso.REVISAO,
            permissoes: { podeEditarCadastro: true }
        };

        (store.buscarContextoCadastroAtividadesPorProcessoEUnidade as any).mockResolvedValue({
            detalhes: {
                codigo: 123,
                situacao: SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO,
                tipoProcesso: TipoProcesso.REVISAO,
                permissoes: { podeEditarCadastro: true }
            },
            mapa: { codigo: 100 },
            atividadesDisponiveis: [],
            unidade: { sigla: 'TESTE' }
        });

        const fluxo = {
            cancelarInicioRevisaoCadastro: vi.fn().mockResolvedValue(false),
            iniciarRevisaoCadastro: vi.fn().mockResolvedValue(true)
        };
        vi.mocked(useFluxoSubprocessoModule.useFluxoSubprocesso).mockReturnValue(fluxo as any);

        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;
        vm.atividadesSnapshotInicial = vm.calcularAssinaturaCadastro(vm.atividades); // matches current

        // Trigger watcher on disponibilizacaoSemMudancas = false to call cancelarInicioRevisaoSeNecessario
        vm.disponibilizacaoSemMudancas = true; // start as true
        await nextTick();
        await flushPromises();
        vm.disponibilizacaoSemMudancas = false;
        await nextTick();
        await flushPromises();

        expect(fluxo.cancelarInicioRevisaoCadastro).toHaveBeenCalledWith(123);
        expect(logger.error).toHaveBeenCalledWith('Falha ao cancelar início da revisão do cadastro');

        // Guard check: houveAlteracaoCadastro
        vm.atividades = [{codigo: 1, descricao: 'changed'}];
        expect(vm.houveAlteracaoCadastro).toBe(true);
        await vm.cancelarInicioRevisaoSeNecessario();
        expect(fluxo.cancelarInicioRevisaoCadastro).toHaveBeenCalledTimes(1); // Not called again
    });

    it("cobre guards em removerAtividade, adicionarConhecimento, etc", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;

        vm.codigoSubprocesso = null;
        
        vm.removerAtividade(0);
        expect(vm.mostrarModalConfirmacaoRemocao).toBe(false);

        await vm.adicionarConhecimento(0, "desc");
        // No call to activity service expected

        vm.removerConhecimento(0, 1);
        expect(vm.mostrarModalConfirmacaoRemocao).toBe(false);

        await vm.salvarEdicaoConhecimento(1, 1, "desc");
        // No call to activity service expected
    });

    it("cobre executarAtualizacaoCadastro success return", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;

        const acao = vi.fn().mockResolvedValue({
            subprocesso: { codigo: 123 },
            permissoes: {},
            atividadesAtualizadas: []
        });

        const res = await vm.executarAtualizacaoCadastro(acao, "erro");
        expect(res).toBe(true);
    });

    it("cobre branches de watch(houveAlteracaoCadastro) e watch(disponibilizacaoSemMudancas)", async () => {
        const store = subprocessosMock;
        store.subprocessoDetalhe = {
            codigo: 123,
            situacao: SituacaoSubprocesso.NAO_INICIADO,
            tipoProcesso: TipoProcesso.REVISAO,
            permissoes: { podeEditarCadastro: true }
        };

        (store.buscarContextoCadastroAtividadesPorProcessoEUnidade as any).mockResolvedValue({
            detalhes: {
                codigo: 123,
                situacao: SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO,
                tipoProcesso: TipoProcesso.REVISAO,
                permissoes: { podeEditarCadastro: true }
            },
            mapa: { codigo: 100 },
            atividadesDisponiveis: [],
            unidade: { sigla: 'TESTE' }
        });

        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;
        vm.atividadesSnapshotInicial = 'orig';

        // Case: alterou && disponibilizacaoSemMudancas.value
        vm.disponibilizacaoSemMudancas = true;
        await flushPromises();
        
        vm.atividades = [{codigo: 1, descricao: 'new'}];
        await flushPromises();
        expect(vm.disponibilizacaoSemMudancas).toBe(false);

        // Case: watch(disponibilizacaoSemMudancas) marked but !precisaIniciarRevisao
        vm.disponibilizacaoSemMudancas = false;
        await flushPromises();
        
        // Change situation so !precisaIniciarRevisao
        vm.subprocesso.situacao = SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO;
        vm.disponibilizacaoSemMudancas = true;
        await flushPromises();
        // Should return early
    });
});
