import unidadesMock from '../mocks/unidades.json';

export const UnidadesService = {
    async buscarTodasUnidades() {
        // Simula uma chamada de API
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({ data: unidadesMock });
            }, 500);
        });
    }
};