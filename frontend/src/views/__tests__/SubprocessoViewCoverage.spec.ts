import {beforeEach, describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import SubprocessoView from '@/views/SubprocessoView.vue';
import {BSpinner} from 'bootstrap-vue-next';
import * as useAcessoModule from '@/composables/useAcesso';
import * as useProcessosModule from '@/composables/useProcessos';
import {reactive, ref} from 'vue';

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
vi.mock('@/composables/useProcessos', () => ({useProcessos: vi.fn()}));

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

const stubs = {
    SubprocessoHeader: SubprocessoHeaderStub,
    SubprocessoCards: SubprocessoCardsStub,
    TabelaMovimentacoes: TabelaMovimentacoesStub,
    SubprocessoModal: SubprocessoModalStub,
    ModalConfirmacao: ModalConfirmacaoStub,
    BSpinner: {template: '<div><slot /></div>'},
    BAlert: BAlertStub,
};

describe('SubprocessoView Coverage', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        fluxoSubprocessoMock.alterarDataLimiteSubprocesso.mockResolvedValue({});
        fluxoSubprocessoMock.reabrirCadastro.mockResolvedValue(true);
        fluxoSubprocessoMock.reabrirRevisaoCadastro.mockResolvedValue(true);
        subprocessosMock.subprocessoDetalhe = null;
        subprocessosMock.buscarSubprocessoPorProcessoEUnidade.mockResolvedValue(123);
        subprocessosMock.buscarSubprocessoDetalhe = vi.fn();
        subprocessosMock.buscarContextoEdicao = vi.fn();
        subprocessosMock.atualizarStatusLocal = vi.fn();
        subprocessosMock.lastError = null;

        vi.mocked(useProcessosModule.useProcessos).mockReturnValue({
            processoDetalhe: { value: null },
            enviarLembrete: vi.fn().mockResolvedValue(undefined)
        } as any);
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
                stubs
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
                stubs
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
                stubs
            },
            props: {codProcesso: 1, siglaUnidade: 'TEST'}
        });

        await (wrapper.vm as any).confirmarAlteracaoDataLimite('');

        expect(fluxoSubprocessoMock.alterarDataLimiteSubprocesso).not.toHaveBeenCalled();
    });

    it('confirmarReabertura coverage', async () => {
        const pinia = createTestingPinia({createSpy: vi.fn});
        subprocessosMock.subprocessoDetalhe = {
            unidade: {codigo: 1, sigla: 'TEST', nome: 'Unidade teste'},
            movimentacoes: [],
        };

        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeReabrirCadastro: {value: true},
            podeVisualizarMapa: {value: true},
        } as any);

        const wrapper = mount(SubprocessoView, {
            global: {
                plugins: [pinia],
                stubs: {
                    ...stubs,
                    BFormTextarea: {template: '<textarea />'}
                }
            },
            props: {codProcesso: 1, siglaUnidade: 'TEST'}
        });

        const vm = wrapper.vm as any;
        await vm.$nextTick();
        vm.codSubprocesso = 123;

        // Empty justification
        vm.justificativaReabertura = '';
        await vm.confirmarReabertura();
        expect(fluxoSubprocessoMock.reabrirCadastro).not.toHaveBeenCalled();

        // Success Cadastro
        vm.justificativaReabertura = 'Justificativa';
        vm.tipoReabertura = 'cadastro';
        fluxoSubprocessoMock.reabrirCadastro.mockResolvedValueOnce(true);
        await vm.confirmarReabertura();
        expect(fluxoSubprocessoMock.reabrirCadastro).toHaveBeenCalledWith(123, 'Justificativa');

        // Success Revisao
        vm.justificativaReabertura = 'Justificativa';
        vm.tipoReabertura = 'revisao';
        fluxoSubprocessoMock.reabrirRevisaoCadastro.mockResolvedValueOnce(true);
        await vm.confirmarReabertura();
        expect(fluxoSubprocessoMock.reabrirRevisaoCadastro).toHaveBeenCalledWith(123, 'Justificativa');

        // Failure
        fluxoSubprocessoMock.reabrirCadastro.mockResolvedValueOnce(false);
        vm.tipoReabertura = 'cadastro';
        await vm.confirmarReabertura();
    });

    it('abrirModalAlterarDataLimite and confirmarAlteracaoDataLimite coverage', async () => {
        const pinia = createTestingPinia({createSpy: vi.fn});

        subprocessosMock.buscarSubprocessoDetalhe.mockImplementation(async () => {
            subprocessosMock.subprocessoDetalhe = {
                codigo: 123,
                unidade: {codigo: 1, sigla: 'TEST'},
                prazoEtapaAtual: '2025-01-01'
            };
        });

        const acesso = {
            podeAlterarDataLimite: ref(false),
            podeVisualizarMapa: ref(true),
        };
        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue(acesso as any);

        const wrapper = mount(SubprocessoView, {
            global: {
                plugins: [pinia],
                stubs: {
                    ...stubs,
                    SubprocessoModal: { template: '<div></div>', props: ['mostrarModal'] }
                }
            },
            props: {codProcesso: 1, siglaUnidade: 'TEST'}
        });

        const vm = wrapper.vm as any;

        // abrirModalAlterarDataLimite - No permission
        vm.abrirModalAlterarDataLimite();

        // abrirModalAlterarDataLimite - With permission
        acesso.podeAlterarDataLimite.value = true;
        vm.abrirModalAlterarDataLimite();

        // fecharModalAlterarDataLimite
        vm.fecharModalAlterarDataLimite();

        // confirmarAlteracaoDataLimite - Empty data
        await vm.confirmarAlteracaoDataLimite('');
        expect(fluxoSubprocessoMock.alterarDataLimiteSubprocesso).not.toHaveBeenCalled();

        // confirmarAlteracaoDataLimite - Success
        await vm.confirmarAlteracaoDataLimite('2025-02-02');
        expect(fluxoSubprocessoMock.alterarDataLimiteSubprocesso).toHaveBeenCalled();

        // confirmarAlteracaoDataLimite - Failure
        fluxoSubprocessoMock.alterarDataLimiteSubprocesso.mockRejectedValueOnce(new Error('Fail'));
        await vm.confirmarAlteracaoDataLimite('2025-02-02');
    });

    it('confirmarEnviarLembrete and enviarLembreteConfirmado coverage', async () => {
        const pinia = createTestingPinia({createSpy: vi.fn});
        const processosStore = {
            enviarLembrete: vi.fn().mockResolvedValue(undefined),
            processoDetalhe: ref({ situacao: 'EM_ANDAMENTO' })
        };
        vi.mocked(useProcessosModule.useProcessos).mockReturnValue(processosStore as any);

        subprocessosMock.buscarSubprocessoDetalhe.mockImplementation(async () => {
            subprocessosMock.subprocessoDetalhe = {
                codigo: 123,
                unidade: {codigo: 1, sigla: 'TEST'},
                situacao: 'MAPEAMENTO_CADASTRO_DISPONIBILIZADO'
            };
        });

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
            props: {codProcesso: 1, siglaUnidade: 'TEST'}
        });

        const vm = wrapper.vm as any;
        await vm.$nextTick();
        vm.codSubprocesso = 123;

        await vm.confirmarEnviarLembrete();
        expect(vm.modalLembreteAberto).toBe(true);

        await vm.enviarLembreteConfirmado();
        expect(processosStore.enviarLembrete).toHaveBeenCalled();
        expect(vm.modalLembreteAberto).toBe(false);

        processosStore.enviarLembrete.mockRejectedValueOnce(new Error('Fail'));
        await vm.enviarLembreteConfirmado();

        // null case for coverage
        vm.codSubprocesso = null;
        await vm.enviarLembreteConfirmado();

        subprocessosMock.subprocessoDetalhe = null;
        await vm.confirmarEnviarLembrete();
        await vm.enviarLembreteConfirmado();
    });

    it('covers miscellaneous UI logic', async () => {
        const pinia = createTestingPinia({createSpy: vi.fn});
        subprocessosMock.subprocessoDetalhe = {
            codigo: 123,
            unidade: {codigo: 1, sigla: 'TEST', nome: 'Unidade'},
            situacao: 'MAPEAMENTO_CADASTRO_EM_ANDAMENTO',
            processoDescricao: 'Processo X',
            prazoEtapaAtual: '2025-01-01',
            titular: { nome: 'Titular', ramal: '123', email: 't@t.com' },
            responsavel: { usuario: { nome: 'Resp', ramal: '456', email: 'r@r.com' }, tipo: 'Substituição', dataFim: '2025-12-31' },
            movimentacoes: [{ codigo: 1, dataHora: '2025-01-01T10:00:00', unidadeOrigemSigla: 'O', unidadeDestinoSigla: 'D', descricao: 'M' }]
        };
        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({ podeVisualizarMapa: ref(true) } as any);

        const wrapper = mount(SubprocessoView, {
            global: { plugins: [pinia], stubs },
            props: { codProcesso: 1, siglaUnidade: 'TEST' }
        });

        const vm = wrapper.vm as any;
        await vm.$nextTick();

        // rowAttrMovimentacao
        expect(vm.rowAttrMovimentacao(null)).toEqual({});
        expect(vm.rowAttrMovimentacao({codigo: 99})).toEqual({'data-testid': 'row-movimentacao-99'});

        // formatTipoResponsabilidade - empty
        expect(vm.formatTipoResponsabilidade(null)).toBe('');
        // formatTipoResponsabilidade - Atribuição temporária
        expect(vm.formatTipoResponsabilidade({tipo: 'Atribuição temporária', dataFim: '2025-01-01'})).toContain('Atrib. temporária');
        // formatTipoResponsabilidade - Outro
        expect(vm.formatTipoResponsabilidade({tipo: 'Titular'})).toBe('Titular');

        // error path for onMounted
        subprocessosMock.buscarSubprocessoPorProcessoEUnidade.mockRejectedValueOnce({ response: { data: 'Err' } });

        mount(SubprocessoView, {
            global: { plugins: [pinia], stubs },
            props: { codProcesso: 1, siglaUnidade: 'TEST' }
        });

        // branch where id is not found
        subprocessosMock.buscarSubprocessoPorProcessoEUnidade.mockResolvedValueOnce(null);
        mount(SubprocessoView, {
            global: { plugins: [pinia], stubs },
            props: { codProcesso: 1, siglaUnidade: 'TEST' }
        });
    });
});
