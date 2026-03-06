import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount, RouterLinkStub} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import SubprocessoView from '@/views/SubprocessoView.vue';
import {useSubprocessosStore} from '@/stores/subprocessos';
import {useMapasStore} from '@/stores/mapas';
import {useProcessosStore} from '@/stores/processos';
import {SituacaoSubprocesso, TipoProcesso} from '@/types/tipos';
import * as processoService from '@/services/processoService';
import * as useAcessoModule from '@/composables/useAcesso';

const SubprocessoCardsStub = {
    template: '<div data-testid="subprocesso-cards"></div>',
    props: ['situacao', 'tipoProcesso']
};
const SubprocessoModalStub = {
    template: '<div data-testid="subprocesso-modal"></div>',
    props: ['mostrarModal'],
    emits: ['confirmar-alteracao', 'fechar-modal']
};

vi.mock('@/services/processoService', () => ({
    reabrirCadastro: vi.fn(),
    reabrirRevisaoCadastro: vi.fn(),
    enviarLembrete: vi.fn(),
}));

describe('SubprocessoView.vue', () => {
    const mockSubprocesso = {
        codigo: 10,
        situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
        situacaoLabel: 'Em Andamento',
        processoDescricao: 'Processo Teste',
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        unidade: {
            codigo: 1,
            sigla: 'TEST',
            nome: 'Unidade Teste'
        },
        responsavel: {
            codigo: 1,
            nome: 'Resp',
            tituloEleitoral: '123456789012',
            unidade: {codigo: 1, sigla: 'TEST', nome: 'Unidade Teste'},
            email: 'resp@test.com',
            ramal: '123'
        },
        titular: {
            codigo: 2,
            nome: 'Titular',
            tituloEleitoral: '987654321012',
            unidade: {codigo: 1, sigla: 'TEST', nome: 'Unidade Teste'},
            email: 'titular@test.com',
            ramal: '456'
        },
        etapaAtual: 1,
        prazoEtapaAtual: '2023-12-31T00:00:00',
        localizacaoAtual: 'Unidade Teste',
        isEmAndamento: true,
        elementosProcesso: [],

        movimentacoes: [] as any[]
    };

    const additionalStubs = {
        BContainer: {template: '<div><slot /></div>'},
        SubprocessoCards: SubprocessoCardsStub,
        SubprocessoModal: SubprocessoModalStub,
        BModal: {
            template: '<div><slot /><slot name="footer" /></div>',
            props: ['modelValue', 'title'],
            emits: ['update:modelValue', 'ok']
        },
        BFormTextarea: {
            template: '<textarea :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue'],
            emits: ['update:modelValue']
        },
        BButton: {template: '<button :disabled="disabled"><slot /></button>', props: ['disabled']}
    };

    beforeEach(() => {
        vi.clearAllMocks();
    });

    // Helper to mount component with specific setup
    const mountComponent = (overrideMockSubprocesso?: Partial<typeof mockSubprocesso>, accessOverrides: Partial<Record<string, any>> = {}) => {
        const subprocessoToUse = overrideMockSubprocesso ? {...mockSubprocesso, ...overrideMockSubprocesso} : mockSubprocesso;

        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeAlterarDataLimite: {value: true},
            podeReabrirCadastro: {value: true},
            podeReabrirRevisao: {value: true},
            podeEnviarLembrete: {value: true},
            podeDisponibilizarCadastro: {value: true},
            podeEditarCadastro: {value: true},
            ...accessOverrides
        } as any);

        const pinia = createTestingPinia({
            createSpy: vi.fn,
            initialState: {
                subprocessos: {
                    subprocessoDetalhe: null,
                },
                mapas: {
                    mapaCompleto: null,
                },
            },
            stubActions: true,
        });

        const store = useSubprocessosStore(pinia);
        const mapaStore = useMapasStore(pinia);
        const processosStore = useProcessosStore(pinia);

        (store.buscarSubprocessoPorProcessoEUnidade as any).mockImplementation(async () => 10);
        (store.buscarSubprocessoDetalhe as any).mockImplementation(async () => {
            store.subprocessoDetalhe = subprocessoToUse as any;
            return subprocessoToUse;
        });
        (mapaStore.buscarMapaCompleto as any).mockResolvedValue({});
        (store.alterarDataLimiteSubprocesso as any).mockResolvedValue({});

        (store.reabrirCadastro as any).mockImplementation(async (cod: number, just: string) => {
            try {
                await (processoService.reabrirCadastro as any)(cod, just);
                return true;
            } catch {
                return false;
            }
        });
        (store.reabrirRevisaoCadastro as any).mockImplementation(async (cod: number, just: string) => {
            try {
                await (processoService.reabrirRevisaoCadastro as any)(cod, just);
                return true;
            } catch {
                return false;
            }
        });

        (processosStore.enviarLembrete as any).mockImplementation(async (codProcesso: number, codUnidade: number) => {
            try {
                await (processoService.enviarLembrete as any)(codProcesso, codUnidade);
                return true;
            } catch {
                return false;
            }
        });

        const wrapper = mount(SubprocessoView, {
            global: {
                plugins: [pinia],
                stubs: {
                    RouterLink: RouterLinkStub,
                    RouterView: true,
                    ...additionalStubs,
                },
            },
            props: {
                codProcesso: 1,
                siglaUnidade: 'TEST'
            }
        });

        return {wrapper, store, mapaStore, processosStore};
    };

    it('fetches data on mount', async () => {
        const {store, mapaStore} = mountComponent();
        await flushPromises();

        expect(store.buscarSubprocessoPorProcessoEUnidade).toHaveBeenCalledWith(1, 'TEST');
        expect(store.buscarSubprocessoDetalhe).toHaveBeenCalledWith(10);
        expect(mapaStore.buscarMapaCompleto).toHaveBeenCalledWith(10);
    });

    it('renders components when data is available', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        await (wrapper.vm as any).$nextTick();

        expect(wrapper.find('[data-testid="header-subprocesso"]').exists()).toBe(true);
        expect(wrapper.findComponent(SubprocessoCardsStub).exists()).toBe(true);
        expect(wrapper.find('[data-testid="tbl-movimentacoes"]').exists()).toBe(true);
    });

    it('opens date limit modal when allowed', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        await (wrapper.vm as any).$nextTick();

        await wrapper.find('[data-testid="btn-alterar-data-limite"]').trigger('click');
        await (wrapper.vm as any).$nextTick();

        expect((wrapper.vm as any).modals.modals.alterarDataLimite.value.isOpen).toBe(true);
    });

    it('shows error when opening date limit modal is not allowed', async () => {
        const {wrapper} = mountComponent({}, {podeAlterarDataLimite: {value: false}});
        await flushPromises();
        await (wrapper.vm as any).$nextTick();

        (wrapper.vm as any).abrirModalAlterarDataLimite();
        await (wrapper.vm as any).$nextTick();

        expect((wrapper.vm as any).modals.modals.alterarDataLimite.value.isOpen).toBe(false);
    });

    it('handles date limit update confirmation', async () => {
        const {wrapper, store} = mountComponent();
        await flushPromises();
        await (wrapper.vm as any).$nextTick();

        // Open modal
        (wrapper.vm as any).modals.open('alterarDataLimite');
        await (wrapper.vm as any).$nextTick();

        const modal = wrapper.findComponent(SubprocessoModalStub);
        await modal.vm.$emit('confirmar-alteracao', '2024-01-01');

        await flushPromises();

        expect(store.alterarDataLimiteSubprocesso).toHaveBeenCalledWith(10, {novaData: '2024-01-01'});
        expect((wrapper.vm as any).modals.modals.alterarDataLimite.value.isOpen).toBe(false);
    });

    it('trata erro ao alterar data limite', async () => {
        const {wrapper, store} = mountComponent();
        await flushPromises();
        (store.alterarDataLimiteSubprocesso as any).mockRejectedValue(new Error('Falha'));

        (wrapper.vm as any).mostrarModalAlterarDataLimite = true;
        const modal = wrapper.findComponent(SubprocessoModalStub);
        await modal.vm.$emit('confirmar-alteracao', '2024-01-01');
        await flushPromises();

        expect(store.alterarDataLimiteSubprocesso).toHaveBeenCalled();
    });

    it('reabre cadastro com sucesso', async () => {
        const {wrapper, store} = mountComponent();
        await flushPromises();

        await wrapper.find('[data-testid="btn-reabrir-cadastro"]').trigger('click');
        await (wrapper.vm as any).$nextTick();

        expect((wrapper.vm as any).tipoReabertura).toBe('cadastro');
        expect((wrapper.vm as any).modals.modals.reabrir.value.isOpen).toBe(true);

        // Preencher justificativa
        const textarea = wrapper.find('textarea');
        await textarea.setValue('Erro no preenchimento');

        const btn = wrapper.find('[data-testid="btn-confirmar-reabrir"]');
        await btn.trigger('click');
        await flushPromises();

        expect(processoService.reabrirCadastro).toHaveBeenCalledWith(10, 'Erro no preenchimento');
        expect(store.buscarSubprocessoDetalhe).toHaveBeenCalledTimes(2); // Initial + Reload
    });

    it('reabre revisão com sucesso', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();

        await wrapper.find('[data-testid="btn-reabrir-revisao"]').trigger('click');
        expect((wrapper.vm as any).tipoReabertura).toBe('revisao');

        const textarea = wrapper.find('textarea');
        await textarea.setValue('Revisão incompleta');

        const btn = wrapper.find('[data-testid="btn-confirmar-reabrir"]');
        await btn.trigger('click');
        await flushPromises();

        expect(processoService.reabrirRevisaoCadastro).toHaveBeenCalledWith(10, 'Revisão incompleta');
    });

    it('impede reabertura se justificativa vazia (botão desabilitado)', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();

        await wrapper.find('[data-testid="btn-reabrir-cadastro"]').trigger('click');

        // O botão deve estar desabilitado se a justificativa for vazia
        const btn = wrapper.find('[data-testid="btn-confirmar-reabrir"]');
        expect(btn.attributes('disabled')).toBeDefined();
        expect(processoService.reabrirCadastro).not.toHaveBeenCalled();
    });

    it('trata erro na API ao reabrir', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        vi.mocked(processoService.reabrirCadastro).mockRejectedValue(new Error('API Error'));

        await wrapper.find('[data-testid="btn-reabrir-cadastro"]').trigger('click');

        const textarea = wrapper.find('textarea');
        await textarea.setValue('Justificativa');

        const btn = wrapper.find('[data-testid="btn-confirmar-reabrir"]');
        await btn.trigger('click');
        await flushPromises();

        expect(processoService.reabrirCadastro).toHaveBeenCalled();
    });

    it('envia lembrete com sucesso', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();

        await wrapper.find('[data-testid="btn-enviar-lembrete"]').trigger('click');
        await (wrapper.vm as any).$nextTick();

        const btn = wrapper.find('[data-testid="btn-confirmar-enviar-lembrete"]');
        await btn.trigger('click');
        await flushPromises();

        expect(processoService.enviarLembrete).toHaveBeenCalledWith(1, 1);
    });

    it('trata erro ao enviar lembrete', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        vi.mocked(processoService.enviarLembrete).mockRejectedValue(new Error('Erro'));

        await wrapper.find('[data-testid="btn-enviar-lembrete"]').trigger('click');
        await (wrapper.vm as any).$nextTick();

        const btn = wrapper.find('[data-testid="btn-confirmar-enviar-lembrete"]');
        await btn.trigger('click');
        await flushPromises();

        expect(processoService.enviarLembrete).toHaveBeenCalled();
    });
});
