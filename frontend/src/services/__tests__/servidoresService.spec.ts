import {describe, expect, it, vi} from 'vitest';
import {ServidoresService} from '../servidoresService';
import apiClient from '@/axios-setup';

vi.mock('@/axios-setup', () => ({
  default: {
    get: vi.fn(),
  },
}));

describe('ServidoresService', () => {
  it('buscarTodosServidores should call the correct endpoint', async () => {
    (apiClient.get as any).mockResolvedValue({ data: [] });
    await ServidoresService.buscarTodosServidores();
    expect(apiClient.get).toHaveBeenCalledWith('/servidores');
  });

  it('buscarServidoresPorUnidade should call the correct endpoint', async () => {
    (apiClient.get as any).mockResolvedValue({ data: [] });
    await ServidoresService.buscarServidoresPorUnidade(1);
    expect(apiClient.get).toHaveBeenCalledWith('/unidades/1/servidores');
  });

  it('buscarServidoresPorUnidade should return an empty array on failure', async () => {
    (apiClient.get as any).mockRejectedValue(new Error('Failed'));
    const result = await ServidoresService.buscarServidoresPorUnidade(1);
    expect(result).toEqual([]);
  });
});
