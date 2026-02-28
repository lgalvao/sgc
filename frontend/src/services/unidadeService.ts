import type {Unidade, UnidadeSnapshot} from "@/types/tipos";
import {apiGet} from "@/utils/apiUtils";

// Mappers Internos (formerly in /mappers/unidades.ts)

export function mapUnidadeSnapshot(obj: any): UnidadeSnapshot {
    return {
        codigo: obj.codigo ?? 0,
        nome: obj.nome ?? obj.nome_unidade ?? "",
        sigla: obj.sigla ?? obj.sigla_unidade ?? obj.unidade ?? "",
        filhas: Array.isArray(obj.filhas || obj.subunidades)
            ? (obj.filhas || obj.subunidades).map(mapUnidadeSnapshot)
            : [],
    };
}

export function mapUnidade(obj: any): Unidade {
    return {
        codigo: obj.codigo ?? obj.codigo_unidade ?? 0,
        sigla: obj.sigla ?? obj.sigla_unidade ?? "",
        tipo: obj.tipo ?? obj.tipo_unidade ?? "",
        nome: obj.nome ?? obj.nome_unidade ?? "",
        isElegivel: obj.isElegivel,
        usuarioCodigo:
            obj.usuarioCodigo ?? obj.idServidorTitular ?? 0,
        responsavel: obj.responsavel
            ? {
                codigo: obj.responsavel.codigo ?? 0,
                nome: obj.responsavel.nome ?? "",
                tituloEleitoral: obj.responsavel.tituloEleitoral ?? "",
                unidade: obj.responsavel.unidade ?? ({} as Unidade),
                email: obj.responsavel.email ?? "",
                ramal: obj.responsavel.ramal ?? "",
                usuarioTitulo: obj.responsavel.usuarioTitulo ?? "",
                unidadeCodigo: obj.responsavel.unidadeCodigo ?? 0,
                usuarioCodigo: obj.responsavel.usuarioCodigo ?? obj.responsavel.idServidorResponsavel ?? 0,
                tipo: obj.responsavel.tipo ?? "",
                dataInicio: obj.responsavel.dataInicio ?? "",
                dataFim: obj.responsavel.dataFim ?? null,
            }
            : null,
        filhas: Array.isArray(obj.filhas || obj.subunidades)
            ? (obj.filhas || obj.subunidades).map(mapUnidade)
            : [],
    };
}

export function mapUnidadesArray(arr: any[] = []): Unidade[] {
    return arr.map(mapUnidade);
}


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
