import {createPinia, setActivePinia} from 'pinia';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {useMapas} from '@/composables/useMapas';
import {useSubprocessos} from '@/composables/useSubprocessos';
import type {ContextoEdicaoSubprocesso, PermissoesSubprocesso, SubprocessoDetalhe, SubprocessoDetalheResponse} from '@/types/tipos';
import {SituacaoSubprocesso, TipoProcesso} from '@/types/tipos';
import {usePerfilStore} from '../perfil';
import {
    buscarContextoEdicao,
    buscarSubprocessoDetalhe,
    buscarSubprocessoPorProcessoEUnidade,
} from '@/services/subprocessoService';
import logger from '@/utils/logger';

vi.mock('@/services/subprocessoService', () => ({
    buscarContextoEdicao: vi.fn(),
    buscarSubprocessoDetalhe: vi.fn(),
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
}));
vi.mock('../perfil', () => ({
    usePerfilStore: vi.fn(),
}));
vi.mock('@/composables/useMapas', () => ({
    useMapas: vi.fn(),
}));

const {mockApiClient} = vi.hoisted(() => ({
    mockApiClient: {
        post: vi.fn().mockResolvedValue({data: {}}),
        get: vi.fn().mockResolvedValue({data: {}})
    }
}));

vi.mock('@/axios-setup', () => ({
    default: mockApiClient,
    apiClient: mockApiClient
}));

function criarPermissoes(parciais: Partial<PermissoesSubprocesso> = {}): PermissoesSubprocesso {
    return {
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
        habilitarAcessoMapa: false,
        ...parciais,
    };
}

function criarDetalhe(parciais: Partial<SubprocessoDetalhe> = {}): SubprocessoDetalhe {
    return {
        codigo: 1,
        unidade: {codigo: 10, nome: 'Unidade', sigla: 'UND'},
        titular: null,
        responsavel: null,
        situacao: SituacaoSubprocesso.NAO_INICIADO,
        localizacaoAtual: 'UND',
        processoDescricao: 'Processo',
        dataCriacaoProcesso: '2024-01-01T00:00:00',
        ultimaDataLimiteSubprocesso: '2025-01-01T00:00:00',
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        prazoEtapaAtual: '2025-01-01T00:00:00',
        isEmAndamento: true,
        etapaAtual: 1,
        movimentacoes: [],
        elementosProcesso: [],
        permissoes: criarPermissoes(),
        ...parciais,
    };
}

function criarRespostaDetalhe(parciais: Partial<SubprocessoDetalheResponse> = {}): SubprocessoDetalheResponse {
    return {
        subprocesso: {
            codigo: 1,
            situacao: SituacaoSubprocesso.NAO_INICIADO,
            unidade: {codigo: 10, nome: 'Unidade', sigla: 'UND'},
            dataLimiteEtapa1: '2025-01-01T00:00:00',
            dataFimEtapa1: null,
            dataLimiteEtapa2: null,
            dataFimEtapa2: null,
            processoDescricao: 'Processo',
            dataCriacaoProcesso: '2024-01-01T00:00:00',
            tipoProcesso: TipoProcesso.MAPEAMENTO,
            isEmAndamento: true,
            etapaAtual: 1,
        },
        titular: null,
        responsavel: null,
        localizacaoAtual: 'UND',
        movimentacoes: [],
        permissoes: criarPermissoes(),
        ...parciais,
    };
}

function criarContexto(parciais: Partial<ContextoEdicaoSubprocesso> = {}): ContextoEdicaoSubprocesso {
    return {
        unidade: {codigo: 10, nome: 'Teste', sigla: 'UND'},
        subprocesso: {
            codigo: 1,
            unidade: {codigo: 10, nome: 'Teste', sigla: 'UND'},
            situacao: SituacaoSubprocesso.NAO_INICIADO,
            dataLimite: '2025-01-01T00:00:00',
            dataFimEtapa1: '',
            dataLimiteEtapa2: '',
            atividades: [],
            codUnidade: 10,
        },
        detalhes: criarDetalhe(),
        mapa: {codigo: 5} as ContextoEdicaoSubprocesso['mapa'],
        atividadesDisponiveis: [],
        ...parciais,
    };
}

