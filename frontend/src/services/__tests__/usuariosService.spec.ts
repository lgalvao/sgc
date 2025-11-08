import {describe, expect, it, vi} from 'vitest';
import {UsuariosService} from '../usuariosService';
import apiClient from '@/axios-setup';

vi.mock('@/axios-setup', () => ({
  default: {
    get: vi.fn(),
  },
}));

describe('UsuariosService', () => {
  it('buscarTodosUsuarios should call the correct endpoint', async () => {
    (apiClient.get as any).mockResolvedValue({ data: [] });
    await UsuariosService.buscarTodosUsuarios();
    expect(apiClient.get).toHaveBeenCalledWith('/usuarios');
  });

  it('buscarUsuariosPorUnidade should call the correct endpoint', async () => {
    (apiClient.get as any).mockResolvedValue({ data: [] });
    await UsuariosService.buscarUsuariosPorUnidade(1);
    expect(apiClient.get).toHaveBeenCalledWith('/unidades/1/usuarios');
  });

  it('buscarUsuariosPorUnidade should return an empty array on failure', async () => {
    (apiClient.get as any).mockRejectedValue(new Error('Failed'));
    const result = await UsuariosService.buscarUsuariosPorUnidade(1);
    expect(result).toEqual([]);
  });
});
