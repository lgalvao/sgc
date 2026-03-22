import {beforeEach, describe, expect, it, vi} from 'vitest';
import * as usuarioService from '../usuarioService';
import apiClient from '@/axios-setup';

vi.mock('@/axios-setup', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

describe('usuarioService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('API calls', () => {
    it('autenticar', async () => {
      (apiClient.post as any).mockResolvedValueOnce({ data: true });
      const result = await usuarioService.autenticar({ tituloEleitoral: '123', senha: '123' });
      expect(apiClient.post).toHaveBeenCalledWith('/usuarios/autenticar', { tituloEleitoral: '123', senha: '123' });
      expect(result).toBe(true);
    });

    it('autorizar', async () => {
      const mockResponse = [{ perfil: 'ADMIN', unidade: { codigo: 1, sigla: 'TEST', nome: 'Teste' }, siglaUnidade: 'TEST' }];
      (apiClient.post as any).mockResolvedValueOnce({ data: mockResponse });
      const result = await usuarioService.autorizar('123');
      expect(apiClient.post).toHaveBeenCalledWith('/usuarios/autorizar', {});
      expect(result).toHaveLength(1);
      expect(result[0].perfil).toBe('ADMIN');
    });

    it('entrar', async () => {
      const mockData = { token: 'abc' };
      (apiClient.post as any).mockResolvedValueOnce({ data: mockData });
      const result = await usuarioService.entrar({ perfil: 'ADMIN', unidadeCodigo: 1 });
      expect(apiClient.post).toHaveBeenCalledWith('/usuarios/entrar', { perfil: 'ADMIN', unidadeCodigo: 1 });
      expect(result).toEqual(mockData);
    });

    it('buscarTodosUsuarios', async () => {
      (apiClient.get as any).mockResolvedValueOnce({ data: [] });
      await usuarioService.buscarTodosUsuarios();
      expect(apiClient.get).toHaveBeenCalledWith('/usuarios');
    });

    it('buscarUsuariosPorUnidade', async () => {
      (apiClient.get as any).mockResolvedValueOnce({ data: [] });
      await usuarioService.buscarUsuariosPorUnidade(1);
      expect(apiClient.get).toHaveBeenCalledWith('/unidades/1/usuarios');
    });

    it('buscarUsuarioPorTitulo', async () => {
      (apiClient.get as any).mockResolvedValueOnce({ data: {} });
      await usuarioService.buscarUsuarioPorTitulo('123');
      expect(apiClient.get).toHaveBeenCalledWith('/usuarios/123');
    });
  });

  describe('mappers', () => {
    it('mapPerfilUnidadeToFrontend', () => {
      const result = usuarioService.mapPerfilUnidadeToFrontend({
        perfil: 'ADMIN',
        unidade: { codigo: 1, sigla: 'TST', nome: 'Teste' },
        siglaUnidade: 'TST'
      });
      expect(result.perfil).toBe('ADMIN');
      expect(result.unidade.codigo).toBe(1);
      expect(result.siglaUnidade).toBe('TST');
    });

    it('mapUsuarioToFrontend', () => {
      const result = usuarioService.mapUsuarioToFrontend({
        tituloEleitoral: '12345',
        nome: 'Teste',
        email: 't@t.com',
        ramal: '123',
        unidade: { codigo: 1, sigla: 'TST', nome: 'Teste' },
        perfis: ['ADMIN']
      });
      expect(result.codigo).toBe(12345);
      expect(result.tituloEleitoral).toBe('12345');
    });

    it('LoginResponseToFrontend', () => {
      const result = usuarioService.LoginResponseToFrontend({
        tituloEleitoral: '123',
        nome: 'Teste',
        perfil: 'ADMIN',
        unidadeCodigo: 1,
        token: 'abc'
      });
      expect(result.token).toBe('abc');
      expect(result.perfil).toBe('ADMIN');
    });

    it('perfisUnidadesParaDominio', () => {
      const result = usuarioService.perfisUnidadesParaDominio([{
        perfil: 'ADMIN',
        unidade: { codigo: 1, sigla: 'TST', nome: 'Teste' },
        siglaUnidade: 'TST'
      }]);
      expect(result).toHaveLength(1);
      expect(result[0].perfil).toBe('ADMIN');
    });

    it('mapVWUsuarioToUsuario', () => {
      const vw1 = { codigo: 10, nome_completo: 'Teste', unidade_sigla: 'TST', titulo_eleitoral: '123' };
      const res1 = usuarioService.mapVWUsuarioToUsuario(vw1);
      expect(res1.codigo).toBe(10);
      expect(res1.nome).toBe('Teste');
      expect(res1.unidade).toBe('TST');
      expect(res1.tituloEleitoral).toBe('123');

      const vw2 = { titulo: '99', nome_usuario: 'Teste 2', unidade_codigo: 1 };
      const res2 = usuarioService.mapVWUsuarioToUsuario(vw2);
      expect(res2.codigo).toBe(99);
      expect(res2.nome).toBe('Teste 2');
      expect(res2.unidade).toBe(1);
    });

    it('mapVWUsuariosArray', () => {
      const result = usuarioService.mapVWUsuariosArray([{ codigo: 1 }]);
      expect(result).toHaveLength(1);
      expect(result[0].codigo).toBe(1);

      const resultEmpty = usuarioService.mapVWUsuariosArray();
      expect(resultEmpty).toEqual([]);
    });
  });
});
