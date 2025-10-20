import atribuicoesMock from '../mocks/atribuicoes.json';

export const AtribuicaoTemporariaService = {
    async buscarTodasAtribuicoes() {
        // Simula uma chamada de API
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({ data: atribuicoesMock });
            }, 500);
        });
    }
};