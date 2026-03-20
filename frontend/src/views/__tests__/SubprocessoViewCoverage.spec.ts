import {beforeEach, describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import SubprocessoView from '@/views/SubprocessoView.vue';
import * as useSubprocessosModule from '@/composables/useSubprocessos';
import {BSpinner} from 'bootstrap-vue-next';
import * as useAcessoModule from '@/composables/useAcesso';
import {reactive} from 'vue';

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

vi.mock('@/composables/useFluxoSubprocesso', () => ({
    useFluxoSubprocesso: () => fluxoSubprocessoMock
}));
vi.mock('@/composables/useSubprocessos', () => ({useSubprocessos: () => subprocessosMock}));

const SubprocessoHeaderStub = {template: '<div />'};
const SubprocessoCardsStub = {template: '<div />'};
const SubprocessoModalStub = {template: '<div />'};
const TabelaMovimentacoesStub = {template: '<div />'};
const ModalConfirmacaoStub = {
    template: '<div><slot /></div>',
    props: ['modelValue', 'titulo', 'loading', 'okDisabled'],
    emits: ['update:modelValue', 'confirmar']
};
const BAlertStub = {
    template: '<div><slot /></div>',
    props: ['modelValue', 'variant', 'dismissible'],
    emits: ['dismissed']
};

describe('SubprocessoView Coverage', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        fluxoSubprocessoMock.alterarDataLimiteSubprocesso.mockResolvedValue({});
        fluxoSubprocessoMock.reabrirCadastro.mockResolvedValue(true);
        fluxoSubprocessoMock.reabrirRevisaoCadastro.mockResolvedValue(true);
        subprocessosMock.subprocessoDetalhe = null;
        subprocessosMock.buscarSubprocessoPorProcessoEUnidade = vi.fn().mockResolvedValue(123);
        subprocessosMock.buscarSubprocessoDetalhe = vi.fn();
        subprocessosMock.buscarContextoEdicao = vi.fn();
        subprocessosMock.atualizarStatusLocal = vi.fn();
        subprocessosMock.lastError = null;
    });

    it('renders loading state when no data and no error', () => {
        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeAlterarDataLimite: {value: false},
            podeReabrirCadastro: {value: false},
            podeReabrirRevisao: {value: false},
            podeEnviarLembrete: {value: false},
            podeDisponibilizarCadastro: {value: false},
            podeEditarCadastro: {value: false},
            podeVisualizarMapa: {value: true},
        } as any);

        const pinia = createTestingPinia({createSpy: vi.fn});

        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeVisualizarMapa: {value: true},
        } as any);

        const wrapper = mount(SubprocessoView, {
            global: {
                plugins: [pinia],
                stubs: {
                    SubprocessoHeader: SubprocessoHeaderStub,
                    SubprocessoCards: SubprocessoCardsStub,
                    TabelaMovimentacoes: TabelaMovimentacoesStub,
                    SubprocessoModal: SubprocessoModalStub,
                    ModalConfirmacao: ModalConfirmacaoStub,
                    BSpinner: { template: '<div><slot /></div>' },
                    BAlert: BAlertStub,
                }
            },
            props: {
                codProcesso: 1,
                siglaUnidade: 'TEST'
            }
        });

        expect(wrapper.findComponent(BSpinner).exists()).toBe(true);
        expect(wrapper.text()).toContain('Carregando informações da unidade...');
    });

    it('renders error state when lastError is present', () => {
        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeAlterarDataLimite: {value: false},
            podeReabrirCadastro: {value: false},
            podeReabrirRevisao: {value: false},
            podeEnviarLembrete: {value: false},
            podeDisponibilizarCadastro: {value: false},
            podeEditarCadastro: {value: false},
            podeVisualizarMapa: {value: true},
        } as any);

        const pinia = createTestingPinia({createSpy: vi.fn});
        subprocessosMock.lastError = {message: 'Erro teste'};

        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeVisualizarMapa: {value: true},
        } as any);

        const wrapper = mount(SubprocessoView, {
            global: {
                plugins: [pinia],
                stubs: {
                    SubprocessoHeader: SubprocessoHeaderStub,
                    SubprocessoCards: SubprocessoCardsStub,
                    TabelaMovimentacoes: TabelaMovimentacoesStub,
                    SubprocessoModal: SubprocessoModalStub,
                    ModalConfirmacao: ModalConfirmacaoStub,
                    BSpinner: { template: '<div><slot /></div>' },
                    BAlert: BAlertStub,
                }
            },
            props: {
                codProcesso: 1,
                siglaUnidade: 'TEST'
            }
        });

        expect(wrapper.text()).toContain('Erro teste');
    });

    it('confirmarAlteracaoDataLimite returns early if novaData is empty', async () => {
        const pinia = createTestingPinia({createSpy: vi.fn});
        subprocessosMock.subprocessoDetalhe = {
            unidade: {codigo: 1, sigla: 'TEST', nome: 'Unidade teste'}
        };
        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeAlterarDataLimite: {value: true},
            podeReabrirCadastro: {value: false},
            podeReabrirRevisao: {value: false},
            podeEnviarLembrete: {value: false},
            podeDisponibilizarCadastro: {value: false},
            podeEditarCadastro: {value: false},
            podeVisualizarMapa: {value: true},
        } as any);

        const wrapper = mount(SubprocessoView, {
            global: {
                plugins: [pinia],
                stubs: {
                    SubprocessoHeader: SubprocessoHeaderStub,
                    SubprocessoCards: SubprocessoCardsStub,
                    TabelaMovimentacoes: TabelaMovimentacoesStub,
                    SubprocessoModal: SubprocessoModalStub,
                    BSpinner: { template: '<div><slot /></div>' },
                    BAlert: BAlertStub,
                }
            },
            props: {codProcesso: 1, siglaUnidade: 'TEST'}
        });

        await (wrapper.vm as any).confirmarAlteracaoDataLimite('');

        expect(fluxoSubprocessoMock.alterarDataLimiteSubprocesso).not.toHaveBeenCalled();
    });

    it('confirmarReabertura returns early if justification is empty', async () => {
        const pinia = createTestingPinia({createSpy: vi.fn});
        const store = subprocessosMock as any;
        store.subprocessoDetalhe = {
            unidade: {codigo: 1, sigla: 'TEST', nome: 'Unidade teste'},
            movimentacoes: [],
        };

        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeAlterarDataLimite: {value: false},
            podeReabrirCadastro: {value: false},
            podeReabrirRevisao: {value: false},
            podeEnviarLembrete: {value: false},
            podeDisponibilizarCadastro: {value: false},
            podeEditarCadastro: {value: false},
            podeVisualizarMapa: {value: true},
        } as any);

        (store.buscarSubprocessoPorProcessoEUnidade as any).mockResolvedValue(123);

        const wrapper = mount(SubprocessoView, {
            global: {
                plugins: [pinia],
                stubs: {
                    SubprocessoHeader: SubprocessoHeaderStub,
                    SubprocessoCards: SubprocessoCardsStub,
                    TabelaMovimentacoes: TabelaMovimentacoesStub,
                    SubprocessoModal: SubprocessoModalStub,
                    BSpinner: { template: '<div><slot /></div>' },
                    BAlert: BAlertStub,
                    BFormTextarea: {template: '<textarea />'}
                }
            },
            props: {codProcesso: 1, siglaUnidade: 'TEST'}
        });

        await (wrapper.vm as any).$nextTick();

        (wrapper.vm as any).justificativaReabertura = '';
        await (wrapper.vm as any).confirmarReabertura();

        expect(fluxoSubprocessoMock.reabrirCadastro).not.toHaveBeenCalled();
    });
});
