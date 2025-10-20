import apiClient from '../axios-setup';

export const UnidadesService = {
    async buscarTodasUnidades() {
        return await apiClient.get('/unidades');
    }
};