import { describe, it, expect, vi, beforeEach } from 'vitest';
import * as AdministradorService from '../administradorService';
import apiClient from '@/axios-setup';

vi.mock('@/axios-setup');

describe('AdministradorService', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('listarAdministradores deve fazer uma requisição GET para /administradores', async () => {
        const mockData = [
            { tituloEleitoral: '123456789012', nome: 'Admin 1', matricula: '111', unidadeCodigo: 1, unidadeSigla: 'UN1' }
        ];
        vi.mocked(apiClient.get).mockResolvedValue({ data: mockData });

        const result = await AdministradorService.listarAdministradores();

        expect(apiClient.get).toHaveBeenCalledWith('/administradores');
        expect(result).toEqual(mockData);
    });

    it('adicionarAdministrador deve fazer uma requisição POST para /administradores', async () => {
        const usuarioTitulo = '123456789012';
        const mockResponse = { tituloEleitoral: '123456789012', nome: 'Admin 1', matricula: '111', unidadeCodigo: 1, unidadeSigla: 'UN1' };
        vi.mocked(apiClient.post).mockResolvedValue({ data: mockResponse });

        const result = await AdministradorService.adicionarAdministrador(usuarioTitulo);

        expect(apiClient.post).toHaveBeenCalledWith('/administradores', { usuarioTitulo });
        expect(result).toEqual(mockResponse);
    });

    it('removerAdministrador deve fazer uma requisição POST para /administradores/{titulo}/remover', async () => {
        const usuarioTitulo = '123456789012';
        vi.mocked(apiClient.post).mockResolvedValue({});

        await AdministradorService.removerAdministrador(usuarioTitulo);

        expect(apiClient.post).toHaveBeenCalledWith(`/administradores/${usuarioTitulo}/remover`);
    });
});
