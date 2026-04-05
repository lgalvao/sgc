import type {DiagnosticoOrganizacional, MapaVigenteReferencia, Unidade, UnidadeSnapshot} from "@/types/tipos";
import {apiGet} from "@/utils/apiUtils";

// Mappers internos (formerly in /mappers/unidades.ts)

type RegistroBruto = Record<string, unknown>;

function asRegistro(valor: unknown): RegistroBruto {
    return typeof valor === "object" && valor !== null ? valor as RegistroBruto : {};
}

function lerTexto(obj: RegistroBruto, ...chaves: string[]): string {
    for (const chave of chaves) {
        const valor = obj[chave];
        if (typeof valor === "string") {
            return valor;
        }
    }

    return "";
}

function lerNumero(obj: RegistroBruto, ...chaves: string[]): number {
    for (const chave of chaves) {
        const valor = obj[chave];
        if (typeof valor === "number") {
            return valor;
        }
    }

    return 0;
}

function lerBooleanoOpcional(obj: RegistroBruto, chave: string): boolean | undefined {
    const valor = obj[chave];
    return typeof valor === "boolean" ? valor : undefined;
}

function lerLista(obj: RegistroBruto, ...chaves: string[]): unknown[] {
    for (const chave of chaves) {
        const valor = obj[chave];
        if (Array.isArray(valor)) {
            return valor;
        }
    }

    return [];
}

export function mapUnidadeSnapshot(objetoBruto: unknown): UnidadeSnapshot {
    const obj = asRegistro(objetoBruto);

    return {
        codigo: lerNumero(obj, "codigo"),
        nome: lerTexto(obj, "nome", "nome_unidade"),
        sigla: lerTexto(obj, "sigla", "sigla_unidade", "unidade"),
        filhas: lerLista(obj, "filhas", "subunidades").map(mapUnidadeSnapshot),
    };
}

export function mapUnidade(objetoBruto: unknown): Unidade {
    const obj = asRegistro(objetoBruto);
    const responsavelBruto = obj.responsavel;
    const responsavel = asRegistro(responsavelBruto);

    return {
        codigo: lerNumero(obj, "codigo", "codigo_unidade"),
        sigla: lerTexto(obj, "sigla", "sigla_unidade"),
        tipo: lerTexto(obj, "tipo", "tipo_unidade"),
        nome: lerTexto(obj, "nome", "nome_unidade"),
        isElegivel: lerBooleanoOpcional(obj, "isElegivel"),
        usuarioCodigo: lerNumero(obj, "usuarioCodigo", "idServidorTitular"),
        responsavel: responsavelBruto
            ? {
                codigo: lerNumero(responsavel, "codigo"),
                nome: lerTexto(responsavel, "nome"),
                tituloEleitoral: lerTexto(responsavel, "tituloEleitoral"),
                unidade: mapUnidade(responsavel.unidade),
                email: lerTexto(responsavel, "email"),
                ramal: lerTexto(responsavel, "ramal"),
                usuarioTitulo: lerTexto(responsavel, "usuarioTitulo"),
                unidadeCodigo: lerNumero(responsavel, "unidadeCodigo"),
                usuarioCodigo: lerNumero(responsavel, "usuarioCodigo", "idServidorResponsavel"),
                tipo: lerTexto(responsavel, "tipo"),
                dataInicio: lerTexto(responsavel, "dataInicio"),
                dataFim: typeof responsavel.dataFim === "string" ? responsavel.dataFim : null,
            }
            : null,
        filhas: lerLista(obj, "filhas", "subunidades").map(mapUnidade),
    };
}

export function mapUnidadesArray(arr: Unidade[] = []): Unidade[] {
    return (arr as unknown[]).map(mapUnidade);
}

export async function buscarTodasUnidades() {
    return apiGet("/unidades");
}

export async function buscarDiagnosticoOrganizacional(): Promise<DiagnosticoOrganizacional> {
    return apiGet("/unidades/diagnostico-organizacional");
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

export async function buscarReferenciaMapaVigente(codigo: number): Promise<MapaVigenteReferencia | null> {
    return (await apiGet(`/unidades/${codigo}/mapa-vigente/referencia`)) ?? null;
}

export async function buscarSubordinadas(sigla: string) {
    return apiGet(`/unidades/sigla/${sigla}/subordinadas`);
}

export async function buscarSuperior(sigla: string) {
    return apiGet(`/unidades/sigla/${sigla}/superior`);
}
