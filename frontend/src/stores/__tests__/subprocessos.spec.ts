import {createPinia, setActivePinia} from 'pinia';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {useSubprocessosStore} from '../subprocessos';
import {useProcessosStore} from '../processos';
import {usePerfilStore} from '../perfil';
import {useUnidadesStore} from '../unidades';
import {useMapasStore} from '../mapas';
import {useAtividadesStore} from '../atividades';
import {useFeedbackStore} from '../feedback';
import {
    buscarContextoEdicao,
    buscarSubprocessoDetalhe,
    buscarSubprocessoPorProcessoEUnidade
} from '@/services/subprocessoService';
import {
    aceitarCadastro,
    aceitarRevisaoCadastro,
    devolverCadastro,
    devolverRevisaoCadastro,
    disponibilizarCadastro,
    disponibilizarRevisaoCadastro,
    homologarCadastro,
    homologarRevisaoCadastro
} from '@/services/cadastroService';

// Mock Dependencies
vi.mock('@/services/subprocessoService', () => ({
    buscarContextoEdicao: vi.fn(),
    buscarSubprocessoDetalhe: vi.fn(),
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
}));

vi.mock('@/services/cadastroService', () => ({
    disponibilizarCadastro: vi.fn(),
    disponibilizarRevisaoCadastro: vi.fn(),
    devolverCadastro: vi.fn(),
    aceitarCadastro: vi.fn(),
    homologarCadastro: vi.fn(),
    devolverRevisaoCadastro: vi.fn(),
    aceitarRevisaoCadastro: vi.fn(),
    homologarRevisaoCadastro: vi.fn(),
}));

vi.mock('@/mappers/mapas', () => ({
    mapMapaCompletoDtoToModel: vi.fn((data) => ({ ...data, mapped: true })),
}));

vi.mock('@/mappers/atividades', () => ({
    mapAtividadeVisualizacaoToModel: vi.fn((data) => ({ ...data, mapped: true })),
}));

// Mock Stores
vi.mock('../processos', () => ({
    useProcessosStore: vi.fn(),
}));
vi.mock('../perfil', () => ({
    usePerfilStore: vi.fn(),
}));
vi.mock('../unidades', () => ({
    useUnidadesStore: vi.fn(),
}));
vi.mock('../mapas', () => ({
    useMapasStore: vi.fn(),
}));
vi.mock('../atividades', () => ({
    useAtividadesStore: vi.fn(),
}));
vi.mock('../feedback', () => ({
    useFeedbackStore: vi.fn(),
}));

// Mock API Client
vi.mock('@/axios-setup', () => ({
    apiClient: {
        post: vi.fn().mockResolvedValue({ data: {} }),
        get: vi.fn().mockResolvedValue({ data: {} })
    }
}));

