import apiClient from "../axios-setup";

export interface CriarAtribuicaoTemporariaRequest {
    tituloEleitoralUsuario: string;
    dataInicio?: string;
    dataTermino: string;
    justificativa: string;
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
