import {describe, expect, it, vi} from 'vitest';
import apiClient from '@/axios-setup';
import {buscarConfiguracoes, buscarDiasInativacaoProcesso, salvarConfiguracoes} from '../configuracaoService';

vi.mock('@/axios-setup', () => ({
    default: {
        get: vi.fn(),
        post: vi.fn()
    },
    apiClient: {
        get: vi.fn(),
        post: vi.fn()
    }
}));

describe('configuracaoService', () => {
    it('deve buscar configuracoes', async () => {
        const mockData = [{chave: 'test', valor: 'val', descricao: 'desc'}];
        (apiClient.get as any).mockResolvedValue({data: mockData});

        const result = await buscarConfiguracoes();
        expect(apiClient.get).toHaveBeenCalledWith('/configuracoes');
        expect(result).toEqual(mockData);
    });

    it('deve salvar configuracoes', async () => {
        const mockData = [{chave: 'test', valor: 'val', descricao: 'desc'}];
        (apiClient.post as any).mockResolvedValue({data: mockData});

        const result = await salvarConfiguracoes(mockData);
        expect(apiClient.post).toHaveBeenCalledWith('/configuracoes', mockData);
        expect(result).toEqual(mockData);
    });

    it('deve buscar dias de inativacao de processo', async () => {
        (apiClient.get as any).mockResolvedValue({data: {dias: 17}});

        const result = await buscarDiasInativacaoProcesso();
        expect(apiClient.get).toHaveBeenCalledWith('/configuracoes/dias-inativacao-processo');
        expect(result).toBe(17);
    });
});
