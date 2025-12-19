import { mount, flushPromises } from '@vue/test-utils';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import CadAtividades from '@/views/CadAtividades.vue';
import { createTestingPinia } from '@pinia/testing';
import { useAtividadesStore } from '@/stores/atividades';
import { useSubprocessosStore } from '@/stores/subprocessos';
import { useProcessosStore } from '@/stores/processos';
import { useFeedbackStore } from '@/stores/feedback';
import { useUnidadesStore } from '@/stores/unidades';
import { useRoute, useRouter } from 'vue-router';
import { Perfil, TipoProcesso } from '@/types/tipos';
import * as subprocessoService from '@/services/subprocessoService';
import * as unidadesService from '@/services/unidadesService';
import * as processoService from '@/services/processoService';

// Mocks
vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-router')>();
  return {
    ...actual,
    useRoute: vi.fn(),
    useRouter: vi.fn(),
  };
});

vi.mock('@/services/subprocessoService', () => ({
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarSubprocessoDetalhe: vi.fn(),
    obterPermissoes: vi.fn(),
    validarCadastro: vi.fn(),
    obterStatus: vi.fn(),
    listarAtividades: vi.fn().mockResolvedValue([]),
}));

vi.mock('@/services/unidadesService', () => ({
    buscarUnidadePorSigla: vi.fn(),
}));

vi.mock('@/services/processoService', () => ({
    obterDetalhesProcesso: vi.fn(),
}));

