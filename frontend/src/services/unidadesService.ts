import apiClient from '../axios-setup';

export async function buscarTodasUnidades() {
    const response = await apiClient.get('/unidades');
    return response.data;
}

export async function buscarUnidadePorSigla(sigla: string) {
    const response = await apiClient.get(`/unidades/sigla/${sigla}`);
    return response.data;
}

export async function buscarArvoreComElegibilidade(tipoProcesso: string, codProcesso?: number) {
    let url = `/unidades/arvore-com-elegibilidade?tipoProcesso=${tipoProcesso}`;
    if (codProcesso) {
        url += `&codProcesso=${codProcesso}`;
    }
    const response = await apiClient.get(url);
    return response.data;
}
