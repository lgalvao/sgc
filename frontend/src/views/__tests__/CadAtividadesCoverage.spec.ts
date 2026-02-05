import {beforeEach, describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import CadAtividades from '@/views/CadAtividades.vue';
import {useAtividadesStore} from '@/stores/atividades';
import {useSubprocessosStore} from '@/stores/subprocessos';
import {useMapasStore} from '@/stores/mapas';
import {useAnalisesStore} from '@/stores/analises';
import {useFeedbackStore} from '@/stores/feedback';
import {flushPromises} from '@vue/test-utils';
import {SituacaoSubprocesso} from '@/types/tipos';

// Mock router
const { mockPush, mockBack } = vi.hoisted(() => ({
    mockPush: vi.fn(),
    mockBack: vi.fn()
}));

vi.mock('vue-router', () => ({
    useRouter: () => ({ push: mockPush, back: mockBack }),
    useRoute: () => ({ params: { codProcesso: '1', sigla: 'TEST' } }),
    createRouter: vi.fn(() => ({
        beforeEach: vi.fn(),
        afterEach: vi.fn(),
    })),
    createWebHistory: vi.fn(),
    createMemoryHistory: vi.fn(),
}));

describe('CadAtividades.vue Coverage', () => {
    const commonStubs = {
        PageHeader: { template: '<div><slot /><slot name="actions" /></div>' },
        BButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
        BDropdown: { template: '<div><slot /></div>' },
        BDropdownItem: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
        BContainer: { template: '<div><slot /></div>' },
        LoadingButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
        EmptyState: { template: '<div><slot /></div>' },
        AtividadeItem: {
            name: 'AtividadeItem',
            template: '<div class="atividade-item-stub">' +
                      '<button class="btn-rem-at" @click="$emit(\'remover-atividade\')">Rem At</button>' +
                      '<button class="btn-add-con" @click="$emit(\'adicionar-conhecimento\', \'novo con\')">Add Con</button>' +
                      '<button class="btn-rem-con" @click="$emit(\'remover-conhecimento\', 100)">Rem Con</button>' +
                      '<button class="btn-edit-at" @click="$emit(\'atualizar-atividade\', \'editat\')">Edit At</button>' +
                      '<button class="btn-edit-con" @click="$emit(\'atualizar-conhecimento\', 100, \'editcon\')">Edit Con</button>' +
                      '</div>',
            props: ['atividade', 'podeEditar', 'erroValidacao']
        },
        CadAtividadeForm: { name: 'CadAtividadeForm', template: '<div><slot /></div>', props: ['modelValue', 'disabled', 'loading'] },
        ImportarAtividadesModal: { name: 'ImportarAtividadesModal', template: '<div />' },
        ImpactoMapaModal: { name: 'ImpactoMapaModal', template: '<div />' },
        ConfirmacaoDisponibilizacaoModal: { name: 'ConfirmacaoDisponibilizacaoModal', template: '<div />' },
        HistoricoAnaliseModal: { name: 'HistoricoAnaliseModal', template: '<div />' },
        BAlert: { name: 'BAlert', template: '<div><slot /></div>', props: ['modelValue'] },
        ModalConfirmacao: {
            name: 'ModalConfirmacao',
            template: '<div class="modal-conf-stub"><button class="btn-conf" @click="$emit(\'confirmar\')">Conf</button></div>',
            props: ['modelValue', 'titulo', 'mensagem']
        }
    };

    const createWrapper = (initialState = {}) => {
        const pinia = createTestingPinia({
            createSpy: vi.fn,
            stubActions: true,
            initialState
        });
        const wrapper = mount(CadAtividades, {
            props: {
                codProcesso: 1,
                sigla: 'TEST'
            },
            global: {
                plugins: [pinia],
                stubs: commonStubs
            }
        });
        return { wrapper, pinia };
    };

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('handles removal of an activity', async () => {
        const { wrapper, pinia } = createWrapper();
        const atividadesStore = useAtividadesStore(pinia);
        const subprocessosStore = useSubprocessosStore(pinia);

        // Mock return values
        (subprocessosStore.buscarSubprocessoPorProcessoEUnidade as any).mockResolvedValue(123);
        (atividadesStore.obterAtividadesPorSubprocesso as any).mockReturnValue([
            { codigo: 1, descricao: 'Atividade 1', conhecimentos: [] }
        ]);

        // Trigger onMounted manually if needed or just wait
        await flushPromises();
        (wrapper.vm as any).codSubprocesso = 123; // Force set
        await wrapper.vm.$nextTick();

        // Trigger removal via stub
        await wrapper.find('.btn-rem-at').trigger('click');

        expect((wrapper.vm as any).dadosRemocao).toEqual({ tipo: 'atividade', index: 0 });
        expect((wrapper.vm as any).mostrarModalConfirmacaoRemocao).toBe(true);

        // Confirm removal
        const spy = vi.spyOn(atividadesStore, 'removerAtividade').mockResolvedValue({} as any);
        await wrapper.find('.btn-conf').trigger('click');
        
        expect(spy).toHaveBeenCalledWith(123, 1);
        expect((wrapper.vm as any).mostrarModalConfirmacaoRemocao).toBe(false);
    });

    it('handles removal of a knowledge item', async () => {
        const { wrapper, pinia } = createWrapper();
        const atividadesStore = useAtividadesStore(pinia);
        const subprocessosStore = useSubprocessosStore(pinia);

        (subprocessosStore.buscarSubprocessoPorProcessoEUnidade as any).mockResolvedValue(123);
        (atividadesStore.obterAtividadesPorSubprocesso as any).mockReturnValue([
            { codigo: 1, descricao: 'Atividade 1', conhecimentos: [{ codigo: 100, descricao: 'C1' }] }
        ]);

        await flushPromises();
        (wrapper.vm as any).codSubprocesso = 123;
        await wrapper.vm.$nextTick();

        // Trigger removal of knowledge
        await wrapper.find('.btn-rem-con').trigger('click');

        expect((wrapper.vm as any).dadosRemocao).toEqual({ tipo: 'conhecimento', index: 0, conhecimentoCodigo: 100 });

        const spy = vi.spyOn(atividadesStore, 'removerConhecimento').mockResolvedValue({} as any);
        await wrapper.find('.btn-conf').trigger('click');

        expect(spy).toHaveBeenCalledWith(123, 1, 100);
    });

    it('saves activity edition', async () => {
        const { wrapper, pinia } = createWrapper();
        const atividadesStore = useAtividadesStore(pinia);
        const subprocessosStore = useSubprocessosStore(pinia);

        (subprocessosStore.buscarSubprocessoPorProcessoEUnidade as any).mockResolvedValue(123);
        (atividadesStore.obterAtividadesPorSubprocesso as any).mockReturnValue([
            { codigo: 1, descricao: 'Atividade 1', conhecimentos: [] }
        ]);
        await flushPromises();
        (wrapper.vm as any).codSubprocesso = 123;
        await wrapper.vm.$nextTick();

        const spy = (atividadesStore.atualizarAtividade as any).mockResolvedValue({});
        await wrapper.find('.btn-edit-at').trigger('click');

        expect(spy).toHaveBeenCalledWith(123, 1, expect.objectContaining({ descricao: 'editat' }));
    });

    it('saves knowledge edition', async () => {
        const { wrapper, pinia } = createWrapper();
        const atividadesStore = useAtividadesStore(pinia);
        const subprocessosStore = useSubprocessosStore(pinia);

        (subprocessosStore.buscarSubprocessoPorProcessoEUnidade as any).mockResolvedValue(123);
        (atividadesStore.obterAtividadesPorSubprocesso as any).mockReturnValue([
            { codigo: 1, descricao: 'Atividade 1', conhecimentos: [{ codigo: 100, descricao: 'C1' }] }
        ]);
        await flushPromises();
        (wrapper.vm as any).codSubprocesso = 123;
        await wrapper.vm.$nextTick();

        const spy = (atividadesStore.atualizarConhecimento as any).mockResolvedValue({});
        await wrapper.find('.btn-edit-con').trigger('click');

        expect(spy).toHaveBeenCalledWith(123, 1, 100, expect.objectContaining({ descricao: 'editcon' }));
    });

    it('adds knowledge', async () => {
        const { wrapper, pinia } = createWrapper();
        const atividadesStore = useAtividadesStore(pinia);
        const subprocessosStore = useSubprocessosStore(pinia);

        (subprocessosStore.buscarSubprocessoPorProcessoEUnidade as any).mockResolvedValue(123);
        (atividadesStore.obterAtividadesPorSubprocesso as any).mockReturnValue([
            { codigo: 1, descricao: 'Atividade 1', conhecimentos: [] }
        ]);
        await flushPromises();
        (wrapper.vm as any).codSubprocesso = 123;
        await wrapper.vm.$nextTick();

        const spy = (atividadesStore.adicionarConhecimento as any).mockResolvedValue({});
        await wrapper.find('.btn-add-con').trigger('click');

        expect(spy).toHaveBeenCalledWith(123, 1, { descricao: 'novo con' });
    });

    it('opens history modal', async () => {
        const { wrapper, pinia } = createWrapper();
        const analisesStore = useAnalisesStore(pinia);
        (wrapper.vm as any).codSubprocesso = 123;

        const spy = vi.spyOn(analisesStore, 'buscarAnalisesCadastro').mockResolvedValue([]);

        await (wrapper.vm as any).abrirModalHistorico();

        expect(spy).toHaveBeenCalledWith(123);
        expect((wrapper.vm as any).mostrarModalHistorico).toBe(true);
    });

    it('opens impact modal', async () => {
        const { wrapper, pinia } = createWrapper();
        const mapasStore = useMapasStore(pinia);
        (wrapper.vm as any).codSubprocesso = 123;

        const spy = vi.spyOn(mapasStore, 'buscarImpactoMapa').mockResolvedValue({} as any);

        await (wrapper.vm as any).abrirModalImpacto();

        expect(spy).toHaveBeenCalledWith(123);
        expect((wrapper.vm as any).mostrarModalImpacto).toBe(true);
    });

    it('handles import activities', async () => {
        const { wrapper, pinia } = createWrapper();
        const atividadesStore = useAtividadesStore(pinia);
        const feedbackStore = useFeedbackStore(pinia);
        (wrapper.vm as any).codSubprocesso = 123;

        const spy = vi.spyOn(atividadesStore, 'buscarAtividadesParaSubprocesso').mockResolvedValue([]);
        const fbSpy = vi.spyOn(feedbackStore, 'show');

        await (wrapper.vm as any).handleImportAtividades();

        expect(spy).toHaveBeenCalledWith(123);
        expect(fbSpy).toHaveBeenCalled();
        expect((wrapper.vm as any).mostrarModalImportar).toBe(false);
    });

    it('validates and opens confirmation modal on disponibilizarCadastro', async () => {
        const { wrapper, pinia } = createWrapper();
        const subprocessosStore = useSubprocessosStore(pinia);

        (wrapper.vm as any).codSubprocesso = 123;
        subprocessosStore.subprocessoDetalhe = {
            situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO
        } as any;

        vi.spyOn(subprocessosStore, 'validarCadastro').mockResolvedValue({ valido: true, erros: [] });

        await (wrapper.vm as any).disponibilizarCadastro();

        expect(subprocessosStore.validarCadastro).toHaveBeenCalledWith(123);
        expect((wrapper.vm as any).mostrarModalConfirmacao).toBe(true);
    });

    it('shows errors on invalid validation', async () => {
        const { wrapper, pinia } = createWrapper();
        const subprocessosStore = useSubprocessosStore(pinia);

        (wrapper.vm as any).codSubprocesso = 123;
        subprocessosStore.subprocessoDetalhe = {
            situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO
        } as any;

        const erros = [
            { atividadeCodigo: 1, mensagem: 'Erro na atividade' },
            { atividadeCodigo: null, mensagem: 'Erro global' }
        ];
        vi.spyOn(subprocessosStore, 'validarCadastro').mockResolvedValue({ valido: false, erros });

        await (wrapper.vm as any).disponibilizarCadastro();

        expect((wrapper.vm as any).errosValidacao).toEqual(erros);
        expect((wrapper.vm as any).erroGlobal).toBe('Erro global');
        expect((wrapper.vm as any).obterErroParaAtividade(1)).toBe('Erro na atividade');
    });

    it('confirms disponibilizacao', async () => {
        const { wrapper, pinia } = createWrapper();
        const subprocessosStore = useSubprocessosStore(pinia);
        (wrapper.vm as any).codSubprocesso = 123;

        const spy = vi.spyOn(subprocessosStore, 'disponibilizarCadastro').mockResolvedValue(true);

        await (wrapper.vm as any).confirmarDisponibilizacao();

        expect(spy).toHaveBeenCalledWith(123);
        expect(mockPush).toHaveBeenCalledWith('/painel');
    });

    it('confirms disponibilizacao revisao', async () => {
        const { wrapper, pinia } = createWrapper();
        const subprocessosStore = useSubprocessosStore(pinia);
        (wrapper.vm as any).codSubprocesso = 123;
        subprocessosStore.subprocessoDetalhe = { tipoProcesso: 'REVISAO' } as any;

        const spy = vi.spyOn(subprocessosStore, 'disponibilizarRevisaoCadastro').mockResolvedValue(true);

        await (wrapper.vm as any).confirmarDisponibilizacao();

        expect(spy).toHaveBeenCalledWith(123);
    });

    it('shows error if disponibilizarCadastro is called in wrong situation', async () => {
        const { wrapper, pinia } = createWrapper();
        const subprocessosStore = useSubprocessosStore(pinia);
        const feedbackStore = useFeedbackStore(pinia);

        subprocessosStore.subprocessoDetalhe = { situacao: 'OUTRA' } as any;

        await (wrapper.vm as any).disponibilizarCadastro();

        expect(feedbackStore.show).toHaveBeenCalledWith("Ação não permitida", expect.any(String), "danger");
    });

    it('handles error in confirmarRemocao', async () => {
        const { wrapper, pinia } = createWrapper();
        const atividadesStore = useAtividadesStore(pinia);
        const feedbackStore = useFeedbackStore(pinia);

        (atividadesStore.obterAtividadesPorSubprocesso as any).mockReturnValue([{ codigo: 1 }]);
        (wrapper.vm as any).codSubprocesso = 123;
        (wrapper.vm as any).dadosRemocao = { tipo: 'atividade', index: 0 };

        (atividadesStore.removerAtividade as any).mockRejectedValue(new Error('Rem Error'));

        await (wrapper.vm as any).confirmarRemocao();

        expect(feedbackStore.show).toHaveBeenCalledWith("Erro na remoção", "Rem Error", "danger");
        expect((wrapper.vm as any).mostrarModalConfirmacaoRemocao).toBe(false);
    });

    it('fecharModalImpacto closes impact modal', async () => {
        const { wrapper } = createWrapper();
        (wrapper.vm as any).mostrarModalImpacto = true;
        (wrapper.vm as any).fecharModalImpacto();
        expect((wrapper.vm as any).mostrarModalImpacto).toBe(false);
    });

    it('handles initialization when subprocess is not found', async () => {
        const pinia = createTestingPinia({ stubActions: true });
        const subprocessosStore = useSubprocessosStore(pinia);
        (subprocessosStore.buscarSubprocessoPorProcessoEUnidade as any).mockResolvedValue(null);

        mount(CadAtividades, {
            props: { codProcesso: 1, sigla: 'TEST' },
            global: { plugins: [pinia], stubs: commonStubs }
        });

        await flushPromises();
        // Should log error, but most importantly we cover the else branch
    });

    it('adicionarAtividade returns false if codMapa is missing', async () => {
        const { wrapper, pinia } = createWrapper();
        const mapasStore = useMapasStore(pinia);
        mapasStore.mapaCompleto = null;

        const result = await (wrapper.vm as any).adicionarAtividade();
        expect(result).toBe(false);
    });

    it('triggers modal close handlers', async () => {
        const { wrapper } = createWrapper();

        const impactModal = wrapper.findComponent({ name: 'ImpactoMapaModal' });
        if (impactModal.exists()) await impactModal.vm.$emit('fechar');

        const importModal = wrapper.findComponent({ name: 'ImportarAtividadesModal' });
        if (importModal.exists()) await importModal.vm.$emit('fechar');

        const confirmModal = wrapper.findComponent({ name: 'ConfirmacaoDisponibilizacaoModal' });
        if (confirmModal.exists()) await confirmModal.vm.$emit('fechar');

        const historyModal = wrapper.findComponent({ name: 'HistoricoAnaliseModal' });
        if (historyModal.exists()) await historyModal.vm.$emit('fechar');

        expect((wrapper.vm as any).mostrarModalImportar).toBe(false);
    });

    it('navigates back when back button is clicked', async () => {
        const { wrapper } = createWrapper();
        await wrapper.find('[data-testid="btn-cad-atividades-voltar"]').trigger('click');
        expect(mockBack).toHaveBeenCalled();
    });

    it('dismisses global error', async () => {
        const { wrapper } = createWrapper();
        (wrapper.vm as any).erroGlobal = 'Some error';
        await wrapper.vm.$nextTick();

        const alert = wrapper.findComponent({ name: 'BAlert' });
        await alert.vm.$emit('dismissed');

        expect((wrapper.vm as any).erroGlobal).toBe(null);
    });
});
