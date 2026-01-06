import { setActivePinia, createPinia } from 'pinia';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { useConfiguracoesStore } from '../configuracoes';

// Mock API Client
vi.mock('@/axios-setup', () => ({
    apiClient: {
        get: vi.fn(),
        post: vi.fn()
    }
}));

describe('Configuracoes Store', () => {
    let store: ReturnType<typeof useConfiguracoesStore>;
    let apiClient: any;

    beforeEach(async () => {
        setActivePinia(createPinia());
        store = useConfiguracoesStore();

        // Reset mocks and re-import apiClient
        vi.clearAllMocks();
        vi.spyOn(console, 'error').mockImplementation(() => {});
        const axiosSetup = await import('@/axios-setup');
        apiClient = axiosSetup.apiClient;
    });

    it('carregarConfiguracoes deve preencher parametros em caso de sucesso', async () => {
        const mockData = [
            { id: 1, chave: 'DIAS_INATIVACAO_PROCESSO', descricao: 'Desc', valor: '60' }
        ];
        apiClient.get.mockResolvedValue({ data: mockData });

        await store.carregarConfiguracoes();

        expect(apiClient.get).toHaveBeenCalledWith('/configuracoes');
        expect(store.parametros).toEqual(mockData);
        expect(store.error).toBeNull();
        expect(store.loading).toBe(false);
    });

    it('carregarConfiguracoes deve definir erro em caso de falha', async () => {
        apiClient.get.mockRejectedValue(new Error('Erro API'));

        await store.carregarConfiguracoes();

        expect(store.parametros).toEqual([]);
        expect(store.error).toBe('Não foi possível carregar as configurações.');
        expect(store.loading).toBe(false);
    });

    it('salvarConfiguracoes deve atualizar parametros em caso de sucesso', async () => {
        const novosParametros = [
            { id: 1, chave: 'DIAS_INATIVACAO_PROCESSO', descricao: 'Desc', valor: '90' }
        ];
        apiClient.post.mockResolvedValue({ data: novosParametros });

        const result = await store.salvarConfiguracoes(novosParametros);

        expect(apiClient.post).toHaveBeenCalledWith('/configuracoes', novosParametros);
        expect(store.parametros).toEqual(novosParametros);
        expect(result).toBe(true);
        expect(store.error).toBeNull();
    });

    it('salvarConfiguracoes deve definir erro em caso de falha', async () => {
        const novosParametros = [
            { id: 1, chave: 'DIAS_INATIVACAO_PROCESSO', descricao: 'Desc', valor: '90' }
        ];
        apiClient.post.mockRejectedValue(new Error('Erro API'));

        const result = await store.salvarConfiguracoes(novosParametros);

        expect(result).toBe(false);
        expect(store.error).toBe('Não foi possível salvar as configurações.');
    });

    it('getValor deve retornar valor correto ou padrao', () => {
        store.parametros = [
            { id: 1, chave: 'TESTE_KEY', descricao: 'Desc', valor: 'valor_teste' }
        ];

        expect(store.getValor('TESTE_KEY')).toBe('valor_teste');
        expect(store.getValor('KEY_INEXISTENTE', 'padrao')).toBe('padrao');
    });

    it('getDiasInativacaoProcesso deve retornar valor configurado ou padrao', () => {
        // Caso padrão
        expect(store.getDiasInativacaoProcesso()).toBe(30);

        // Caso configurado
        store.parametros = [
            { id: 1, chave: 'DIAS_INATIVACAO_PROCESSO', descricao: 'Desc', valor: '45' }
        ];
        expect(store.getDiasInativacaoProcesso()).toBe(45);

        // Caso inválido
        store.parametros = [
            { id: 1, chave: 'DIAS_INATIVACAO_PROCESSO', descricao: 'Desc', valor: 'abc' }
        ];
        expect(store.getDiasInativacaoProcesso()).toBe(30);
    });

    it('getDiasAlertaNovo deve retornar valor configurado ou padrao', () => {
        // Caso padrão
        expect(store.getDiasAlertaNovo()).toBe(3);

        // Caso configurado
        store.parametros = [
            { id: 1, chave: 'DIAS_ALERTA_NOVO', descricao: 'Desc', valor: '7' }
        ];
        expect(store.getDiasAlertaNovo()).toBe(7);

        // Caso inválido
        store.parametros = [
             { id: 1, chave: 'DIAS_ALERTA_NOVO', descricao: 'Desc', valor: 'xyz' }
        ];
        expect(store.getDiasAlertaNovo()).toBe(3);
    });
});
