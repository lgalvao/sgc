import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import CadAtividades from "@/views/CadAtividades.vue";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useAtividadesStore} from "@/stores/atividades";
import {useMapasStore} from "@/stores/mapas";
import {useAnalisesStore} from "@/stores/analises";
import * as subprocessoService from "@/services/subprocessoService";
import {SituacaoSubprocesso, TipoProcesso,} from "@/types/tipos";
import {createTestingPinia} from "@pinia/testing";
import {nextTick} from "vue";

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
}));

vi.mock("@/services/processoService");

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

describe("CadAtividades.vue", () => {
    let subprocessosStore: any;
    let atividadesStore: any;
    let analisesStore: any;
    let mapasStore: any;

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

        // Mock Services (kept for safety, though stubActions: true bypasses them mostly)
        vi.mocked(subprocessoService.buscarSubprocessoPorProcessoEUnidade).mockResolvedValue({ codigo: 123 } as any);

        // Setup Pinia with Stubs
        const pinia = createTestingPinia({
            createSpy: vi.fn,
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
                    mapaCompleto: { codigo: 456, competencias: [] },
                    impactoMapa: null
                },
                subprocessos: {
                    subprocessoDetalhe: {
                        codigo: 123,
                        situacao: isRevisao
                            ? SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
                            : SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                         permissoes: {
                             podeEditarMapa: true,
                             podeDisponibilizarCadastro: true,
                             podeVisualizarImpacto: true
                        },
                        tipoProcesso: isRevisao ? TipoProcesso.REVISAO : TipoProcesso.MAPEAMENTO
                    }
                },
                atividades: {
                    atividadesPorSubprocesso: new Map([[123, mockAtividades]])
                },
                unidades: {
                    unidade: { codigo: 1, sigla: "TESTE", nome: "Teste" }
                }
            },
            stubActions: true
        });

        // PRE-CONFIGURE STORE SPIES
        subprocessosStore = useSubprocessosStore(pinia);
        subprocessosStore.buscarSubprocessoPorProcessoEUnidade.mockResolvedValue(123);
        subprocessosStore.buscarContextoEdicao.mockResolvedValue(undefined);
        subprocessosStore.disponibilizarCadastro.mockResolvedValue(true);
        subprocessosStore.disponibilizarRevisaoCadastro.mockResolvedValue(true);

        atividadesStore = useAtividadesStore(pinia);
        // Actions
        atividadesStore.adicionarAtividade.mockResolvedValue({});
        atividadesStore.removerAtividade.mockResolvedValue({});
        atividadesStore.removerConhecimento.mockResolvedValue({});
        atividadesStore.atualizarAtividade.mockResolvedValue({});
        atividadesStore.atualizarConhecimento.mockResolvedValue({});
        atividadesStore.adicionarConhecimento.mockResolvedValue({});
        atividadesStore.buscarAtividadesParaSubprocesso.mockResolvedValue({});

        mapasStore = useMapasStore(pinia);
        mapasStore.buscarImpactoMapa.mockResolvedValue({});

        analisesStore = useAnalisesStore(pinia);
        analisesStore.buscarAnalisesCadastro.mockResolvedValue([]);

        const wrapper = mount(CadAtividades, {
            global: {
                plugins: [pinia],
                stubs: {
                    ConfirmacaoDisponibilizacaoModal: ConfirmacaoDisponibilizacaoModalStub,
                    HistoricoAnaliseModal: HistoricoAnaliseModalStub,
                    AtividadeItem: AtividadeItemStub,
                    ImportarAtividadesModal: {
                         name: 'ImportarAtividadesModal',
                         template: '<div v-if="mostrar"></div>',
                         props: ['mostrar'],
                         emits: ['importar', 'fechar']
                    },
                    ImpactoMapaModal: true,
                    ModalConfirmacao: {
                        name: "ModalConfirmacao",
                        template: '<div v-if="modelValue"><button @click="$emit(\'confirmar\')">Confirmar</button></div>',
                        props: ['modelValue'],
                        emits: ['confirmar', 'update:modelValue']
                    },
                    BContainer: { template: '<div><slot /></div>' },
                    BButton: { template: '<button :disabled="disabled" @click="$emit(\'click\')"><slot /></button>', props: ['disabled'] },
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
        const { wrapper, atividadesStore } = createWrapper();
        await flushPromises();

        const input = wrapper.find('input[placeholder="Nova atividade"]');
        await input.setValue("Nova Atividade");
        await wrapper.find('form').trigger("submit");
        await flushPromises();

        expect(atividadesStore.adicionarAtividade).toHaveBeenCalledWith(
            123,
            456,
            {descricao: "Nova Atividade"}
        );
    });

    it("não deve adicionar atividade vazia", async () => {
        const { wrapper, atividadesStore } = createWrapper();
        await flushPromises();

        const input = wrapper.find('input[placeholder="Nova atividade"]');
        await input.setValue("   ");
        await wrapper.find('form').trigger("submit");
        await flushPromises();

        expect(atividadesStore.adicionarAtividade).not.toHaveBeenCalled();
    });

    it("deve remover atividade após confirmação", async () => {
        const { wrapper, atividadesStore } = createWrapper();
        await flushPromises();

        const item = wrapper.findComponent(AtividadeItemStub);
        await item.vm.$emit('remover-atividade');
        await flushPromises();

        const modal = wrapper.findComponent({ name: 'ModalConfirmacao' });
        await modal.vm.$emit('confirmar');
        await flushPromises();

        expect(atividadesStore.removerAtividade).toHaveBeenCalledWith(123, 1);
    });

    it("deve remover um conhecimento", async () => {
        const { wrapper, atividadesStore } = createWrapper();
        await flushPromises();

        const item = wrapper.findComponent(AtividadeItemStub);
        await item.vm.$emit('remover-conhecimento', 101);
        await flushPromises();

        const modal = wrapper.findComponent({ name: 'ModalConfirmacao' });
        await modal.vm.$emit('confirmar');
        await flushPromises();

        expect(atividadesStore.removerConhecimento).toHaveBeenCalledWith(123, 1, 101);
    });

    it("deve adicionar conhecimento", async () => {
        const { wrapper, atividadesStore } = createWrapper();
        await flushPromises();

        const item = wrapper.findComponent(AtividadeItemStub);
        await item.vm.$emit('adicionar-conhecimento', 'Novo Conhecimento');
        await flushPromises();

        expect(atividadesStore.adicionarConhecimento).toHaveBeenCalledWith(
            123,
            1,
            expect.objectContaining({descricao: 'Novo Conhecimento'})
        );
    });

    it("não deve adicionar conhecimento vazio", async () => {
        const { wrapper, atividadesStore } = createWrapper();
        await flushPromises();

        const item = wrapper.findComponent(AtividadeItemStub);
        await item.vm.$emit('adicionar-conhecimento', '   ');
        await flushPromises();

        expect(atividadesStore.adicionarConhecimento).not.toHaveBeenCalled();
    });

    it("deve disponibilizar o cadastro", async () => {
        vi.mocked(subprocessoService.validarCadastro).mockResolvedValue({ valido: true, erros: [] });

        const { wrapper, subprocessosStore } = createWrapper();
        await flushPromises();

        await wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]').trigger("click");
        await flushPromises();

        const modal = wrapper.findComponent(ConfirmacaoDisponibilizacaoModalStub);
        expect(modal.props('mostrar')).toBe(true);

        await modal.vm.$emit('confirmar');
        await flushPromises();

        expect(subprocessosStore.disponibilizarCadastro).toHaveBeenCalledWith(123);
        expect(mocks.push).toHaveBeenCalledWith("/painel");
    });

    it("deve disponibilizar revisao de cadastro", async () => {
        vi.mocked(subprocessoService.validarCadastro).mockResolvedValue({ valido: true, erros: [] });

        const { wrapper, subprocessosStore } = createWrapper(true); // isRevisao=true
        await flushPromises();

        await wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]').trigger("click");
        await flushPromises();

        const modal = wrapper.findComponent(ConfirmacaoDisponibilizacaoModalStub);
        await modal.vm.$emit('confirmar');
        await flushPromises();

        expect(subprocessosStore.disponibilizarRevisaoCadastro).toHaveBeenCalledWith(123);
    });

    it("não deve permitir disponibilizar se status incorreto", async () => {
        const { wrapper, subprocessosStore } = createWrapper();
        await flushPromises();

        // Change state
        subprocessosStore.subprocessoDetalhe!.situacao = SituacaoSubprocesso.AGUARDANDO_ANALISE_CADASTRO;

        await wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]').trigger("click");
        await flushPromises();

        const modal = wrapper.findComponent(ConfirmacaoDisponibilizacaoModalStub);
        expect(modal.props('mostrar')).toBe(false);
    });

    it("deve permitir edição inline de atividade", async () => {
        const { wrapper, atividadesStore } = createWrapper();
        await flushPromises();

        const item = wrapper.findComponent(AtividadeItemStub);
        await item.vm.$emit('atualizar-atividade', "Atividade Editada");
        await flushPromises();

        expect(atividadesStore.atualizarAtividade).toHaveBeenCalledWith(
            123,
            1,
            expect.objectContaining({descricao: "Atividade Editada"}),
        );
    });

    it("não deve salvar edição de atividade vazia", async () => {
        const { wrapper, atividadesStore } = createWrapper();
        await flushPromises();

        const item = wrapper.findComponent(AtividadeItemStub);
        await item.vm.$emit('atualizar-atividade', "   ");
        await flushPromises();

        expect(atividadesStore.atualizarAtividade).not.toHaveBeenCalled();
    });

    it("deve permitir edição inline de conhecimento", async () => {
        const { wrapper, atividadesStore } = createWrapper();
        await flushPromises();

        const item = wrapper.findComponent(AtividadeItemStub);
        await item.vm.$emit('atualizar-conhecimento', 101, "Conhecimento Editado");
        await flushPromises();

        expect(atividadesStore.atualizarConhecimento).toHaveBeenCalledWith(
            123,
            1,
            101,
            expect.objectContaining({descricao: "Conhecimento Editado"}),
        );
    });

    it("não deve salvar edição de conhecimento vazio", async () => {
        const { wrapper, atividadesStore } = createWrapper();
        await flushPromises();

        const item = wrapper.findComponent(AtividadeItemStub);
        await item.vm.$emit('atualizar-conhecimento', 101, "   ");
        await flushPromises();

        expect(atividadesStore.atualizarConhecimento).not.toHaveBeenCalled();
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

    it("deve mostrar erro global se validação falhar sem código de atividade", async () => {
        vi.mocked(subprocessoService.validarCadastro).mockResolvedValue({
            valido: false,
            erros: [
                {tipo: 'ERRO_GERAL', mensagem: 'Erro geral no mapa'}
            ]
        });

        const { wrapper } = createWrapper();
        await flushPromises();

        await wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]').trigger("click");
        await flushPromises();

        expect((wrapper.vm as any).erroGlobal).toBe('Erro geral no mapa');
    });

    it("deve exibir e fechar modal de impacto", async () => {
        const { wrapper } = createWrapper();
        await flushPromises();

        // Dropdown actions
        await wrapper.find('[data-testid="btn-mais-acoes"]').trigger("click");
        await wrapper.find('[data-testid="cad-atividades__btn-impactos-mapa"]').trigger("click");

        await flushPromises();
        expect((wrapper.vm as any).mostrarModalImpacto).toBe(true);

        const modal = wrapper.findComponent({name: 'ImpactoMapaModal'}); // Using name if component is stubbed with true or partial
        // Since we stubbed with ImpactoMapaModal: true, it renders <impacto-mapa-modal-stub>
        // We can find it by component definition or name if we knew it.
        // Actually, let's just trigger the close event on the stub if possible or check variable

        (wrapper.vm as any).fecharModalImpacto();
        expect((wrapper.vm as any).mostrarModalImpacto).toBe(false);
    });

    it("deve exibir e fechar modal de historico", async () => {
        const { wrapper } = createWrapper(); // isChefe=true in setup
        await flushPromises();

        await wrapper.find('[data-testid="btn-mais-acoes"]').trigger("click");
        await wrapper.find('[data-testid="btn-cad-atividades-historico"]').trigger("click");

        await flushPromises();
        expect((wrapper.vm as any).mostrarModalHistorico).toBe(true);
    });

    it("deve processar importação de atividades", async () => {
        const { wrapper, atividadesStore } = createWrapper();
        await flushPromises();

        // Open modal
        await wrapper.find('[data-testid="btn-mais-acoes"]').trigger("click");
        await wrapper.find('[data-testid="btn-cad-atividades-importar"]').trigger("click");
        expect((wrapper.vm as any).mostrarModalImportar).toBe(true);

        const modal = wrapper.findComponent({ name: "ImportarAtividadesModal" });
        // The real component emits 'importar'
        await modal.vm.$emit("importar");
        await flushPromises();

        expect(atividadesStore.buscarAtividadesParaSubprocesso).toHaveBeenCalledWith(123);
        expect((wrapper.vm as any).mostrarModalImportar).toBe(false);
    });

    it("deve fazer scroll para erro de validação", async () => {
        const scrollIntoViewMock = vi.fn();
        Element.prototype.scrollIntoView = scrollIntoViewMock;

        const { wrapper } = createWrapper();
        await flushPromises();

        // Force errors
        vi.mocked(subprocessoService.validarCadastro).mockResolvedValue({
            valido: false,
            erros: [{tipo: 'ERRO', mensagem: 'Erro', atividadeCodigo: 1}]
        });

        // Mock setAtividadeRef to ensure the ref is populated
        // The template calls setAtividadeRef automatically.
        // We need to make sure the v-for rendered the activity and the ref function ran.
        // This usually happens automatically in mount.

        await wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]').trigger("click");
        await flushPromises();
        await nextTick();

        expect(scrollIntoViewMock).toHaveBeenCalled();
    });

    it("deve tratar erro ao remover atividade", async () => {
        const { wrapper, atividadesStore } = createWrapper();
        const feedbackStore = (wrapper.vm as any).feedbackStore; // Access store from VM or createTestingPinia

        atividadesStore.removerAtividade.mockRejectedValue(new Error("Erro ao remover"));

        await flushPromises();

        const item = wrapper.findComponent(AtividadeItemStub);
        await item.vm.$emit('remover-atividade');
        await flushPromises();

        const modal = wrapper.findComponent({ name: 'ModalConfirmacao' });
        await modal.vm.$emit('confirmar');
        await flushPromises();

        expect(feedbackStore.show).toHaveBeenCalledWith("Erro na remoção", "Erro ao remover", "danger");
    });
});
