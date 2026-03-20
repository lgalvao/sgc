import {createPinia, setActivePinia} from 'pinia';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {useMapas} from '@/composables/useMapas';
import {useSubprocessos} from '@/composables/useSubprocessos';
import {SituacaoSubprocesso} from '@/types/tipos';
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

describe('Subprocessos store', () => {
    let store: ReturnType<typeof useSubprocessos>;

    const mockPerfilStore = {
        perfilSelecionado: null as string | null,
        unidadeSelecionada: null as number | null,
        perfisUnidades: [] as any[],
        unidadeAtual: null as number | null,
    };
    const mockMapasStore = {
        mapaCompleto: null,
    };

    beforeEach(() => {
        setActivePinia(createPinia());

        vi.clearAllMocks();

        (usePerfilStore as any).mockReturnValue(mockPerfilStore);
        (useMapas as any).mockReturnValue(mockMapasStore);

        store = useSubprocessos();
    });

    describe('buscarSubprocessoDetalhe', () => {
        it('deve limpar o estado anterior antes de buscar novo detalhe', async () => {
            store.subprocessoDetalhe = {codigo: 1} as any;
            (buscarSubprocessoDetalhe as any).mockReturnValue(new Promise(() => {
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
            mockPerfilStore.perfilSelecionado = 'ADMIN' as any;
            mockPerfilStore.unidadeAtual = null;
            (buscarSubprocessoDetalhe as any).mockResolvedValue({codigo: 1, situacao: 'CRIADO'});

            await store.buscarSubprocessoDetalhe(1);

            expect(buscarSubprocessoDetalhe).toHaveBeenCalledWith(1, 'ADMIN', null);
            expect(store.subprocessoDetalhe).toMatchObject({codigo: 1, situacao: 'CRIADO'});
            expect(store.lastError).toBeNull();
        });

        it('deve buscar com sucesso para SERVIDOR com unidade selecionada', async () => {
            mockPerfilStore.perfilSelecionado = 'SERVIDOR' as any;
            mockPerfilStore.unidadeSelecionada = 10;
            mockPerfilStore.perfisUnidades = [{perfil: 'SERVIDOR', unidade: {codigo: 10}}] as any;
            mockPerfilStore.unidadeAtual = 10;

            (buscarSubprocessoDetalhe as any).mockResolvedValue({codigo: 1, situacao: 'CRIADO'});

            await store.buscarSubprocessoDetalhe(1);

            expect(buscarSubprocessoDetalhe).toHaveBeenCalledWith(1, 'SERVIDOR', 10);
        });

        it('deve lidar com erro do serviço', async () => {
            mockPerfilStore.perfilSelecionado = 'ADMIN' as any;
            (buscarSubprocessoDetalhe as any).mockRejectedValue(new Error('Erro backend'));

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
            (buscarSubprocessoPorProcessoEUnidade as any).mockResolvedValue({codigo: 123});

            const result = await store.buscarSubprocessoPorProcessoEUnidade(1, 'SESEL');

            expect(result).toBe(123);
            expect(store.lastError).toBeNull();
        });

        it('deve retornar null em caso de erro', async () => {
            (buscarSubprocessoPorProcessoEUnidade as any).mockRejectedValue(new Error('Não encontrado'));

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
            store.subprocessoDetalhe = {codigo: 1} as any;
            (buscarContextoEdicao as any).mockReturnValue(new Promise(() => {
            }));

            store.buscarContextoEdicao(2);

            expect(store.subprocessoDetalhe).toBeNull();
        });

        it('deve popular stores relacionados com dados retornados', async () => {
            mockPerfilStore.perfilSelecionado = 'ADMIN' as any;
            const mockData = {
                detalhes: {codigo: 1},
                unidade: {codigo: 10, nome: 'Teste'},
                mapa: {id: 5},
                atividadesDisponiveis: [{id: 100}]
            };
            (buscarContextoEdicao as any).mockResolvedValue(mockData);

            await store.buscarContextoEdicao(1);

            expect(store.subprocessoDetalhe).toMatchObject(mockData.detalhes);
            expect(mockMapasStore.mapaCompleto).toEqual(mockData.mapa);
        });

        it('deve falhar se não houver perfil', async () => {
            mockPerfilStore.perfilSelecionado = null;
            await store.buscarContextoEdicao(1);
            expect(store.lastError).toBeTruthy();
            expect(buscarContextoEdicao).not.toHaveBeenCalled();
        });

        it('deve lidar com erro do serviço', async () => {
            mockPerfilStore.perfilSelecionado = 'ADMIN' as any;
            (buscarContextoEdicao as any).mockRejectedValue(new Error("Fail"));
            await expect(store.buscarContextoEdicao(1)).rejects.toThrow("Fail");
            expect(store.lastError).toBeTruthy();
        });
    });

    describe('atualizarStatusLocal', () => {
        it('deve atualizar o status se houver detalhe carregado', () => {
            store.subprocessoDetalhe = {situacao: 'CRIADO'} as any;
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
