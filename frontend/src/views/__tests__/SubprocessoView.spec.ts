import {beforeEach, describe, expect, it, vi} from 'vitest';
import {flushPromises, mount, RouterLinkStub} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import SubprocessoView from '@/views/SubprocessoView.vue';
import {useMapas} from '@/composables/useMapas';
import {reactive} from 'vue';
import {SituacaoSubprocesso, TipoProcesso} from '@/types/tipos';
import * as processoService from '@/services/processoService';
import * as useAcessoModule from '@/composables/useAcesso';

vi.mock('vue-router', () => ({
    useRoute: () => ({params: {codProcesso: '1', siglaUnidade: 'TEST'}, query: {}}),
}));

type SubprocessoViewVm = {
    mostrarModalAlterarDataLimite: boolean;
    mostrarModalReabrir: boolean;
    modalLembreteAberto: boolean;
    tipoReabertura: 'cadastro' | 'revisao' | null;
    notify: (mensagem: string, variante: string) => void;
    confirmarAlteracaoDataLimite: (novaData: string | null) => Promise<void>;
    abrirModalAlterarDataLimite: () => void;
    $nextTick: () => Promise<void>;
};

type SubprocessoDetalheMock = {
    codigo: number;
    situacao: SituacaoSubprocesso;
    situacaoLabel: string;
    processoDescricao: string;
    tipoProcesso: TipoProcesso;
    unidade: {codigo: number; sigla: string; nome: string};
    responsavel: {
        codigo: number;
        nome: string;
        tituloEleitoral: string;
        unidade: {codigo: number; sigla: string; nome: string};
        email: string;
        ramal: string;
    } | null;
    titular: {
        codigo: number;
        nome: string;
        tituloEleitoral: string;
        unidade: {codigo: number; sigla: string; nome: string};
        email: string;
        ramal: string;
    } | null;
    etapaAtual: number;
    prazoEtapaAtual: string;
    localizacaoAtual: string;
    isEmAndamento: boolean;
    elementosProcesso: unknown[];
    movimentacoes: unknown[];
    permissoes?: {podeEditarCadastro?: boolean};
};

