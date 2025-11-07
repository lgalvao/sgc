import apiClient from '../axios-setup';
import type {Usuario} from '@/types/tipos';

export const UsuariosService = {
    async buscarTodosUsuarios() {
        return await apiClient.get('/usuarios');
    },
    async buscarUsuariosPorUnidade(codigoUnidade: number): Promise<Usuario[]> {
        try {
            const response = await apiClient.get(`/unidades/${codigoUnidade}/usuarios`);
            return response.data;
        } catch (error: any) {
            if (error?.response?.status === 404) {
                return [];
            }
            return [];
        }
    }
};