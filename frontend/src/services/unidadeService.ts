import {apiGet} from "@/utils/apiUtils";

export async function buscarTodasUnidades() {
    return apiGet("/unidades");
}

export async function buscarUnidadePorSigla(sigla: string) {
    return apiGet(`/unidades/sigla/${sigla}`);
}

export async function buscarUnidadePorCodigo(codigo: number) {
    return apiGet(`/unidades/${codigo}`);
}

export async function buscarArvoreComElegibilidade(
    tipoProcesso: string,
    codProcesso?: number,
) {
    let url = `/unidades/arvore-com-elegibilidade?tipoProcesso=${tipoProcesso}`;
    if (codProcesso) {
        url += `&codProcesso=${codProcesso}`;
    }
    return apiGet(url);
}

export async function buscarArvoreUnidade(codigo: number) {
    return apiGet(`/unidades/${codigo}/arvore`);
}

export async function buscarSubordinadas(sigla: string) {
    return apiGet(`/unidades/sigla/${sigla}/subordinadas`);
}

export async function buscarSuperior(sigla: string) {
    return apiGet(`/unidades/sigla/${sigla}/superior`) || null;
}
