import apiClient from "../axios-setup";

export async function buscarTodasUnidades() {
    const response = await apiClient.get("/unidades");
    return response.data;
}

export async function buscarUnidadePorSigla(sigla: string) {
    const response = await apiClient.get(`/unidades/sigla/${sigla}`);
    return response.data;
}

export async function buscarUnidadePorCodigo(codigo: number) {
    const response = await apiClient.get(`/unidades/${codigo}`);
    return response.data;
}

export async function buscarArvoreComElegibilidade(
    tipoProcesso: string,
    codProcesso?: number,
) {
    let url = `/unidades/arvore-com-elegibilidade?tipoProcesso=${tipoProcesso}`;
    if (codProcesso) {
        url += `&codProcesso=${codProcesso}`;
    }
    const response = await apiClient.get(url);
    return response.data;
}

export async function buscarArvoreUnidade(codigo: number) {
    const response = await apiClient.get(`/unidades/${codigo}/arvore`);
    return response.data;
}

export async function buscarSubordinadas(sigla: string) {
    const response = await apiClient.get(`/unidades/sigla/${sigla}/subordinadas`);
    return response.data;
}

export async function buscarSuperior(sigla: string) {
    const response = await apiClient.get(`/unidades/sigla/${sigla}/superior`);
    return response.data || null;
}
