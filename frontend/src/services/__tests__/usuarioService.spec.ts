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
    it('login', async () => {
      const mockResponse = {
        autenticado: true,
        requerSelecaoPerfil: true,
        perfisUnidades: [{ perfil: 'ADMIN', unidade: { codigo: 1, sigla: 'TEST', nome: 'Teste' }, siglaUnidade: 'TEST' }],
        sessao: null,
      };
      (apiClient.post as any).mockResolvedValueOnce({ data: mockResponse });
      const result = await usuarioService.login({ tituloEleitoral: '123', senha: '123' });
      expect(apiClient.post).toHaveBeenCalledWith('/usuarios/login', { tituloEleitoral: '123', senha: '123' });
      expect(result.autenticado).toBe(true);
      expect(result.requerSelecaoPerfil).toBe(true);
      expect(result.perfisUnidades).toHaveLength(1);
    });

    it('entrar', async () => {
      const mockData = { tituloEleitoral: '123', nome: 'Teste', perfil: 'ADMIN', unidadeCodigo: 1 };
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

    it('pesquisarUsuarios', async () => {
      (apiClient.get as any).mockResolvedValueOnce({ data: [] });
      await usuarioService.pesquisarUsuarios('maria');
      expect(apiClient.get).toHaveBeenCalledWith('/usuarios/pesquisar', {
        params: { termo: 'maria' }
      });
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

    it('mapSessaoLoginToFrontend', () => {
      const result = usuarioService.mapSessaoLoginToFrontend({
        tituloEleitoral: '123',
        nome: 'Teste',
        perfil: 'ADMIN',
        unidadeCodigo: 1,
      });
      expect(result.perfil).toBe('ADMIN');
    });

    it('mapFluxoLoginToFrontend', () => {
      const result = usuarioService.mapFluxoLoginToFrontend({
        autenticado: true,
        requerSelecaoPerfil: false,
        perfisUnidades: [{
          perfil: 'ADMIN',
          unidade: { codigo: 1, sigla: 'TST', nome: 'Teste' },
          siglaUnidade: 'TST'
        }],
        sessao: {
          tituloEleitoral: '123',
          nome: 'Teste',
          perfil: 'ADMIN',
          unidadeCodigo: 1,
        }
      });
      expect(result.autenticado).toBe(true);
      expect(result.requerSelecaoPerfil).toBe(false);
      expect(result.sessao?.tituloEleitoral).toBe('123');
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
