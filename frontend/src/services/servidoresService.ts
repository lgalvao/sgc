import servidoresMock from '../mocks/servidores.json';

export const ServidoresService = {
    async buscarTodosServidores() {
        // Simula uma chamada de API
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({ data: servidoresMock });
            }, 500);
        });
    }
};