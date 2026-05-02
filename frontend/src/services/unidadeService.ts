import type {DiagnosticoOrganizacional, MapaVigenteReferencia, Unidade, UnidadeSnapshot} from "@/types/tipos";
import {apiGet} from "@/utils/apiUtils";

export function mapUnidadeSnapshot(dto: any): UnidadeSnapshot {
    return {
        codigo: dto.codigo,
        nome: dto.nome,
        sigla: dto.sigla,
        filhas: dto.subunidades.map(mapUnidadeSnapshot),
    };
}

export function mapUnidade(dto: any): Unidade {
    return {
        codigo: dto.codigo,
        sigla: dto.sigla,
        tipo: dto.tipo,
        nome: dto.nome,
        isElegivel: dto.isElegivel,
        tituloTitular: dto.tituloTitular,
        tipoResponsabilidade: dto.tipoResponsabilidade,
        titular: dto.titular ? {
            codigo: 0,
            tituloEleitoral: dto.titular.tituloEleitoral,
            matricula: dto.titular.matricula,
            nome: dto.titular.nome,
            email: dto.titular.email,
            ramal: dto.titular.ramal,
            unidade: {} as Unidade
        } : null,
        responsavel: dto.responsavel
            ? {
                codigo: 0,
                tituloEleitoral: dto.responsavel.tituloEleitoral,
                matricula: dto.responsavel.matricula,
                nome: dto.responsavel.nome,
                email: dto.responsavel.email,
                ramal: dto.responsavel.ramal,
                unidade: {} as Unidade,
            }
            : null,
        filhas: dto.subunidades.map(mapUnidade),
    };
}

export function mapUnidadesArray(arr: any[]): Unidade[] {
    return arr.map(mapUnidade);
}

export async function buscarTodasUnidades(): Promise<Unidade[]> {
    const data = await apiGet<any[]>("/unidades");
    return mapUnidadesArray(data);
}

export async function buscarDiagnosticoOrganizacional(): Promise<DiagnosticoOrganizacional> {
    return apiGet("/unidades/diagnostico-organizacional");
}

export async function buscarCodigosUnidadesComMapaVigente(): Promise<number[]> {
    return apiGet("/unidades/com-mapa-vigente");
}

export async function buscarUnidadePorSigla(sigla: string): Promise<Unidade> {
    const data = await apiGet<any>(`/unidades/sigla/${sigla}`);
    return mapUnidade(data);
}

export async function buscarUnidadePorCodigo(codigo: number): Promise<Unidade> {
    const data = await apiGet<any>(`/unidades/${codigo}`);
    return mapUnidade(data);
}

export async function buscarArvoreComElegibilidade(
    tipoProcesso: string,
    codProcesso?: number,
): Promise<Unidade[]> {
    let url = `/unidades/arvore-com-elegibilidade?tipoProcesso=${tipoProcesso}`;
    if (codProcesso) {
        url += `&codProcesso=${codProcesso}`;
    }
    const data = await apiGet<any[]>(url);
    return mapUnidadesArray(data);
}

export async function buscarArvoreUnidade(codigo: number): Promise<Unidade[]> {
    const data = await apiGet<any[]>(`/unidades/${codigo}/arvore`);
    return mapUnidadesArray(data);
}

export async function buscarReferenciaMapaVigente(codigo: number): Promise<MapaVigenteReferencia | null> {
    return (await apiGet<MapaVigenteReferencia | null>(`/unidades/${codigo}/mapa-vigente/referencia`)) ?? null;
}

export async function buscarSubordinadas(sigla: string): Promise<Unidade[]> {
    const data = await apiGet<any[]>(`/unidades/sigla/${sigla}/subordinadas`);
    return mapUnidadesArray(data);
}

export async function buscarSuperior(sigla: string): Promise<Unidade> {
    const data = await apiGet<any>(`/unidades/sigla/${sigla}/superior`);
    return mapUnidade(data);
}