describe('Subprocessos Store', () => {
    let store: ReturnType<typeof useSubprocessosStore>;

    // Mocks dos stores retornados
    const mockProcessosStore = {
        buscarProcessoDetalhe: vi.fn(),
        processoDetalhe: { codigo: 999 },
    };
    const mockPerfilStore = {
        perfilSelecionado: null,
        unidadeSelecionada: null,
        perfisUnidades: [],
        unidadeAtual: null,
    };
    const mockUnidadesStore = {
        unidade: null,
    };
    const mockMapasStore = {
        mapaCompleto: null,
    };
    const mockAtividadesStore = {
        setAtividadesParaSubprocesso: vi.fn(),
    };
    const mockFeedbackStore = {
        show: vi.fn(),
    };

    beforeEach(() => {
        setActivePinia(createPinia());

        // Reset mocks
        vi.clearAllMocks();

        // Configura retorno dos useStore mocks
        (useProcessosStore as any).mockReturnValue(mockProcessosStore);
        (usePerfilStore as any).mockReturnValue(mockPerfilStore);
        (useUnidadesStore as any).mockReturnValue(mockUnidadesStore);
        (useMapasStore as any).mockReturnValue(mockMapasStore);
        (useAtividadesStore as any).mockReturnValue(mockAtividadesStore);
        (useFeedbackStore as any).mockReturnValue(mockFeedbackStore);

        store = useSubprocessosStore();
    });

    describe('buscarSubprocessoDetalhe', () => {
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
            (buscarSubprocessoDetalhe as any).mockResolvedValue({ codigo: 1, situacao: 'CRIADO' });

            await store.buscarSubprocessoDetalhe(1);

            expect(buscarSubprocessoDetalhe).toHaveBeenCalledWith(1, 'ADMIN', null);
            expect(store.subprocessoDetalhe).toEqual({ codigo: 1, situacao: 'CRIADO' });
            expect(store.lastError).toBeNull();
        });

        it('deve buscar com sucesso para SERVIDOR com unidade selecionada', async () => {
            mockPerfilStore.perfilSelecionado = 'SERVIDOR' as any;
            mockPerfilStore.unidadeSelecionada = 10;
            mockPerfilStore.perfisUnidades = [{ perfil: 'SERVIDOR', unidade: { codigo: 10 } }] as any;
            mockPerfilStore.unidadeAtual = 10;

            (buscarSubprocessoDetalhe as any).mockResolvedValue({ codigo: 1, situacao: 'CRIADO' });

            await store.buscarSubprocessoDetalhe(1);

            expect(buscarSubprocessoDetalhe).toHaveBeenCalledWith(1, 'SERVIDOR', 10);
        });

        it('deve lidar com erro do serviço', async () => {
            mockPerfilStore.perfilSelecionado = 'ADMIN' as any;
            (buscarSubprocessoDetalhe as any).mockRejectedValue(new Error('Erro backend'));

            await store.buscarSubprocessoDetalhe(1);

            expect(store.subprocessoDetalhe).toBeNull();
            expect(store.lastError).toBeTruthy();
        });
    });

    describe('buscarSubprocessoPorProcessoEUnidade', () => {
        it('deve retornar codigo em caso de sucesso', async () => {
            (buscarSubprocessoPorProcessoEUnidade as any).mockResolvedValue({ codigo: 123 });

            const result = await store.buscarSubprocessoPorProcessoEUnidade(1, 'SESEL');

            expect(result).toBe(123);
            expect(store.lastError).toBeNull();
        });

        it('deve retornar null em caso de erro', async () => {
            (buscarSubprocessoPorProcessoEUnidade as any).mockRejectedValue(new Error('Não encontrado'));

            const result = await store.buscarSubprocessoPorProcessoEUnidade(1, 'SESEL');

            expect(result).toBeNull();
            expect(store.lastError).toBeTruthy();
        });
    });

    describe('buscarContextoEdicao', () => {
        it('deve popular stores relacionados com dados retornados', async () => {
            mockPerfilStore.perfilSelecionado = 'ADMIN' as any;
            const mockData = {
                subprocesso: { codigo: 1 },
                unidade: { codigo: 10, nome: 'Teste' },
                mapa: { id: 5 },
                atividadesDisponiveis: [{ id: 100 }]
            };
            (buscarContextoEdicao as any).mockResolvedValue(mockData);

            await store.buscarContextoEdicao(1);

            expect(store.subprocessoDetalhe).toEqual(mockData.subprocesso);
            expect(mockUnidadesStore.unidade).toEqual(mockData.unidade);
            // Verifica mapper
            expect(mockMapasStore.mapaCompleto).toEqual({ id: 5, mapped: true });
            expect(mockAtividadesStore.setAtividadesParaSubprocesso).toHaveBeenCalledWith(
                1,
                [{ id: 100, mapped: true }]
            );
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

    describe('Ações de Workflow', () => {
        // --- Disponibilizar Cadastro ---
        it('disponibilizarCadastro deve executar com sucesso e feedback', async () => {
            (disponibilizarCadastro as any).mockResolvedValue({});
            const result = await store.disponibilizarCadastro(1);
            expect(result).toBe(true);
            expect(disponibilizarCadastro).toHaveBeenCalledWith(1);
            expect(mockFeedbackStore.show).toHaveBeenCalledWith(expect.stringContaining("Cadastro"), expect.anything(), 'success');
        });

        it('disponibilizarCadastro deve lidar com erro', async () => {
            (disponibilizarCadastro as any).mockRejectedValue(new Error('Falha'));
            const result = await store.disponibilizarCadastro(1);
            expect(result).toBe(false);
            expect(store.lastError).toBeTruthy();
        });

        // --- Disponibilizar Revisão ---
        it('disponibilizarRevisaoCadastro deve executar com sucesso', async () => {
            (disponibilizarRevisaoCadastro as any).mockResolvedValue({});
            const result = await store.disponibilizarRevisaoCadastro(1);
            expect(result).toBe(true);
            expect(disponibilizarRevisaoCadastro).toHaveBeenCalledWith(1);
            expect(mockFeedbackStore.show).toHaveBeenCalledWith(expect.stringContaining("Revisão"), expect.anything(), 'success');
        });

        it('disponibilizarRevisaoCadastro deve lidar com erro', async () => {
             (disponibilizarRevisaoCadastro as any).mockRejectedValue(new Error('Falha'));
             const result = await store.disponibilizarRevisaoCadastro(1);
             expect(result).toBe(false);
             expect(store.lastError).toBeTruthy();
        });

        // --- Devolver Cadastro ---
        it('devolverCadastro deve executar com sucesso', async () => {
            (devolverCadastro as any).mockResolvedValue({});
            const result = await store.devolverCadastro(1, { observacoes: 'Erro' });
            expect(result).toBe(true);
            expect(devolverCadastro).toHaveBeenCalledWith(1, { observacoes: 'Erro' });
            expect(mockFeedbackStore.show).toHaveBeenCalledWith(expect.stringContaining("Cadastro"), expect.anything(), 'success');
        });

         it('devolverCadastro deve lidar com erro', async () => {
             (devolverCadastro as any).mockRejectedValue(new Error('Falha'));
             const result = await store.devolverCadastro(1, { observacoes: 'Erro' });
             expect(result).toBe(false);
             expect(store.lastError).toBeTruthy();
        });

        // --- Aceitar Cadastro ---
        it('aceitarCadastro deve executar com sucesso', async () => {
            (aceitarCadastro as any).mockResolvedValue({});
            const result = await store.aceitarCadastro(1, { aprovado: true } as any);
            expect(result).toBe(true);
            expect(aceitarCadastro).toHaveBeenCalledWith(1, { aprovado: true });
        });

        it('aceitarCadastro deve lidar com erro', async () => {
            (aceitarCadastro as any).mockRejectedValue(new Error('Falha'));
            const result = await store.aceitarCadastro(1, { aprovado: true } as any);
            expect(result).toBe(false);
        });

        // --- Homologar Cadastro ---
        it('homologarCadastro deve recarregar detalhes após sucesso', async () => {
            mockPerfilStore.perfilSelecionado = 'ADMIN' as any;
            mockPerfilStore.unidadeAtual = null;
            (homologarCadastro as any).mockResolvedValue({});
            (buscarSubprocessoDetalhe as any).mockResolvedValue({ codigo: 1, situacao: 'HOMOLOGADO' });

            const result = await store.homologarCadastro(1, { aprovado: true } as any);
            expect(result).toBe(true);
            expect(homologarCadastro).toHaveBeenCalledWith(1, { aprovado: true });
            expect(buscarSubprocessoDetalhe).toHaveBeenCalled();
        });

        it('homologarCadastro deve lidar com erro', async () => {
            (homologarCadastro as any).mockRejectedValue(new Error('Falha'));
            const result = await store.homologarCadastro(1, { aprovado: true } as any);
            expect(result).toBe(false);
            expect(store.lastError).toBeTruthy();
        });

         // --- Devolver Revisão ---
         it('devolverRevisaoCadastro deve executar com sucesso', async () => {
            (devolverRevisaoCadastro as any).mockResolvedValue({});
            const result = await store.devolverRevisaoCadastro(1, { observacoes: 'Erro' });
            expect(result).toBe(true);
            expect(devolverRevisaoCadastro).toHaveBeenCalledWith(1, { observacoes: 'Erro' });
        });

         it('devolverRevisaoCadastro deve lidar com erro', async () => {
             (devolverRevisaoCadastro as any).mockRejectedValue(new Error('Falha'));
             const result = await store.devolverRevisaoCadastro(1, { observacoes: 'Erro' });
             expect(result).toBe(false);
        });

         // --- Aceitar Revisão ---
         it('aceitarRevisaoCadastro deve executar com sucesso', async () => {
            (aceitarRevisaoCadastro as any).mockResolvedValue({});
            const result = await store.aceitarRevisaoCadastro(1, { aprovado: true } as any);
            expect(result).toBe(true);
            expect(aceitarRevisaoCadastro).toHaveBeenCalledWith(1, { aprovado: true });
        });

        it('aceitarRevisaoCadastro deve lidar com erro', async () => {
             (aceitarRevisaoCadastro as any).mockRejectedValue(new Error('Falha'));
             const result = await store.aceitarRevisaoCadastro(1, { aprovado: true } as any);
             expect(result).toBe(false);
        });

         // --- Homologar Revisão ---
         it('homologarRevisaoCadastro deve executar com sucesso e recarregar', async () => {
            mockPerfilStore.perfilSelecionado = 'ADMIN' as any;
            mockPerfilStore.unidadeAtual = null;
            (homologarRevisaoCadastro as any).mockResolvedValue({});
            (buscarSubprocessoDetalhe as any).mockResolvedValue({ codigo: 1 });

            const result = await store.homologarRevisaoCadastro(1, { aprovado: true } as any);
            expect(result).toBe(true);
            expect(homologarRevisaoCadastro).toHaveBeenCalledWith(1, { aprovado: true });
            expect(buscarSubprocessoDetalhe).toHaveBeenCalled();
        });

        it('homologarRevisaoCadastro deve lidar com erro', async () => {
             (homologarRevisaoCadastro as any).mockRejectedValue(new Error('Falha'));
             const result = await store.homologarRevisaoCadastro(1, { aprovado: true } as any);
             expect(result).toBe(false);
        });
    });

    describe('alterarDataLimiteSubprocesso', () => {
        it('deve delegar para apiClient e recarregar detalhes', async () => {
            mockPerfilStore.perfilSelecionado = 'ADMIN' as any;
            mockPerfilStore.unidadeAtual = null;
            (buscarSubprocessoDetalhe as any).mockResolvedValue({});

            const { apiClient } = await import('@/axios-setup');
            const dados = { novaData: '2024-12-31' };

            await store.alterarDataLimiteSubprocesso(123, dados);

            expect(apiClient.post).toHaveBeenCalledWith('/subprocessos/123/data-limite', {
                 novaDataLimite: dados.novaData
            });
            expect(buscarSubprocessoDetalhe).toHaveBeenCalledWith(123, 'ADMIN', null);
        });

        it('deve lidar com erro na API', async () => {
            const { apiClient } = await import('@/axios-setup');
            (apiClient.post as any).mockRejectedValue(new Error("API Fail"));

            await expect(store.alterarDataLimiteSubprocesso(123, { novaData: '2022' }))
                .rejects.toThrow("API Fail");
            expect(store.lastError).toBeTruthy();
        });
    });
});
