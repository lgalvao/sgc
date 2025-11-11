import apiClient from '../axios-setup';

export async function buscarTodasAtribuicoes() {
    const response = await apiClient.get('/atribuicoes');
    return response.data;
}