// Component Stubs
const AtividadeItemStub = {
    template: '<div>AtividadeItem <button @click="$emit(\'remover-atividade\')">Remover</button></div>',
    props: ['atividade', 'pode-editar', 'erro-validacao'],
    emits: ['atualizar-atividade', 'remover-atividade', 'adicionar-conhecimento', 'atualizar-conhecimento', 'remover-conhecimento']
};
const ImportarAtividadesModalStub = { template: '<div>Importar</div>', props: ['mostrar'] };
const ImpactoMapaModalStub = { template: '<div>Impacto</div>', props: ['mostrar'] };
const ConfirmacaoDisponibilizacaoModalStub = { template: '<div>Confirmacao</div>', props: ['mostrar'], emits: ['confirmar'] };
const ModalConfirmacaoStub = { template: '<div>ModalConfirmacao</div>', props: ['modelValue', 'titulo', 'mensagem', 'variant'], emits: ['update:modelValue', 'confirmar'] };
const HistoricoAnaliseModalStub = { template: '<div>Historico</div>', props: ['mostrar'] };
const BContainerStub = { template: '<div><slot></slot></div>' };
const BButtonStub = { template: '<button @click="$emit(\'click\')"><slot></slot></button>', props: ['disabled'] };
const BFormStub = { template: '<form @submit="$emit(\'submit\', $event)"><slot></slot></form>' };
const BFormInputStub = { template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />', props: ['modelValue'] };
const BColStub = { template: '<div><slot></slot></div>' };

describe('CadAtividades.vue', () => {

    beforeEach(() => {
        // Mock router
        (useRouter as any).mockReturnValue({
            push: vi.fn(),
            back: vi.fn()
        });
        (useRoute as any).mockReturnValue({
            params: {}
        });

        // Setup default mocks
        vi.mocked(subprocessoService.obterPermissoes).mockResolvedValue({
            podeEditarMapa: true,
            podeVisualizarImpacto: true,
            podeDisponibilizarCadastro: true
        } as any);
        vi.mocked(subprocessoService.validarCadastro).mockResolvedValue({ valido: true, erros: [] });
        vi.mocked(unidadesService.buscarUnidadePorSigla).mockResolvedValue({ codigo: 10, sigla: 'TIC', nome: 'Tecnologia' } as any);
        vi.mocked(processoService.obterDetalhesProcesso).mockResolvedValue({
            codigo: 1,
            descricao: 'Processo',
            unidades: [
                { sigla: 'TIC', codSubprocesso: 100, mapaCodigo: 200, situacaoSubprocesso: 'EM_ANDAMENTO', filhas: [] }
            ],
            resumoSubprocessos: []
        } as any);
    });

    const mountComponent = (initialState = {}) => {
        return mount(CadAtividades, {
            props: {
                codProcesso: 1,
                sigla: 'TIC'
            },
            global: {
                plugins: [createTestingPinia({
                    createSpy: vi.fn,
                    stubActions: false, // We'll mock store actions individually if needed
                    initialState: {
                        perfil: {
                            perfilSelecionado: Perfil.CHEFE
                        },
                        ...initialState
                    }
                })],
                stubs: {
                    AtividadeItem: AtividadeItemStub,
                    ImportarAtividadesModal: ImportarAtividadesModalStub,
                    ImpactoMapaModal: ImpactoMapaModalStub,
                    ConfirmacaoDisponibilizacaoModal: ConfirmacaoDisponibilizacaoModalStub,
                    ModalConfirmacao: ModalConfirmacaoStub,
                    HistoricoAnaliseModal: HistoricoAnaliseModalStub,
                    BContainer: BContainerStub,
                    BButton: BButtonStub,
                    BForm: BFormStub,
                    BFormInput: BFormInputStub,
                    BCol: BColStub
                }
            }
        });
    };

    it('deve carregar dados e exibir atividades', async () => {
        // Mock resolver to return valid IDs
        // Note: useSubprocessoResolver uses useProcessosStore().processoDetalhe logic.
        // We need to setup store state correctly for the resolver to work.
        const initialState = {
            processos: {
                processoDetalhe: {
                    codigo: 1,
                    unidades: [
                        { sigla: 'TIC', codSubprocesso: 100, mapaCodigo: 200 } // Resolver needs this structure
                    ]
                }
            }
        };

        const wrapper = mountComponent(initialState);
        const atividadesStore = useAtividadesStore();
        atividadesStore.buscarAtividadesParaSubprocesso = vi.fn().mockResolvedValue(undefined);
        atividadesStore.obterAtividadesPorSubprocesso = vi.fn().mockReturnValue([
            { codigo: 1, descricao: 'Atv 1' }
        ]);

        await flushPromises(); // Wait for onMounted

        expect(atividadesStore.buscarAtividadesParaSubprocesso).toHaveBeenCalledWith(100);
        expect(wrapper.findAllComponents(AtividadeItemStub)).toHaveLength(1);
    });

    it('deve adicionar nova atividade', async () => {
        vi.mocked(processoService.obterDetalhesProcesso).mockResolvedValue({
            codigo: 1,
            descricao: 'Processo',
            unidades: [
                { sigla: 'TIC', codSubprocesso: 100, mapaCodigo: 200, situacaoSubprocesso: 'MAPEAMENTO_CADASTRO_EM_ANDAMENTO', filhas: [] }
            ],
            resumoSubprocessos: []
        } as any);

        const wrapper = mountComponent();
        await flushPromises();

        const atividadesStore = useAtividadesStore();
        atividadesStore.adicionarAtividade = vi.fn().mockResolvedValue('EM_ANDAMENTO');

        // Input text
        const input = wrapper.findComponent(BFormInputStub);
        await input.vm.$emit('update:modelValue', 'Nova Atividade');

        // Submit form
        const form = wrapper.findComponent(BFormStub);
        await form.trigger('submit');
        await flushPromises();

        expect(atividadesStore.adicionarAtividade).toHaveBeenCalledWith(100, 200, { descricao: 'Nova Atividade' });
    });

    it('deve disponibilizar cadastro', async () => {
        vi.mocked(processoService.obterDetalhesProcesso).mockResolvedValue({
            codigo: 1,
            descricao: 'Processo',
            unidades: [
                { sigla: 'TIC', codSubprocesso: 100, mapaCodigo: 200, situacaoSubprocesso: 'MAPEAMENTO_CADASTRO_EM_ANDAMENTO', filhas: [] }
            ],
            resumoSubprocessos: []
        } as any);

        const wrapper = mountComponent();
        await flushPromises();

        // Click disponibilizar button
        const btnDisponibilizar = wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]');
        await btnDisponibilizar.trigger('click');
        await flushPromises();

        // Check if confirmation modal opened
        expect(wrapper.findComponent(ConfirmacaoDisponibilizacaoModalStub).props('mostrar')).toBe(true);

        // Confirm
        const subprocessosStore = useSubprocessosStore();
        subprocessosStore.disponibilizarCadastro = vi.fn().mockResolvedValue(true);

        const modal = wrapper.findComponent(ConfirmacaoDisponibilizacaoModalStub);
        await modal.vm.$emit('confirmar');
        await flushPromises();

        expect(subprocessosStore.disponibilizarCadastro).toHaveBeenCalledWith(100);
    });

    it('deve exibir erros de validação ao tentar disponibilizar', async () => {
        vi.mocked(processoService.obterDetalhesProcesso).mockResolvedValue({
            codigo: 1,
            descricao: 'Processo',
            unidades: [
                { sigla: 'TIC', codSubprocesso: 100, mapaCodigo: 200, situacaoSubprocesso: 'MAPEAMENTO_CADASTRO_EM_ANDAMENTO', filhas: [] }
            ],
            resumoSubprocessos: []
        } as any);

        const wrapper = mountComponent();
        await flushPromises();

        vi.mocked(subprocessoService.validarCadastro).mockResolvedValue({
            valido: false,
            erros: [{ atividadeId: 1, mensagem: 'Erro teste' }]
        });

        const btnDisponibilizar = wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]');
        await btnDisponibilizar.trigger('click');
        await flushPromises();

        expect(wrapper.findComponent(ConfirmacaoDisponibilizacaoModalStub).props('mostrar')).toBe(false);
        // Verify that error props are passed to AtividadeItem if possible,
        // but AtividadeItemStub props are reactive?
        // We can check if wrapper.vm.errosValidacao has values.
        expect((wrapper.vm as any).errosValidacao).toHaveLength(1);
    });

    it('deve remover atividade após confirmação', async () => {
        vi.mocked(processoService.obterDetalhesProcesso).mockResolvedValue({
            codigo: 1,
            descricao: 'Processo',
            unidades: [
                { sigla: 'TIC', codSubprocesso: 100, mapaCodigo: 200, situacaoSubprocesso: 'MAPEAMENTO_CADASTRO_EM_ANDAMENTO', filhas: [] }
            ],
            resumoSubprocessos: []
        } as any);

        const wrapper = mountComponent();
        const atividadesStore = useAtividadesStore();
        atividadesStore.obterAtividadesPorSubprocesso = vi.fn().mockReturnValue([
            { codigo: 1, descricao: 'Atv 1' }
        ]);
        await flushPromises();

        // Trigger remove
        const item = wrapper.findComponent(AtividadeItemStub);
        await item.vm.$emit('remover-atividade');
        await flushPromises();

        // Assert Modal Open
        const modal = wrapper.findComponent(ModalConfirmacaoStub);
        expect(modal.exists()).toBe(true);
        expect(modal.props('modelValue')).toBe(true);

        // Confirm
        atividadesStore.removerAtividade = vi.fn().mockResolvedValue('EM_ANDAMENTO');
        await modal.vm.$emit('confirmar');
        await flushPromises();

        expect(atividadesStore.removerAtividade).toHaveBeenCalledWith(100, 1);
    });
});
