import apiClient from '../axios-setup'; // Importar a instância configurada do axios

export const AtribuicaoTemporariaService = {
    async buscarTodasAtribuicoes() {
         // Usar apiClient e o endpoint correto
        return await apiClient.get('/atribuicoes'); // Retornar a resposta completa do axios
    }
};