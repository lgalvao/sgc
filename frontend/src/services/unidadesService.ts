import apiClient from '../axios-setup';

export const UnidadesService = {
    async buscarTodasUnidades() {
        return await apiClient.get('/unidades');
    },
    async buscarUnidadePorSigla(sigla: string) {
        return await apiClient.get(`/unidades/sigla/${sigla}`);
    }
};