const SubprocessoCardsStub = {
    template: '<div data-testid="subprocesso-cards"></div>',
    props: ['situacao', 'tipoProcesso', 'subprocesso']
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

const fluxoSubprocessoMock = {
    alterarDataLimiteSubprocesso: vi.fn(),
    reabrirCadastro: vi.fn(),
    reabrirRevisaoCadastro: vi.fn(),
};
const subprocessosMock = reactive({
    subprocessoDetalhe: null as SubprocessoDetalheMock | null,
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarSubprocessoDetalhe: vi.fn(),
    buscarContextoEdicao: vi.fn(),
    atualizarStatusLocal: vi.fn(),
    lastError: null as {message: string} | null,
    clearError: vi.fn(),
});

vi.mock('@/composables/useFluxoSubprocesso', () => ({
    useFluxoSubprocesso: () => fluxoSubprocessoMock
}));
vi.mock('@/composables/useSubprocessos', () => ({useSubprocessos: () => subprocessosMock}));

// Mock da store de cache de subprocesso (Rodada 2)
const subprocessoStoreCacheMock = {
    garantirContextoEdicao: vi.fn(),
    garantirContextoEdicaoPorProcessoEUnidade: vi.fn(),
    dadosValidos: vi.fn().mockReturnValue(false),
    invalidar: vi.fn(),
};
vi.mock('@/stores/subprocesso', () => ({
    useSubprocessoStore: () => subprocessoStoreCacheMock,
}));

describe('SubprocessoView.vue', () => {
    const mockSubprocesso: SubprocessoDetalheMock = {
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

        movimentacoes: []
    };

    const additionalStubs = {
        SubprocessoCards: SubprocessoCardsStub,
        SubprocessoModal: SubprocessoModalStub,
        ModalConfirmacao: {
            name: 'ModalConfirmacao',
            template: '<div><slot /><button :data-testid="testCodigoConfirmar" :disabled="okDisabled" @click="$emit(\'confirmar\')">OK</button></div>',
            props: ['modelValue', 'titulo', 'testCodigoConfirmar', 'okDisabled'],
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
        fluxoSubprocessoMock.alterarDataLimiteSubprocesso.mockResolvedValue({});
        fluxoSubprocessoMock.reabrirCadastro.mockResolvedValue(true);
        fluxoSubprocessoMock.reabrirRevisaoCadastro.mockResolvedValue(true);
        subprocessosMock.subprocessoDetalhe = null;
        subprocessosMock.buscarSubprocessoPorProcessoEUnidade = vi.fn().mockResolvedValue(10);
        subprocessosMock.buscarSubprocessoDetalhe = vi.fn();
        subprocessosMock.buscarContextoEdicao = vi.fn();
        subprocessosMock.atualizarStatusLocal = vi.fn();
        subprocessosMock.lastError = null;
        // Configura o mock da store de cache
        subprocessoStoreCacheMock.dadosValidos.mockReturnValue(false);
        subprocessoStoreCacheMock.garantirContextoEdicaoPorProcessoEUnidade = vi.fn();
        subprocessoStoreCacheMock.invalidar = vi.fn();
    });

    // Helper to mount component with specific setup
    const mountComponent = (
        overrideMockSubprocesso?: Partial<typeof mockSubprocesso>,
        accessOverrides: Partial<Record<string, unknown>> = {}
    ) => {
        const subprocessoToUse = overrideMockSubprocesso ? {...mockSubprocesso, ...overrideMockSubprocesso} : mockSubprocesso;

        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeAlterarDataLimite: {value: true},
            podeReabrirCadastro: {value: true},
            podeReabrirRevisao: {value: true},
            podeEnviarLembrete: {value: true},
            podeDisponibilizarCadastro: {value: true},
            podeEditarCadastro: {value: true},
            ...accessOverrides
        } as unknown as ReturnType<typeof useAcessoModule.useAcesso>);

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

        subprocessoStoreCacheMock.garantirContextoEdicaoPorProcessoEUnidade.mockImplementation(async () => {
            subprocessosMock.subprocessoDetalhe = subprocessoToUse;
            return {
                codigo: 10,
                contexto: {
                    detalhes: subprocessoToUse,
                    mapa: {codigo: 100},
                    unidade: subprocessoToUse.unidade,
                    atividadesDisponiveis: [],
                    subprocesso: null,
                },
            };
        });
        const mapaStore = useMapas();
        mapaStore.buscarMapaCompleto = vi.fn().mockResolvedValue({});
        mapaStore.mapaCompleto.value = null;

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

        return {wrapper, store: subprocessosMock, mapaStore};
    };

    it('fetches data on mount', async () => {
        const {mapaStore} = mountComponent();
        await flushPromises();

        expect(subprocessoStoreCacheMock.garantirContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledWith(1, 'TEST');
        expect(mapaStore.mapaCompleto.value).toEqual({codigo: 100});
    });

    it('recarrega dados ao reativar a view em keepAlive', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        subprocessoStoreCacheMock.garantirContextoEdicaoPorProcessoEUnidade.mockClear();
        // Simula cache inválido para que o onActivated recarregue
        subprocessoStoreCacheMock.dadosValidos.mockReturnValue(false);

        const hooks = ((wrapper.vm.$ as {a?: Array<() => unknown>} | undefined)?.a) ?? [];
        for (const hook of hooks) {
            await hook.call(wrapper.vm);
        }
        await flushPromises();

        expect(subprocessoStoreCacheMock.garantirContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledWith(1, 'TEST');
    });

    it('mantem orçamento enxuto de chamadas no carregamento inicial quando o contexto já traz mapa', async () => {
        const {mapaStore} = mountComponent();
        await flushPromises();

        // Apenas 1 chamada ao cache (que resolve buscar+contexto internamente)
        expect(subprocessoStoreCacheMock.garantirContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledTimes(1);
        expect(mapaStore.buscarMapaCompleto).not.toHaveBeenCalled();
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
        };

        mountComponent();
        await flushPromises();

        // Após o carregamento, o subprocessoDetalhe deve refletir os dados corretos
        // (mockSubprocesso com codigo: 10), não os dados residuais do estado anterior (codigo: 999)
        expect(subprocessosMock.subprocessoDetalhe).not.toBeNull();
        expect(subprocessosMock.subprocessoDetalhe?.codigo).toBe(10);
    });

    it('substitui permissões stale de ADMIN por permissões de CHEFE ao abrir o subprocesso na mesma sessão', async () => {
        subprocessosMock.subprocessoDetalhe = {
            ...mockSubprocesso,
            codigo: 999,
            situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
            permissoes: {podeEditarCadastro: false}
        };

        const {wrapper} = mountComponent({
            codigo: 10,
            situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
            permissoes: {podeEditarCadastro: true}
        });
        await flushPromises();

        const cards = wrapper.findComponent(SubprocessoCardsStub);
        expect(cards.props('subprocesso')).toEqual(expect.objectContaining({
            codigo: 10,
            permissoes: expect.objectContaining({
                podeEditarCadastro: true
            })
        }));
    });

    it('renders components when data is available', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        const vm = wrapper.vm as unknown as SubprocessoViewVm;
        await vm.$nextTick();

        expect(wrapper.find('[data-testid="header-subprocesso"]').exists()).toBe(true);
        expect(wrapper.findComponent(SubprocessoCardsStub).exists()).toBe(true);
        expect(wrapper.find('[data-testid="tbl-movimentacoes"]').exists()).toBe(true);
    });

    it('passa o subprocesso carregado ao componente de cards', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        const vm = wrapper.vm as unknown as SubprocessoViewVm;
        await vm.$nextTick();

        const cards = wrapper.findComponent(SubprocessoCardsStub);
        expect(cards.props('subprocesso')).toEqual(expect.objectContaining({
            codigo: 10,
            tipoProcesso: TipoProcesso.MAPEAMENTO
        }));
    });

    it('opens date limit modal when allowed', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        const vm = wrapper.vm as unknown as SubprocessoViewVm;
        await vm.$nextTick();

        await wrapper.find('[data-testid="btn-alterar-data-limite"]').trigger('click');
        await vm.$nextTick();

        expect(vm.mostrarModalAlterarDataLimite).toBe(true);
    });

    it('shows error when opening date limit modal is not allowed', async () => {
        const {wrapper} = mountComponent({}, {podeAlterarDataLimite: {value: false}});
        await flushPromises();
        const vm = wrapper.vm as unknown as SubprocessoViewVm;
        await vm.$nextTick();

        vm.abrirModalAlterarDataLimite();
        await vm.$nextTick();

        expect(vm.mostrarModalAlterarDataLimite).toBe(false);
    });

    it('handles date limit update confirmation', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        const vm = wrapper.vm as unknown as SubprocessoViewVm;
        await vm.$nextTick();

        vm.mostrarModalAlterarDataLimite = true;
        await vm.$nextTick();

        const modal = wrapper.findComponent(SubprocessoModalStub);
        await modal.vm.$emit('confirmar-alteracao', '2024-01-01');

        await flushPromises();

        expect(fluxoSubprocessoMock.alterarDataLimiteSubprocesso).toHaveBeenCalledWith(10, {novaData: '2024-01-01'});
        expect(vm.mostrarModalAlterarDataLimite).toBe(false);
    });

    it('trata erro ao alterar data limite', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        fluxoSubprocessoMock.alterarDataLimiteSubprocesso.mockRejectedValue(new Error('Falha'));

        const vm = wrapper.vm as unknown as SubprocessoViewVm;
        vm.mostrarModalAlterarDataLimite = true;
        const modal = wrapper.findComponent(SubprocessoModalStub);
        await modal.vm.$emit('confirmar-alteracao', '2024-01-01');
        await flushPromises();

        expect(fluxoSubprocessoMock.alterarDataLimiteSubprocesso).toHaveBeenCalled();
    });

    it('reabre cadastro com sucesso', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();

        await wrapper.find('[data-testid="btn-reabrir-cadastro"]').trigger('click');
        const vm = wrapper.vm as unknown as SubprocessoViewVm;
        await vm.$nextTick();

        expect(vm.tipoReabertura).toBe('cadastro');
        expect(vm.mostrarModalReabrir).toBe(true);

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
        const vm = wrapper.vm as unknown as SubprocessoViewVm;
        expect(vm.tipoReabertura).toBe('revisao');

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
        const vm = wrapper.vm as unknown as SubprocessoViewVm;
        await vm.$nextTick();

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
        const vm = wrapper.vm as unknown as SubprocessoViewVm;
        await vm.$nextTick();

        const btn = wrapper.find('[data-testid="btn-confirmar-enviar-lembrete"]');
        await btn.trigger('click');
        await flushPromises();

        expect(processoService.enviarLembrete).toHaveBeenCalled();
    });

    it('deve gerenciar notificações de erro, alertas de sistema e estados de modais de confirmação', async () => {
        const {wrapper} = mountComponent();
        await flushPromises();
        const vm = wrapper.vm as unknown as SubprocessoViewVm;

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
