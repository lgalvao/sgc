import {beforeEach, describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import {createTestingPinia} from '@pinia/testing';
import SubprocessoView from '@/views/SubprocessoView.vue';
import {BSpinner} from 'bootstrap-vue-next';
import * as useAcessoModule from '@/composables/useAcesso';
import * as processoService from '@/services/processoService';
import {computed, reactive} from 'vue';
import {SituacaoSubprocesso, TipoProcesso, type SubprocessoDetalhe} from '@/types/tipos';

vi.mock('vue-router', () => ({
    useRoute: () => ({params: {codProcesso: '1', siglaUnidade: 'TEST'}, query: {}}),
}));

type ErroStore = {
    message: string;
    details?: string;
};

type SubprocessoViewVm = {
    $nextTick: () => Promise<void>;
    codSubprocesso: number | null;
    justificativaReabertura: string;
    tipoReabertura: 'cadastro' | 'revisao';
    modalLembreteAberto: boolean;
    loadingLembrete: boolean;
    abrirModalAlterarDataLimite: () => void;
    fecharModalAlterarDataLimite: () => void;
    confirmarAlteracaoDataLimite: (novaData: string) => Promise<void>;
    confirmarReabertura: () => Promise<void>;
    confirmarEnviarLembrete: () => Promise<void>;
    enviarLembreteConfirmado: () => Promise<void>;
    rowAttrMovimentacao: (item: { codigo: number } | null) => Record<string, string>;
    formatTipoResponsabilidade: (responsavel: { tipo?: string | null; dataFim?: string | null } | null) => string;
};

type AcessoHook = ReturnType<typeof useAcessoModule.useAcesso>;

function computedMutavel(valorInicial: boolean) {
    let valor = valorInicial;
    return computed({
        get: () => valor,
        set: (novoValor: boolean) => {
            valor = novoValor;
        },
    });
}

function criarAcessoMock(parcial: Partial<AcessoHook> = {}): AcessoHook {
    return {
        podeAlterarDataLimite: computedMutavel(false),
        podeReabrirCadastro: computedMutavel(false),
        podeReabrirRevisao: computedMutavel(false),
        podeEnviarLembrete: computedMutavel(false),
        podeDisponibilizarCadastro: computedMutavel(false),
        podeEditarCadastro: computedMutavel(false),
        habilitarAcessoMapa: computedMutavel(true),
        ...parcial,
    } as AcessoHook;
}

function criarSubprocessoDetalhe(parcial: Partial<SubprocessoDetalhe> = {}): SubprocessoDetalhe {
    const unidade = {codigo: 1, sigla: 'TEST', nome: 'Unidade teste'};

    return {
        codigo: 123,
        unidade,
        titular: null,
        responsavel: null,
        situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
        localizacaoAtual: unidade.sigla,
        processoDescricao: 'Processo teste',
        dataCriacaoProcesso: '2025-01-01',
        ultimaDataLimiteSubprocesso: '2025-01-01',
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        prazoEtapaAtual: '',
        isEmAndamento: true,
        etapaAtual: 1,
        movimentacoes: [],
        elementosProcesso: [],
        permissoes: {
            podeEditarCadastro: false,
            podeDisponibilizarCadastro: false,
            podeDevolverCadastro: false,
            podeAceitarCadastro: false,
            podeHomologarCadastro: false,
            podeEditarMapa: false,
            podeDisponibilizarMapa: false,
            podeValidarMapa: false,
            podeApresentarSugestoes: false,
            podeVerSugestoes: false,
            podeDevolverMapa: false,
            podeAceitarMapa: false,
            podeHomologarMapa: false,
            podeVisualizarImpacto: false,
            podeAlterarDataLimite: false,
            podeReabrirCadastro: false,
            podeReabrirRevisao: false,
            podeEnviarLembrete: false,
            mesmaUnidade: false,
            habilitarAcessoCadastro: false,
            habilitarAcessoMapa: true,
            habilitarEditarCadastro: false,
            habilitarDisponibilizarCadastro: false,
            habilitarDevolverCadastro: false,
            habilitarAceitarCadastro: false,
            habilitarHomologarCadastro: false,
            habilitarEditarMapa: false,
            habilitarDisponibilizarMapa: false,
            habilitarValidarMapa: false,
            habilitarApresentarSugestoes: false,
            habilitarDevolverMapa: false,
            habilitarAceitarMapa: false,
            habilitarHomologarMapa: false,
        },
        ...parcial,
    };
}

const fluxoSubprocessoMock = {
    alterarDataLimiteSubprocesso: vi.fn(),
    reabrirCadastro: vi.fn(),
    reabrirRevisaoCadastro: vi.fn(),
};

const subprocessosMock = reactive<{
    subprocessoDetalhe: SubprocessoDetalhe | null;
    buscarSubprocessoPorProcessoEUnidade: ReturnType<typeof vi.fn>;
    buscarSubprocessoDetalhe: ReturnType<typeof vi.fn>;
    buscarContextoEdicao: ReturnType<typeof vi.fn>;
    atualizarStatusLocal: ReturnType<typeof vi.fn>;
    lastError: ErroStore | null;
    clearError: ReturnType<typeof vi.fn>;
}>({
    subprocessoDetalhe: null,
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarSubprocessoDetalhe: vi.fn(),
    buscarContextoEdicao: vi.fn(),
    atualizarStatusLocal: vi.fn(),
    lastError: null,
    clearError: vi.fn(),
});

vi.mock('@/composables/useFluxoSubprocesso', () => ({
    useFluxoSubprocesso: () => fluxoSubprocessoMock
}));
vi.mock('@/composables/useSubprocessos', () => ({useSubprocessos: () => subprocessosMock}));
vi.mock('@/services/processoService', () => ({
    enviarLembrete: vi.fn(),
}));

const subprocessoStoreCacheMock = {
    garantirContextoEdicao: vi.fn(),
    garantirContextoEdicaoPorProcessoEUnidade: vi.fn(),
    dadosValidos: vi.fn().mockReturnValue(false),
    invalidar: vi.fn(),
};
vi.mock('@/stores/subprocesso', () => ({
    useSubprocessoStore: () => subprocessoStoreCacheMock,
}));

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

function obterVm(wrapper: ReturnType<typeof mount>): SubprocessoViewVm {
    return wrapper.vm as unknown as SubprocessoViewVm;
}

describe('SubprocessoView Coverage', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        fluxoSubprocessoMock.alterarDataLimiteSubprocesso.mockResolvedValue({});
        fluxoSubprocessoMock.reabrirCadastro.mockResolvedValue(true);
        fluxoSubprocessoMock.reabrirRevisaoCadastro.mockResolvedValue(true);
        subprocessosMock.subprocessoDetalhe = null;
        subprocessosMock.buscarSubprocessoPorProcessoEUnidade.mockResolvedValue(123);
        subprocessosMock.buscarSubprocessoDetalhe = vi.fn();
        subprocessosMock.buscarContextoEdicao = vi.fn().mockImplementation(async () => {
            subprocessosMock.subprocessoDetalhe = criarSubprocessoDetalhe({
                codigo: 123,
                unidade: {codigo: 1, sigla: 'TEST', nome: 'Unidade teste'},
                movimentacoes: [],
            });
            return {
                detalhes: subprocessosMock.subprocessoDetalhe,
                mapa: {codigo: 1},
            };
        });
        subprocessosMock.atualizarStatusLocal = vi.fn();
        subprocessosMock.lastError = null;
        subprocessoStoreCacheMock.garantirContextoEdicaoPorProcessoEUnidade = vi.fn().mockImplementation(async () => {
            subprocessosMock.subprocessoDetalhe = criarSubprocessoDetalhe({
                codigo: 123,
                unidade: {codigo: 1, sigla: 'TEST', nome: 'Unidade teste'},
                movimentacoes: [],
            });
            return {
                codigo: 123,
                contexto: {
                    detalhes: subprocessosMock.subprocessoDetalhe,
                    mapa: {codigo: 1},
                    unidade: {codigo: 1, sigla: 'TEST', nome: 'Unidade teste'},
                    atividadesDisponiveis: [],
                    subprocesso: null,
                },
            };
        });
        subprocessoStoreCacheMock.dadosValidos.mockReturnValue(false);
        subprocessoStoreCacheMock.invalidar = vi.fn();
    });

    it('renders loading state when no data and no error', () => {
        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue(criarAcessoMock());

        const pinia = createTestingPinia({createSpy: vi.fn});

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
        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue(criarAcessoMock());

        const pinia = createTestingPinia({createSpy: vi.fn});
        subprocessosMock.lastError = {message: 'Erro teste'};

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
        subprocessosMock.subprocessoDetalhe = criarSubprocessoDetalhe({
            unidade: {codigo: 1, sigla: 'TEST', nome: 'Unidade teste'}
        });
        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue(criarAcessoMock({
            podeAlterarDataLimite: computedMutavel(true),
        }));

        const wrapper = mount(SubprocessoView, {
            global: {
                plugins: [pinia],
                stubs
            },
            props: {codProcesso: 1, siglaUnidade: 'TEST'}
        });

        await obterVm(wrapper).confirmarAlteracaoDataLimite('');

        expect(fluxoSubprocessoMock.alterarDataLimiteSubprocesso).not.toHaveBeenCalled();
    });

    it('confirmarReabertura coverage', async () => {
        const pinia = createTestingPinia({createSpy: vi.fn});
        subprocessosMock.subprocessoDetalhe = criarSubprocessoDetalhe({
            unidade: {codigo: 1, sigla: 'TEST', nome: 'Unidade teste'},
            movimentacoes: [],
        });

        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue(criarAcessoMock({
            podeReabrirCadastro: computedMutavel(true),
        }));

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

        const vm = obterVm(wrapper);
        await vm.$nextTick();
        vm.codSubprocesso = 123;

        vm.justificativaReabertura = '';
        await vm.confirmarReabertura();
        expect(fluxoSubprocessoMock.reabrirCadastro).not.toHaveBeenCalled();

        vm.justificativaReabertura = 'Justificativa';
        vm.tipoReabertura = 'cadastro';
        fluxoSubprocessoMock.reabrirCadastro.mockResolvedValueOnce(true);
        await vm.confirmarReabertura();
        expect(fluxoSubprocessoMock.reabrirCadastro).toHaveBeenCalledWith(123, 'Justificativa');

        vm.justificativaReabertura = 'Justificativa';
        vm.tipoReabertura = 'revisao';
        fluxoSubprocessoMock.reabrirRevisaoCadastro.mockResolvedValueOnce(true);
        await vm.confirmarReabertura();
        expect(fluxoSubprocessoMock.reabrirRevisaoCadastro).toHaveBeenCalledWith(123, 'Justificativa');

        fluxoSubprocessoMock.reabrirCadastro.mockResolvedValueOnce(false);
        vm.tipoReabertura = 'cadastro';
        await vm.confirmarReabertura();
    });

    it('abrirModalAlterarDataLimite and confirmarAlteracaoDataLimite coverage', async () => {
        const pinia = createTestingPinia({createSpy: vi.fn});

        subprocessoStoreCacheMock.garantirContextoEdicaoPorProcessoEUnidade.mockImplementation(async () => {
            subprocessosMock.subprocessoDetalhe = criarSubprocessoDetalhe({
                codigo: 123,
                unidade: {codigo: 1, sigla: 'TEST', nome: 'Unidade teste'},
                prazoEtapaAtual: '2025-01-01'
            });
            return {
                codigo: 123,
                contexto: {
                    detalhes: subprocessosMock.subprocessoDetalhe,
                    mapa: {codigo: 1},
                    unidade: {codigo: 1, sigla: 'TEST'},
                    atividadesDisponiveis: [],
                    subprocesso: null,
                },
            };
        });

        const acesso = {
            podeAlterarDataLimite: computedMutavel(false),
            habilitarAcessoMapa: computedMutavel(true),
        } satisfies Partial<AcessoHook>;
        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue(criarAcessoMock(acesso));

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

        const vm = obterVm(wrapper);

        vm.abrirModalAlterarDataLimite();

        acesso.podeAlterarDataLimite!.value = true;
        vm.abrirModalAlterarDataLimite();

        vm.fecharModalAlterarDataLimite();

        await vm.confirmarAlteracaoDataLimite('');
        expect(fluxoSubprocessoMock.alterarDataLimiteSubprocesso).not.toHaveBeenCalled();

        await vm.confirmarAlteracaoDataLimite('2025-02-02');
        expect(fluxoSubprocessoMock.alterarDataLimiteSubprocesso).toHaveBeenCalled();

        fluxoSubprocessoMock.alterarDataLimiteSubprocesso.mockRejectedValueOnce(new Error('Fail'));
        await vm.confirmarAlteracaoDataLimite('2025-02-02');
    });

    it('confirmarEnviarLembrete and enviarLembreteConfirmado coverage', async () => {
        const pinia = createTestingPinia({createSpy: vi.fn});
        vi.mocked(processoService.enviarLembrete).mockResolvedValue(undefined as never);

        subprocessosMock.buscarSubprocessoDetalhe.mockImplementation(async () => {
            subprocessosMock.subprocessoDetalhe = criarSubprocessoDetalhe({
                codigo: 123,
                unidade: {codigo: 1, sigla: 'TEST', nome: 'Unidade teste'},
                situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO
            });
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

        const vm = obterVm(wrapper);
        await vm.$nextTick();
        vm.codSubprocesso = 123;

        await vm.confirmarEnviarLembrete();
        expect(vm.modalLembreteAberto).toBe(true);

        await vm.enviarLembreteConfirmado();
        expect(processoService.enviarLembrete).toHaveBeenCalled();
        expect(vm.modalLembreteAberto).toBe(false);

        vi.mocked(processoService.enviarLembrete).mockRejectedValueOnce(new Error('Fail'));
        await vm.enviarLembreteConfirmado();

        vm.codSubprocesso = null;
        await vm.enviarLembreteConfirmado();

        subprocessosMock.subprocessoDetalhe = null;
        await vm.confirmarEnviarLembrete();
        await vm.enviarLembreteConfirmado();
    });

    it('ignora envios repetidos de lembrete enquanto a operação está em andamento', async () => {
        const pinia = createTestingPinia({createSpy: vi.fn});

        subprocessosMock.subprocessoDetalhe = criarSubprocessoDetalhe({
            codigo: 123,
            unidade: {codigo: 1, sigla: 'TEST', nome: 'Unidade teste'},
            situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO
        });

        const wrapper = mount(SubprocessoView, {
            global: {
                plugins: [pinia],
                stubs
            },
            props: {codProcesso: 1, siglaUnidade: 'TEST'}
        });

        const vm = obterVm(wrapper);
        vm.codSubprocesso = 123;

        let resolver!: () => void;
        vi.mocked(processoService.enviarLembrete).mockImplementation(() => new Promise<void>((resolve) => {
            resolver = resolve;
        }) as never);

        const primeiraExecucao = vm.enviarLembreteConfirmado();
        const segundaExecucao = vm.enviarLembreteConfirmado();

        expect(processoService.enviarLembrete).toHaveBeenCalledTimes(1);
        expect(vm.loadingLembrete).toBe(true);

        resolver();
        await primeiraExecucao;
        await segundaExecucao;

        expect(vm.loadingLembrete).toBe(false);
    });

    it('covers miscellaneous UI logic', async () => {
        const pinia = createTestingPinia({createSpy: vi.fn});
        subprocessosMock.subprocessoDetalhe = criarSubprocessoDetalhe({
            codigo: 123,
            unidade: {codigo: 1, sigla: 'TEST', nome: 'Unidade'},
            situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
            processoDescricao: 'Processo X',
            prazoEtapaAtual: '2025-01-01',
            titular: {
                codigo: 1,
                nome: 'Titular',
                tituloEleitoral: '1111',
                unidade: {codigo: 1, sigla: 'TEST', nome: 'Unidade'},
                email: 't@t.com',
                ramal: '123'
            },
            responsavel: {
                usuario: {
                    codigo: 2,
                    nome: 'Resp',
                    tituloEleitoral: '2222',
                    unidade: {codigo: 1, sigla: 'TEST', nome: 'Unidade'},
                    email: 'r@r.com',
                    ramal: '456'
                },
                tipo: 'Substituição',
                dataInicio: '2025-01-01',
                dataFim: '2025-12-31'
            },
            movimentacoes: [{
                codigo: 1,
                dataHora: '2025-01-01T10:00:00',
                unidadeOrigemCodigo: 1,
                unidadeOrigemSigla: 'O',
                unidadeOrigemNome: 'Origem',
                unidadeDestinoCodigo: 2,
                unidadeDestinoSigla: 'D',
                unidadeDestinoNome: 'Destino',
                usuarioTitulo: '1111',
                usuarioNome: 'Usuario',
                descricao: 'M'
            }]
        });
        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue(criarAcessoMock({habilitarAcessoMapa: computedMutavel(true)}));

        const wrapper = mount(SubprocessoView, {
            global: { plugins: [pinia], stubs },
            props: { codProcesso: 1, siglaUnidade: 'TEST' }
        });

        const vm = obterVm(wrapper);
        await vm.$nextTick();

        expect(vm.rowAttrMovimentacao(null)).toEqual({});
        expect(vm.rowAttrMovimentacao({codigo: 99})).toEqual({'data-testid': 'row-movimentacao-99'});

        expect(vm.formatTipoResponsabilidade(null)).toBe('');
        expect(vm.formatTipoResponsabilidade({tipo: 'Atribuição temporária', dataFim: '2025-01-01'})).toContain('Atrib. temporária');
        expect(vm.formatTipoResponsabilidade({tipo: 'Titular'})).toBe('Titular');

        subprocessoStoreCacheMock.garantirContextoEdicaoPorProcessoEUnidade.mockResolvedValueOnce(null);

        mount(SubprocessoView, {
            global: { plugins: [pinia], stubs },
            props: { codProcesso: 1, siglaUnidade: 'TEST' }
        });
    });
});
