import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount, RouterLinkStub} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import SubprocessoView from '@/views/SubprocessoView.vue';
import {useMapas} from '@/composables/useMapas';
import {reactive, ref} from 'vue';
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

const processosMock = {
    processoDetalhe: ref<any>(null),
    enviarLembrete: vi.fn(),
};

const fluxoSubprocessoMock = {
    alterarDataLimiteSubprocesso: vi.fn(),
    reabrirCadastro: vi.fn(),
    reabrirRevisaoCadastro: vi.fn(),
};
const subprocessosMock = reactive({
    subprocessoDetalhe: null as any,
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarSubprocessoDetalhe: vi.fn(),
    buscarContextoEdicao: vi.fn(),
    atualizarStatusLocal: vi.fn(),
    lastError: null as any,
    clearError: vi.fn(),
});

vi.mock('@/composables/useProcessos', () => ({
    useProcessos: () => processosMock
}));

vi.mock('@/composables/useFluxoSubprocesso', () => ({
    useFluxoSubprocesso: () => fluxoSubprocessoMock
}));
vi.mock('@/composables/useSubprocessos', () => ({useSubprocessos: () => subprocessosMock}));

describe('SubprocessoView.vue', () => {
    const mockSubprocesso = {
        codigo: 10,
        situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
        situacaoLabel: 'Em andamento',
        processoDescricao: 'Processo teste',
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        unidade: {
            codigo: 1,
            sigla: 'TEST',
            nome: 'Unidade teste'
        },
        responsavel: {
            codigo: 1,
            nome: 'Resp',
            tituloEleitoral: '123456789012',
            unidade: {codigo: 1, sigla: 'TEST', nome: 'Unidade teste'},
            email: 'resp@test.com',
            ramal: '123'
        },
        titular: {
            codigo: 2,
            nome: 'Titular',
            tituloEleitoral: '987654321012',
            unidade: {codigo: 1, sigla: 'TEST', nome: 'Unidade teste'},
            email: 'titular@test.com',
            ramal: '456'
        },
        etapaAtual: 1,
        prazoEtapaAtual: '2023-12-31T00:00:00',
        localizacaoAtual: 'Unidade teste',
        isEmAndamento: true,
        elementosProcesso: [],

        movimentacoes: [] as any[]
    };

    const additionalStubs = {
        SubprocessoCards: SubprocessoCardsStub,
        SubprocessoModal: SubprocessoModalStub,
        ModalConfirmacao: {
            name: 'ModalConfirmacao',
            template: '<div><slot /><button :data-testid="testIdConfirmar" :disabled="okDisabled" @click="$emit(\'confirmar\')">OK</button></div>',
            props: ['modelValue', 'titulo', 'testIdConfirmar', 'okDisabled'],
            emits: ['update:modelValue', 'confirmar']
        },
        AppAlert: {
            name: 'AppAlert',
            template: '<div><button @click="$emit(\'dismissed\')">x</button></div>'
        },
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
        BButton: {template: '<button :disabled="disabled"><slot /></button>', props: ['disabled']},
        BAlert: {
            name: 'BAlert',
            template: '<div><slot /><button @click="$emit(\'dismissed\')">x</button></div>', 
            props: ['variant', 'dismissible', 'modelValue']
        },
        BSpinner: {template: '<div></div>'},
    };

    beforeEach(() => {
        vi.clearAllMocks();
        processosMock.processoDetalhe.value = null;
        fluxoSubprocessoMock.alterarDataLimiteSubprocesso.mockResolvedValue({});
        fluxoSubprocessoMock.reabrirCadastro.mockResolvedValue(true);
        fluxoSubprocessoMock.reabrirRevisaoCadastro.mockResolvedValue(true);
        subprocessosMock.subprocessoDetalhe = null;
        subprocessosMock.buscarSubprocessoPorProcessoEUnidade = vi.fn().mockResolvedValue(10);
        subprocessosMock.buscarSubprocessoDetalhe = vi.fn();
        subprocessosMock.buscarContextoEdicao = vi.fn();
        subprocessosMock.atualizarStatusLocal = vi.fn();
        subprocessosMock.lastError = null;
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

        const store = subprocessosMock as any;
        const mapaStore = useMapas();
        processosMock.processoDetalhe.value = {situacao: 'EM_ANDAMENTO'};

        (store.buscarSubprocessoPorProcessoEUnidade as any).mockImplementation(async () => 10);
        (store.buscarSubprocessoDetalhe as any).mockImplementation(async () => {
            store.subprocessoDetalhe = subprocessoToUse as any;
            return subprocessoToUse;
        });
        mapaStore.buscarMapaCompleto = vi.fn().mockResolvedValue({});
        mapaStore.mapaCompleto.value = null;

        (processosMock.enviarLembrete as any).mockImplementation(async (codProcesso: number, codUnidade: number) => {
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

        return {wrapper, store, mapaStore, processos: processosMock};
    };

    it('fetches data on mount', async () => {
        const {store, mapaStore} = mountComponent();
        await flushPromises();

        expect(store.buscarSubprocessoPorProcessoEUnidade).toHaveBeenCalledWith(1, 'TEST');
        expect(store.buscarSubprocessoDetalhe).toHaveBeenCalledWith(10);
        expect(mapaStore.buscarMapaCompleto).toHaveBeenCalledWith(10);
    });

    it('limpa subprocessoDetalhe imediatamente ao montar para evitar dados desatualizados', async () => {
        // Simula dado desatualizado de uma visita anterior (ex: processo de mapeamento
        // com podeEditarCadastro=false), que poderia fazer SubprocessoCards mostrar
        // a rota errada (vis-cadastro em vez de cadastro) na primeira renderização
        subprocessosMock.subprocessoDetalhe = {
            ...mockSubprocesso,
            codigo: 999,
            situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
            permissoes: {podeEditarCadastro: false}
        } as any;

        mountComponent();
        // Antes de qualquer await, o subprocessoDetalhe já deve ter sido limpo
        expect(subprocessosMock.subprocessoDetalhe).toBeNull();
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
        const {wrapper} = mountComponent();
        await flushPromises();
        await (wrapper.vm as any).$nextTick();

        (wrapper.vm as any).modals.open('alterarDataLimite');
        await (wrapper.vm as any).$nextTick();

        const modal = wrapper.findComponent(SubprocessoModalStub);
        await modal.vm.$emit('confirmar-alteracao', '2024-01-01');

        await flushPromises();

        expect(fluxoSubprocessoMock.alterarDataLimiteSubprocesso).toHaveBeenCalledWith(10, {novaData: '2024-01-01'});
        expect((wrapper.vm as any).modals.modals.alterarDataLimite.value.isOpen).toBe(false);
    });

    it('trata erro ao alterar data limite', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        fluxoSubprocessoMock.alterarDataLimiteSubprocesso.mockRejectedValue(new Error('Falha'));

        (wrapper.vm as any).mostrarModalAlterarDataLimite = true;
        const modal = wrapper.findComponent(SubprocessoModalStub);
        await modal.vm.$emit('confirmar-alteracao', '2024-01-01');
        await flushPromises();

        expect(fluxoSubprocessoMock.alterarDataLimiteSubprocesso).toHaveBeenCalled();
    });

    it('reabre cadastro com sucesso', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();

        await wrapper.find('[data-testid="btn-reabrir-cadastro"]').trigger('click');
        await (wrapper.vm as any).$nextTick();

        expect((wrapper.vm as any).tipoReabertura).toBe('cadastro');
        expect((wrapper.vm as any).modals.modals.reabrir.value.isOpen).toBe(true);

        const textarea = wrapper.find('textarea');
        await textarea.setValue('Erro no preenchimento');

        const btn = wrapper.find('[data-testid="btn-confirmar-reabrir"]');
        await btn.trigger('click');
        await flushPromises();

        expect(fluxoSubprocessoMock.reabrirCadastro).toHaveBeenCalledWith(10, 'Erro no preenchimento');
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

        expect(fluxoSubprocessoMock.reabrirRevisaoCadastro).toHaveBeenCalledWith(10, 'Revisão incompleta');
    });

    it('impede reabertura se justificativa vazia (botão desabilitado)', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();

        await wrapper.find('[data-testid="btn-reabrir-cadastro"]').trigger('click');

        // O botão deve estar desabilitado se a justificativa for vazia
        const btn = wrapper.find('[data-testid="btn-confirmar-reabrir"]');
        expect(btn.attributes('disabled')).toBeDefined();
        expect(fluxoSubprocessoMock.reabrirCadastro).not.toHaveBeenCalled();
    });

    it('trata erro na API ao reabrir', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        fluxoSubprocessoMock.reabrirCadastro.mockResolvedValue(false);

        await wrapper.find('[data-testid="btn-reabrir-cadastro"]').trigger('click');

        const textarea = wrapper.find('textarea');
        await textarea.setValue('Justificativa');

        const btn = wrapper.find('[data-testid="btn-confirmar-reabrir"]');
        await btn.trigger('click');
        await flushPromises();

        expect(fluxoSubprocessoMock.reabrirCadastro).toHaveBeenCalled();
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

    it('deve gerenciar notificações de erro, alertas de sistema e estados de modais de confirmação', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        const vm = wrapper.vm as any;

        // Gerenciamento de notificações
        vm.notify("Msg", "info");
        await vm.$nextTick();
        const appAlert = wrapper.findComponent({name: 'AppAlert'});
        if (appAlert.exists()) await appAlert.vm.$emit('dismissed');

        // Gerenciamento de erros de subprocesso
        subprocessosMock.subprocessoDetalhe = null;
        subprocessosMock.lastError = { message: "Erro" };
        await vm.$nextTick();
        const bAlert = wrapper.findComponent({name: 'BAlert'});
        if (bAlert.exists()) await bAlert.vm.$emit('dismissed');
        expect(subprocessosMock.clearError).toHaveBeenCalled();

        // Atualização de estados de v-model nos modais
        const modalsComp = wrapper.findAllComponents({name: 'ModalConfirmacao'});
        for (const modal of modalsComp) {
            await modal.vm.$emit('update:modelValue', true);
        }
        expect(vm.modalLembreteAberto).toBe(true);

        // Validação de entrada nula para alteração de data limite
        expect(await vm.confirmarAlteracaoDataLimite(null)).toBeUndefined();
        
        // Exibição de toast pendente ao montar componente
        const {useToastStore} = await import("@/stores/toast");
        const toastStore = useToastStore();
        toastStore.setPending("Msg");
        mountComponent();
    });
});
