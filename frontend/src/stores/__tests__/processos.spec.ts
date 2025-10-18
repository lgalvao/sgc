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
        const error = new Error('Service failed');

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

        it('fetchProcessosPainel should not update state on service failure', async () => {
            painelService.listarProcessos.mockRejectedValue(error);
            await expect(processosStore.fetchProcessosPainel('perfil', 1, 0, 10)).rejects.toThrow(error);
            expect(processosStore.processosPainel).toEqual([]);
            expect(processosStore.processosPainelPage).toEqual({});
        });

        it('fetchProcessosFinalizados should update state on success', async () => {
            const mockProcessos = [{codigo: 2, descricao: 'Finalizado', tipo: 'DIAGNOSTICO', situacao: 'FINALIZADO', dataLimite: '2025-12-31', dataCriacao: '2025-01-01', unidadeCodigo: 1, unidadeNome: 'TESTE'}];
            processoService.fetchProcessosFinalizados.mockResolvedValue(mockProcessos);
            await processosStore.fetchProcessosFinalizados();
            expect(processoService.fetchProcessosFinalizados).toHaveBeenCalled();
            expect(processosStore.processosFinalizados).toEqual(mockProcessos);
        });

        it('fetchProcessosFinalizados should not update state on failure', async () => {
            processoService.fetchProcessosFinalizados.mockRejectedValue(error);
            await expect(processosStore.fetchProcessosFinalizados()).rejects.toThrow(error);
            expect(processosStore.processosFinalizados).toEqual([]);
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

        it('fetchProcessoDetalhe should not update state on service failure', async () => {
            processoService.obterDetalhesProcesso.mockRejectedValue(error);
            await expect(processosStore.fetchProcessoDetalhe(1)).rejects.toThrow(error);
            expect(processosStore.processoDetalhe).toBeNull();
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

        it('criarProcesso should throw error on service failure', async () => {
            const payload = { descricao: 'Novo Processo', tipo: 'MAPEAMENTO', dataLimiteEtapa1: '2025-12-31', unidades: [1] };
            processoService.criarProcesso.mockRejectedValue(error);
            await expect(processosStore.criarProcesso(payload)).rejects.toThrow(error);
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

        it('atualizarProcesso should throw error on service failure', async () => {
            const payload = { codigo: 1, descricao: 'Processo Atualizado', tipo: 'MAPEAMENTO', dataLimiteEtapa1: '2025-12-31', unidades: [1] };
            processoService.atualizarProcesso.mockRejectedValue(error);
            await expect(processosStore.atualizarProcesso(1, payload)).rejects.toThrow(error);
        });


        it('removerProcesso should call processoService', async () => {
            processoService.excluirProcesso.mockResolvedValue();

            await processosStore.removerProcesso(1);

            expect(processoService.excluirProcesso).toHaveBeenCalledWith(1);
        });

        it('removerProcesso should throw error on service failure', async () => {
            processoService.excluirProcesso.mockRejectedValue(error);
            await expect(processosStore.removerProcesso(1)).rejects.toThrow(error);
        });

        it('iniciarProcesso should call processoService with correct parameters', async () => {
            processoService.iniciarProcesso.mockResolvedValue();
            processoService.obterDetalhesProcesso.mockResolvedValue({} as any);
            const fetchDetalheSpy = vi.spyOn(processosStore, 'fetchProcessoDetalhe');

            await processosStore.iniciarProcesso(1, 'MAPEAMENTO', [10, 20]);

            expect(processoService.iniciarProcesso).toHaveBeenCalledWith(1, 'MAPEAMENTO', [10, 20]);
            expect(fetchDetalheSpy).toHaveBeenCalledWith(1);
        });

        it('iniciarProcesso should throw error and not reload details on failure', async () => {
            processoService.iniciarProcesso.mockRejectedValue(error);
            const fetchDetalheSpy = vi.spyOn(processosStore, 'fetchProcessoDetalhe');
            await expect(processosStore.iniciarProcesso(1, 'MAPEAMENTO', [10, 20])).rejects.toThrow(error);
            expect(fetchDetalheSpy).not.toHaveBeenCalled();
        });

        it('finalizarProcesso should call processoService and reload details', async () => {
            processoService.finalizarProcesso.mockResolvedValue();
            processoService.obterDetalhesProcesso.mockResolvedValue({} as any);
            const fetchDetalheSpy = vi.spyOn(processosStore, 'fetchProcessoDetalhe');

            await processosStore.finalizarProcesso(1);

            expect(processoService.finalizarProcesso).toHaveBeenCalledWith(1);
            expect(fetchDetalheSpy).toHaveBeenCalledWith(1);
        });

        it('finalizarProcesso should throw error and not reload details on failure', async () => {
            processoService.finalizarProcesso.mockRejectedValue(error);
            const fetchDetalheSpy = vi.spyOn(processosStore, 'fetchProcessoDetalhe');
            await expect(processosStore.finalizarProcesso(1)).rejects.toThrow(error);
            expect(fetchDetalheSpy).not.toHaveBeenCalled();
        });

        it('processarCadastroBloco should update subprocesso statuses', () => {
            processosStore.processoDetalhe = {
                codigo: 1,
                unidades: [
                    { sigla: 'A', situacaoSubprocesso: 'AGUARDANDO_ACEITE' },
                    { sigla: 'B', situacaoSubprocesso: 'AGUARDANDO_ACEITE' },
                ]
            };

            processosStore.processarCadastroBloco({
                idProcesso: 1,
                unidades: ['A'],
                tipoAcao: 'aceitar',
                unidadeUsuario: 'USER'
            });

            const unidadeA = processosStore.processoDetalhe.unidades.find(u => u.sigla === 'A');
            expect(unidadeA.situacaoSubprocesso).toBe('MAPA_VALIDADO');
        });

        it('addMovement should add a new movement with a generated id and date', () => {
            const initialMovementsCount = processosStore.movements.length;
            processosStore.addMovement({
                idSubprocesso: 1,
                responsavel: 'user',
                acao: 'Teste',
                detalhes: {}
            });
            expect(processosStore.movements.length).toBe(initialMovementsCount + 1);
            const lastMovement = processosStore.movements[processosStore.movements.length - 1];
            expect(lastMovement.codigo).toBeDefined();
            expect(lastMovement.dataHora).toBeDefined();
        });

    });

    describe('getters', () => {
        beforeEach(() => {
            processosStore.processoDetalhe = {
                codigo: 1,
                resumoSubprocessos: [
                    { unidade: { sigla: 'A' } },
                    { unidade: { sigla: 'B' } },
                    { unidade: { sigla: 'A' } },
                ]
            };
        });

        it('getUnidadesDoProcesso should return subprocessos for the correct process', () => {
            const subprocessos = processosStore.getUnidadesDoProcesso(1);
            expect(subprocessos).toHaveLength(3);
            const emptySubprocessos = processosStore.getUnidadesDoProcesso(2);
            expect(emptySubprocessos).toHaveLength(0);
        });

        it('getSubprocessosElegiveisAceiteBloco should filter by unit', () => {
            const subprocessos = processosStore.getSubprocessosElegiveisAceiteBloco(1, 'A');
            expect(subprocessos).toHaveLength(2);
            const emptySubprocessos = processosStore.getSubprocessosElegiveisAceiteBloco(1, 'C');
            expect(emptySubprocessos).toHaveLength(0);
        });

        it('getSubprocessosElegiveisHomologacaoBloco should return all subprocessos', () => {
            const subprocessos = processosStore.getSubprocessosElegiveisHomologacaoBloco(1);
            expect(subprocessos).toHaveLength(3);
        });
    });
});
