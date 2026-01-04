import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import CadAtividades from "@/views/CadAtividades.vue";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useAtividadesStore} from "@/stores/atividades";
import * as subprocessoService from "@/services/subprocessoService";
import * as atividadeService from "@/services/atividadeService";
import * as cadastroService from "@/services/cadastroService";
import {SituacaoSubprocesso, TipoProcesso,} from "@/types/tipos";
import {createTestingPinia} from "@pinia/testing";

// Mocks
const mocks = vi.hoisted(() => ({
    push: vi.fn(),
    mockRoute: { query: {} as Record<string, string> }
}));

vi.mock("vue-router", () => ({
    useRouter: () => ({
        push: mocks.push,
        back: vi.fn(),
    }),
    useRoute: () => mocks.mockRoute,
    createRouter: vi.fn(() => ({
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
        push: mocks.push,
        resolve: vi.fn(),
        currentRoute: { value: mocks.mockRoute }
    })),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

vi.mock("@/services/subprocessoService", () => ({
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarContextoEdicao: vi.fn(),
    validarCadastro: vi.fn(),
    listarAtividades: vi.fn(),
}));

vi.mock("@/services/atividadeService", () => ({
    adicionarAtividade: vi.fn(),
    removerAtividade: vi.fn(),
    adicionarConhecimento: vi.fn(),
    removerConhecimento: vi.fn(),
    atualizarAtividade: vi.fn(),
    atualizarConhecimento: vi.fn(),
    buscarAtividadesParaSubprocesso: vi.fn(),
    criarAtividade: vi.fn(),
    criarConhecimento: vi.fn(),
    excluirAtividade: vi.fn(),
    excluirConhecimento: vi.fn(),
}));


vi.mock("@/services/cadastroService", () => ({
    disponibilizarCadastro: vi.fn(),
}));

vi.mock("@/services/processoService");
vi.mock("@/services/analiseService");

// Stubs
const ConfirmacaoDisponibilizacaoModalStub = {
    template:
        '<div data-testid="mdl-confirmacao-disp" v-if="mostrar"><button data-testid="btn-confirmar-disponibilizacao" @click="$emit(\'confirmar\')"></button></div>',
    props: ["mostrar", "isRevisao"],
    emits: ["confirmar", "fechar"],
};

const HistoricoAnaliseModalStub = {
    template: '<div v-if="mostrar"><slot /></div>',
    props: ["mostrar", "codSubprocesso"],
    emits: ["fechar"],
};

const AtividadeItemStub = {
    template: `
    <div class="atividade-item">
      <button data-testid="btn-remover-atividade" @click="$emit('remover-atividade')">Remover Atv</button>
      <button data-testid="btn-editar-atividade" @click="$emit('editar-atividade')">Editar Atv</button>
      <input data-testid="inp-editar-atividade" v-if="editandoAtividade" :value="atividade.descricao" @input="$emit('update:atividade', {...atividade, descricao: $event.target.value})" />
      <button data-testid="btn-salvar-edicao-atividade" v-if="editandoAtividade" @click="$emit('atualizar-atividade', 'Atividade Editada')">Salvar Atv</button>

      <div v-for="c in atividade.conhecimentos" :key="c.codigo">
         <button data-testid="btn-remover-conhecimento" @click="$emit('remover-conhecimento', c.codigo)">Remover Conh</button>
         <button data-testid="btn-editar-conhecimento" @click="$emit('editar-conhecimento', c.codigo)">Editar Conh</button>
         <input data-testid="inp-editar-conhecimento" v-if="editandoConhecimento === c.codigo" :value="c.descricao" @input="$emit('update:conhecimento', {...c, descricao: $event.target.value})" />
         <button data-testid="btn-salvar-edicao-conhecimento" v-if="editandoConhecimento === c.codigo" @click="$emit('atualizar-conhecimento', c.codigo, 'Conhecimento Editado')">Salvar Conh</button>
      </div>
    </div>
  `,
    props: ["atividade", "podeEditar", "editandoAtividade", "editandoConhecimento"],
    emits: ["remover-atividade", "editar-atividade", "atualizar-atividade", "adicionar-conhecimento", "remover-conhecimento", "editar-conhecimento", "atualizar-conhecimento", "update:atividade", "update:conhecimento"]
};

// Custom helper to override common options but ensure pinia stubActions is FALSE
function mountCadAtividades(options: any = {}) {
    return mount(CadAtividades, {
        global: {
            plugins: [
                createTestingPinia({
                    createSpy: vi.fn,
                    initialState: options.initialState || {},
                    stubActions: false // Real actions, using mocked services
                }),
            ],
            stubs: {
                ConfirmacaoDisponibilizacaoModal: ConfirmacaoDisponibilizacaoModalStub,
                HistoricoAnaliseModal: HistoricoAnaliseModalStub,
                AtividadeItem: AtividadeItemStub,
                ImportarAtividadesModal: true,
                ImpactoMapaModal: true,
                ModalConfirmacao: {
                    name: "ModalConfirmacao", // Ensure name matches
                    template: '<div v-if="modelValue"><button @click="$emit(\'confirmar\')">Confirmar</button></div>',
                    props: ['modelValue'],
                    emits: ['confirmar', 'update:modelValue']
                },
                BContainer: { template: '<div><slot /></div>' },
                BButton: { template: '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>', props: ['disabled'] },
                // FIX: Use plain form with submit event to play nice with @submit.prevent in parent
                BForm: { template: '<form @submit="$emit(\'submit\', $event)"><slot /></form>' },
                BFormInput: { template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />', props: ['modelValue'], emits: ['update:modelValue'] },
                BCol: { template: '<div><slot /></div>' },
                BDropdown: { template: '<div><slot /></div>' },
                BDropdownItem: { template: '<div><slot /></div>' },
                BAlert: { template: '<div><slot /></div>' },
            },
        },
        props: {
            codProcesso: 1,
            sigla: "TESTE",
        },
    });
}

describe("CadAtividades.vue", () => {
    let subprocessosStore: any;
    let atividadesStore: any;

    const mockAtividades = [
        {
            codigo: 1,
            descricao: "Atividade 1",
            conhecimentos: [{codigo: 101, descricao: "Conhecimento 1"}],
        },
    ];

    const createWrapper = (isRevisao = false) => {
        // Mock route
        mocks.mockRoute.query = {};

        // Mock Services BEFORE mounting
        vi.mocked(subprocessoService.buscarSubprocessoPorProcessoEUnidade).mockResolvedValue({ codigo: 123 } as any);
        vi.mocked(subprocessoService.listarAtividades).mockResolvedValue([...mockAtividades] as any);

        // Contexto lookup
        vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue({
            subprocesso: {
                codigo: 123,
                situacao: isRevisao
                            ? SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
                            : SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                permissoes: {
                     podeEditarMapa: true,
                     podeDisponibilizarCadastro: true,
                }
            },
            mapa: { codigo: 456, competencias: [] },
            atividadesDisponiveis: [...mockAtividades],
            unidade: { codigo: 1, sigla: "TESTE", nome: "Teste" }
        } as any);

        const wrapper = mountCadAtividades({
            initialState: {
                perfil: {
                    perfilSelecionado: "CHEFE",
                    unidadeSelecionada: 1,
                    perfisUnidades: [{ perfil: "CHEFE", unidade: { codigo: 1 } }]
                },
                processos: {
                    processoDetalhe: {
                        codigo: 1,
                        tipo: isRevisao ? TipoProcesso.REVISAO : TipoProcesso.MAPEAMENTO,
                        unidades: [{ codUnidade: 1, codSubprocesso: 123 }]
                    }
                },
                mapas: {
                    mapaCompleto: { codigo: 456, competencias: [] }
                }
            }
        });

        subprocessosStore = useSubprocessosStore();
        atividadesStore = useAtividadesStore();

        return { wrapper, subprocessosStore, atividadesStore };
    };

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("deve renderizar a lista de atividades", async () => {
        const { wrapper } = createWrapper();
        await flushPromises();

        expect(wrapper.findAllComponents(AtividadeItemStub)).toHaveLength(1);
    });

    it("deve adicionar nova atividade", async () => {
        // Mock service for this action
        vi.mocked(atividadeService.criarAtividade).mockResolvedValue({ subprocesso: {} } as any);

        const { wrapper } = createWrapper();
        await flushPromises();

        const input = wrapper.find('input[placeholder="Nova atividade"]');
        await input.setValue("Nova Atividade");
        await wrapper.find('form').trigger("submit");
        await flushPromises();

        expect(atividadeService.criarAtividade).toHaveBeenCalledWith(
            {descricao: "Nova Atividade"},
            456
        );
    });

    it("deve remover atividade após confirmação", async () => {
        // Service method is excluirAtividade
        vi.mocked(atividadeService.excluirAtividade).mockResolvedValue({ subprocesso: {} } as any);

        const { wrapper } = createWrapper();
        await flushPromises();

        const item = wrapper.findComponent(AtividadeItemStub);
        await item.vm.$emit('remover-atividade');
        await flushPromises();

        const modal = wrapper.findComponent({ name: 'ModalConfirmacao' });
        await modal.vm.$emit('confirmar');
        await flushPromises();

        expect(atividadeService.excluirAtividade).toHaveBeenCalledWith(1);
    });

    it("deve remover um conhecimento", async () => {
        vi.mocked(atividadeService.excluirConhecimento).mockResolvedValue({ subprocesso: {} } as any);

        const { wrapper } = createWrapper();
        await flushPromises();

        const item = wrapper.findComponent(AtividadeItemStub);
        await item.vm.$emit('remover-conhecimento', 101);
        await flushPromises();

        const modal = wrapper.findComponent({ name: 'ModalConfirmacao' });
        await modal.vm.$emit('confirmar');
        await flushPromises();

        expect(atividadeService.excluirConhecimento).toHaveBeenCalledWith(1, 101);
    });

    it("deve disponibilizar o cadastro", async () => {
        vi.mocked(subprocessoService.validarCadastro).mockResolvedValue({ valido: true, erros: [] });
        vi.mocked(cadastroService.disponibilizarCadastro).mockResolvedValue();

        const { wrapper } = createWrapper();
        await flushPromises();

        await wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]').trigger("click");
        await flushPromises();

        const modal = wrapper.findComponent(ConfirmacaoDisponibilizacaoModalStub);
        expect(modal.props('mostrar')).toBe(true);

        await modal.vm.$emit('confirmar');
        await flushPromises();

        expect(cadastroService.disponibilizarCadastro).toHaveBeenCalledWith(123);
        expect(mocks.push).toHaveBeenCalledWith("/painel");
    });

    it("deve permitir edição inline de atividade", async () => {
        vi.mocked(atividadeService.atualizarAtividade).mockResolvedValue({ subprocesso: {} } as any);

        const { wrapper } = createWrapper();
        await flushPromises();

        const item = wrapper.findComponent(AtividadeItemStub);
        await item.vm.$emit('atualizar-atividade', "Atividade Editada");
        await flushPromises();

        expect(atividadeService.atualizarAtividade).toHaveBeenCalledWith(
            1,
            expect.objectContaining({descricao: "Atividade Editada"}),
        );
    });

    it("deve permitir edição inline de conhecimento", async () => {
        vi.mocked(atividadeService.atualizarConhecimento).mockResolvedValue({ subprocesso: {} } as any);

        const { wrapper } = createWrapper();
        await flushPromises();

        const item = wrapper.findComponent(AtividadeItemStub);
        await item.vm.$emit('atualizar-conhecimento', 101, "Conhecimento Editado");
        await flushPromises();

        expect(atividadeService.atualizarConhecimento).toHaveBeenCalledWith(
            1,
            101,
            expect.objectContaining({descricao: "Conhecimento Editado"}),
        );
    });

    it("deve mostrar erros de validação ao tentar disponibilizar se cadastro inválido", async () => {
        vi.mocked(subprocessoService.validarCadastro).mockResolvedValue({
            valido: false,
            erros: [
                {tipo: 'ATIVIDADE_SEM_CONHECIMENTO', mensagem: 'Atividade sem conhecimento', atividadeCodigo: 1}
            ]
        });

        const { wrapper } = createWrapper();
        await flushPromises();

        await wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]').trigger("click");
        await flushPromises();

        expect(wrapper.findComponent(ConfirmacaoDisponibilizacaoModalStub).props('mostrar')).toBe(false);
        expect((wrapper.vm as any).errosValidacao).toHaveLength(1);
    });
});
