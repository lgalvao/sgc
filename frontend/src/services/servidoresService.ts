import apiClient from '../axios-setup';
import type {Servidor} from '@/types/tipos';

export const ServidoresService = {
    async buscarTodosServidores() {
        return await apiClient.get('/servidores');
    },
    async buscarServidoresPorUnidade(codigoUnidade: number): Promise<Servidor[]> {
        try {
            const response = await apiClient.get(`/unidades/${codigoUnidade}/servidores`);
            return response.data;
        } catch (error) {
            console.error(`Erro ao buscar servidores para a unidade ${codigoUnidade}:`, error);
            return [];
        }
    }
};