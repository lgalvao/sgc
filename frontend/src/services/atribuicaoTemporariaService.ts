import apiClient from "../axios-setup";

export interface CriarAtribuicaoTemporariaRequest {
    tituloEleitoralServidor: string;
    dataTermino: string;
    justificativa: string;
}

export async function buscarTodasAtribuicoes() {
    const response = await apiClient.get("/atribuicoes");
    return response.data;
}

export async function criarAtribuicaoTemporaria(
    codUnidade: number,
    request: CriarAtribuicaoTemporariaRequest,
): Promise<void> {
    await apiClient.post(
        `/unidades/${codUnidade}/atribuicoes-temporarias`,
        request,
    );
}
