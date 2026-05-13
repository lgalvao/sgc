import {apiGet, apiPost} from "@/utils/apiUtils";

export interface AtribuicaoTemporaria {
    codigo: number;
    unidadeCodigo: number;
    unidadeSigla: string;
    usuario: {
        tituloEleitoral: string;
        matricula: string;
        nome: string;
        email: string;
        ramal: string;
    };
    dataInicio: string;
    dataTermino: string;
    justificativa: string;
}

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
    await apiPost(`/unidades/${codUnidade}/atribuicoes-temporarias`, request);
}

export async function buscarAtribuicoesTemporariasPorUnidade(codUnidade: number): Promise<AtribuicaoTemporaria[]> {
    return apiGet(`/unidades/${codUnidade}/atribuicoes-temporarias`);
}

export async function atualizarAtribuicaoTemporaria(
    codUnidade: number,
    codigoAtribuicao: number,
    request: CriarAtribuicaoTemporariaRequest,
): Promise<void> {
    await apiPost(`/unidades/${codUnidade}/atribuicoes-temporarias/${codigoAtribuicao}/atualizar`, request);
}

export async function removerAtribuicaoTemporaria(codUnidade: number, codigoAtribuicao: number): Promise<void> {
    await apiPost(`/unidades/${codUnidade}/atribuicoes-temporarias/${codigoAtribuicao}/excluir`);
}