describe('Subprocessos store', () => {
    let store: ReturnType<typeof useSubprocessos>;

    const mockPerfilStore = {
        perfilSelecionado: null as string | null,
        unidadeSelecionada: null as number | null,
        perfisUnidades: [] as Array<{perfil: string; unidade: {codigo: number}}>,
        unidadeAtual: null as number | null,
    };
    const mockMapasStore = {
        mapaCompleto: {value: null as ContextoEdicaoSubprocesso['mapa'] | null},
    };

    beforeEach(() => {
        setActivePinia(createPinia());

        vi.clearAllMocks();

        vi.mocked(usePerfilStore).mockReturnValue(mockPerfilStore as never);
        vi.mocked(useMapas).mockReturnValue(mockMapasStore as never);

        store = useSubprocessos();
    });

    describe('buscarSubprocessoDetalhe', () => {
        it('deve limpar o estado anterior antes de buscar novo detalhe', async () => {
            store.subprocessoDetalhe = criarDetalhe();
            vi.mocked(buscarSubprocessoDetalhe).mockReturnValue(new Promise(() => {
            }));

            store.buscarSubprocessoDetalhe(2);

            expect(store.subprocessoDetalhe).toBeNull();
        });

        it('deve falhar se não houver perfil selecionado', async () => {
            mockPerfilStore.perfilSelecionado = null;

            await store.buscarSubprocessoDetalhe(1);

            expect(store.lastError).toBeTruthy();
            expect(store.lastError?.message).toBe("Informações de perfil ou unidade não disponíveis.");
            expect(buscarSubprocessoDetalhe).not.toHaveBeenCalled();
        });

        it('deve buscar com sucesso para ADMIN (global)', async () => {
            mockPerfilStore.perfilSelecionado = 'ADMIN';
            mockPerfilStore.unidadeAtual = null;
            vi.mocked(buscarSubprocessoDetalhe).mockResolvedValue(
                criarRespostaDetalhe({subprocesso: {...criarRespostaDetalhe().subprocesso, situacao: SituacaoSubprocesso.NAO_INICIADO}})
            );

            await store.buscarSubprocessoDetalhe(1);

            expect(buscarSubprocessoDetalhe).toHaveBeenCalledWith(1, 'ADMIN', null);
            expect(store.subprocessoDetalhe).toMatchObject({codigo: 1, situacao: SituacaoSubprocesso.NAO_INICIADO});
            expect(store.lastError).toBeNull();
        });

        it('deve buscar com sucesso para SERVIDOR com unidade selecionada', async () => {
            mockPerfilStore.perfilSelecionado = 'SERVIDOR';
            mockPerfilStore.unidadeSelecionada = 10;
            mockPerfilStore.perfisUnidades = [{perfil: 'SERVIDOR', unidade: {codigo: 10}}];
            mockPerfilStore.unidadeAtual = 10;

            vi.mocked(buscarSubprocessoDetalhe).mockResolvedValue(criarRespostaDetalhe());

            await store.buscarSubprocessoDetalhe(1);

            expect(buscarSubprocessoDetalhe).toHaveBeenCalledWith(1, 'SERVIDOR', 10);
        });

        it('deve lidar com erro do serviço', async () => {
            mockPerfilStore.perfilSelecionado = 'ADMIN';
            vi.mocked(buscarSubprocessoDetalhe).mockRejectedValue(new Error('Erro backend'));

            await expect(store.buscarSubprocessoDetalhe(1)).rejects.toThrow('Erro backend');

            expect(store.subprocessoDetalhe).toBeNull();
            expect(store.lastError).toBeTruthy();
            expect(logger.error).toHaveBeenCalledWith(
                expect.stringContaining("Erro ao buscar detalhes"),
                expect.objectContaining({message: "Erro backend"})
            );
        });
    });

    describe('buscarSubprocessoPorProcessoEUnidade', () => {
        it('deve retornar codigo em caso de sucesso', async () => {
            vi.mocked(buscarSubprocessoPorProcessoEUnidade).mockResolvedValue({codigo: 123});

            const result = await store.buscarSubprocessoPorProcessoEUnidade(1, 'SESEL');

            expect(result).toBe(123);
            expect(store.lastError).toBeNull();
        });

        it('deve retornar null em caso de erro', async () => {
            vi.mocked(buscarSubprocessoPorProcessoEUnidade).mockRejectedValue(new Error('Não encontrado'));

            const result = await store.buscarSubprocessoPorProcessoEUnidade(1, 'SESEL');

            expect(result).toBeNull();
            expect(store.lastError).toBeTruthy();
            expect(logger.error).toHaveBeenCalledWith(
                expect.stringContaining("Erro ao buscar ID"),
                expect.any(Error) // Here it catches the original error because it's a try/catch block, not withErrorHandling callback
            );
        });
    });

    describe('buscarContextoEdicao', () => {
        it('deve limpar o estado anterior antes de buscar novo contexto', async () => {
            store.subprocessoDetalhe = criarDetalhe();
            vi.mocked(buscarContextoEdicao).mockReturnValue(new Promise(() => {
            }));

            store.buscarContextoEdicao(2);

            expect(store.subprocessoDetalhe).toBeNull();
        });

        it('deve popular stores relacionados com dados retornados', async () => {
            mockPerfilStore.perfilSelecionado = 'ADMIN';
            const mockData = criarContexto({
                detalhes: criarDetalhe({codigo: 1, localizacaoAtual: 'UND'}),
                atividadesDisponiveis: [{codigo: 100, descricao: 'Atividade', conhecimentos: []}],
            });
            vi.mocked(buscarContextoEdicao).mockResolvedValue(mockData);

            await store.buscarContextoEdicao(1);

            expect(store.subprocessoDetalhe).toMatchObject(mockData.detalhes);
        });

        it('deve falhar se não houver perfil', async () => {
            mockPerfilStore.perfilSelecionado = null;
            await store.buscarContextoEdicao(1);
            expect(store.lastError).toBeTruthy();
            expect(buscarContextoEdicao).not.toHaveBeenCalled();
        });

        it('deve lidar com erro do serviço', async () => {
            mockPerfilStore.perfilSelecionado = 'ADMIN';
            vi.mocked(buscarContextoEdicao).mockRejectedValue(new Error("Fail"));
            await expect(store.buscarContextoEdicao(1)).rejects.toThrow("Fail");
            expect(store.lastError).toBeTruthy();
        });
    });

    describe('atualizarStatusLocal', () => {
        it('deve atualizar o status se houver detalhe carregado', () => {
            store.subprocessoDetalhe = criarDetalhe();
            store.atualizarStatusLocal({codigo: 1, situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO});
            expect(store.subprocessoDetalhe?.situacao).toBe(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        });

        it('não deve fazer nada se não houver detalhe carregado', () => {
            store.subprocessoDetalhe = null;
            store.atualizarStatusLocal({codigo: 1, situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO});
            expect(store.subprocessoDetalhe).toBeNull();
        });
    });
});
