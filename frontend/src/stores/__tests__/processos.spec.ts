import {beforeEach, describe, expect, it, vi, Mocked} from 'vitest';
import {useProcessosStore} from '../processos';
import {SituacaoProcesso, Subprocesso, TipoProcesso} from '@/types/tipos';
import {useNotificacoesStore} from '../notificacoes';
import {SITUACOES_SUBPROCESSO} from '@/constants/situacoes'; // Adicionado
import {initPinia} from '@/test-utils/helpers';

// Mock dos serviços
vi.mock('@/services/painelService');
vi.mock('@/services/processoService');

// Mock dos stores
vi.mock('../unidades', () => ({
    useUnidadesStore: vi.fn(() => ({
        getUnidadeImediataSuperior: vi.fn((unidade: string) => {
            if (unidade === 'SESEL') return 'UNIDADE_SUPERIOR_SESEL';
            if (unidade === 'COSIS') return 'UNIDADE_SUPERIOR_COSIS';
            if (unidade === 'UNIDADE_GESTOR') return 'UNIDADE_SUPERIOR_SESEL';
            return null;
        }),
    })),
}));
vi.mock('../alertas', () => ({
    useAlertasStore: vi.fn(() => ({
        criarAlerta: vi.fn(),
    })),
}));
vi.mock('../notificacoes', () => ({
    useNotificacoesStore: vi.fn(() => ({
        email: vi.fn(),
    })),
}));


describe('useProcessosStore', () => {
    let processosStore: ReturnType<typeof useProcessosStore>;
    let painelService: Mocked<typeof import('@/services/painelService')>;
    let processoService: Mocked<typeof import('@/services/processoService')>;


    beforeEach(async () => {
        initPinia();
        processosStore = useProcessosStore();

        // Importar os módulos mockados dinamicamente
        painelService = (await import('@/services/painelService')) as Mocked<typeof import('@/services/painelService')>;
        processoService = (await import('@/services/processoService')) as Mocked<typeof import('@/services/processoService')>;

        vi.clearAllMocks();

        // Resetar o estado do store
        processosStore.$reset();
    });

    it('should initialize with mock processos and subprocessos with parsed dates', () => {
        expect(processosStore.processosPainel).toEqual([]);
        expect(processosStore.processosPainelPage).toEqual({});
        expect(processosStore.processoDetalhe).toBeNull();
    });

    describe('actions', () => {
        it('fetchProcessosPainel should call painelService and update state', async () => {
            const mockPage = {
                content: [{codigo: 1, descricao: 'Teste', tipo: 'MAPEAMENTO', situacao: 'EM_ANDAMENTO', dataLimite: '2025-12-31', dataCriacao: '2025-01-01', unidadeCodigo: 1, unidadeNome: 'TESTE'}],
                totalPages: 1,
                totalElements: 1,
                number: 0,
                size: 10,
                first: true,
                last: true,
                empty: false
            };
            painelService.listarProcessos.mockResolvedValue(mockPage);

            await processosStore.fetchProcessosPainel('perfil', 1, 0, 10);

            expect(painelService.listarProcessos).toHaveBeenCalledWith('perfil', 1, 0, 10);
            expect(processosStore.processosPainel).toEqual(mockPage.content);
            expect(processosStore.processosPainelPage).toEqual(mockPage);
        });

        it('fetchProcessoDetalhe should call processoService and update state', async () => {
            const mockDetalhe = {
                codigo: 1,
                descricao: 'Detalhe Teste',
                tipo: 'MAPEAMENTO',
                situacao: 'EM_ANDAMENTO',
                dataLimite: '2025-12-31',
                dataCriacao: '2025-01-01',
                dataFinalizacao: '2025-12-31',
                unidades: [],
                resumoSubprocessos: []
            };
            processoService.obterDetalhesProcesso.mockResolvedValue(mockDetalhe);

            await processosStore.fetchProcessoDetalhe(1);

            expect(processoService.obterDetalhesProcesso).toHaveBeenCalledWith(1);
            expect(processosStore.processoDetalhe).toEqual(mockDetalhe);
        });

        it('criarProcesso should call processoService', async () => {
            const payload = {
                descricao: 'Novo Processo',
                tipo: 'MAPEAMENTO',
                dataLimiteEtapa1: '2025-12-31',
                unidades: [1]
            };
            processoService.criarProcesso.mockResolvedValue({ codigo: 2, descricao: 'Novo Processo', tipo: 'MAPEAMENTO', situacao: 'CRIADO', dataLimite: '2025-12-31', dataCriacao: '2025-02-01', dataFinalizacao: '' });

            await processosStore.criarProcesso(payload);

            expect(processoService.criarProcesso).toHaveBeenCalledWith(payload);
        });

        it('atualizarProcesso should call processoService', async () => {
            const payload = {
                codigo: 1,
                descricao: 'Processo Atualizado',
                tipo: 'MAPEAMENTO',
                dataLimiteEtapa1: '2025-12-31',
                unidades: [1]
            };
            processoService.atualizarProcesso.mockResolvedValue({ codigo: 1, descricao: 'Processo Atualizado', tipo: 'MAPEAMENTO', situacao: 'CRIADO', dataLimite: '2025-12-31', dataCriacao: '2025-01-01', dataFinalizacao: '' });

            await processosStore.atualizarProcesso(1, payload);

            expect(processoService.atualizarProcesso).toHaveBeenCalledWith(1, payload);
        });

        it('removerProcesso should call processoService', async () => {
            processoService.excluirProcesso.mockResolvedValue();

            await processosStore.removerProcesso(1);

            expect(processoService.excluirProcesso).toHaveBeenCalledWith(1);
        });

        it('iniciarProcesso should call processoService with correct parameters', async () => {
            processoService.iniciarProcesso.mockResolvedValue();
            const fetchDetalheSpy = vi.spyOn(processosStore, 'fetchProcessoDetalhe');

            await processosStore.iniciarProcesso(1, 'MAPEAMENTO', [10, 20]);

            expect(processoService.iniciarProcesso).toHaveBeenCalledWith(1, 'MAPEAMENTO', [10, 20]);
            expect(fetchDetalheSpy).toHaveBeenCalledWith(1);
        });

        it('finalizarProcesso should call processoService and reload details', async () => {
            processoService.finalizarProcesso.mockResolvedValue();
            const fetchDetalheSpy = vi.spyOn(processosStore, 'fetchProcessoDetalhe');

            await processosStore.finalizarProcesso(1);

            expect(processoService.finalizarProcesso).toHaveBeenCalledWith(1);
            expect(fetchDetalheSpy).toHaveBeenCalledWith(1);
        });
    });
});
