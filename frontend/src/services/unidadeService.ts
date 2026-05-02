import type {DiagnosticoOrganizacional, MapaVigenteReferencia, Unidade} from "@/types/tipos";
import {apiGet} from "@/utils/apiUtils";

type UsuarioResumoDto = {
    tituloEleitoral: string;
    matricula: string;
    nome: string;
    email: string;
    ramal: string;
};

type UnidadeApiDto = {
    codigo: number;
    nome: string;
    sigla: string;
    tipo?: string;
    isElegivel?: boolean;
    tituloTitular?: string;
    tipoResponsabilidade?: string;
    titular?: UsuarioResumoDto | null;
    responsavel?: UsuarioResumoDto | null;
    subunidades?: UnidadeApiDto[];
};

const unidadeVazia: Unidade = {
    codigo: 0,
    nome: "",
    sigla: "",
    filhas: []
};

function mapearUsuarioResumo(usuario?: UsuarioResumoDto | null): Unidade["titular"] {
    if (!usuario) {
        return null;
    }

    return {
        codigo: 0,
        tituloEleitoral: usuario.tituloEleitoral,
        matricula: usuario.matricula,
        nome: usuario.nome,
        email: usuario.email,
        ramal: usuario.ramal,
        unidade: unidadeVazia
    };
}

function mapearUnidade(dto: UnidadeApiDto): Unidade {
    return {
        codigo: dto.codigo,
        sigla: dto.sigla,
        tipo: dto.tipo,
        nome: dto.nome,
        isElegivel: dto.isElegivel,
        tituloTitular: dto.tituloTitular,
        tipoResponsabilidade: dto.tipoResponsabilidade,
        titular: mapearUsuarioResumo(dto.titular),
        responsavel: mapearUsuarioResumo(dto.responsavel),
        filhas: (dto.subunidades ?? []).map(mapearUnidade),
    };
}

function mapearListaUnidades(unidades: UnidadeApiDto[]): Unidade[] {
    return unidades.map(mapearUnidade);
}

function montarUrlArvoreComElegibilidade(tipoProcesso: string, codProcesso?: number): string {
    const params = new URLSearchParams({tipoProcesso});
    if (codProcesso !== undefined) {
        params.set("codProcesso", String(codProcesso));
    }
    return `/unidades/arvore-com-elegibilidade?${params.toString()}`;
}

export async function buscarTodasUnidades(): Promise<Unidade[]> {
    const data = await apiGet<UnidadeApiDto[]>("/unidades");
    return mapearListaUnidades(data);
}

export async function buscarDiagnosticoOrganizacional(): Promise<DiagnosticoOrganizacional> {
    return apiGet("/unidades/diagnostico-organizacional");
}

export async function buscarCodigosUnidadesComMapaVigente(): Promise<number[]> {
    return apiGet("/unidades/com-mapa-vigente");
}

export async function buscarUnidadePorCodigo(codigo: number): Promise<Unidade> {
    const data = await apiGet<UnidadeApiDto>(`/unidades/${codigo}`);
    return mapearUnidade(data);
}

export async function buscarArvoreComElegibilidade(
    tipoProcesso: string,
    codProcesso?: number,
): Promise<Unidade[]> {
    const data = await apiGet<UnidadeApiDto[]>(montarUrlArvoreComElegibilidade(tipoProcesso, codProcesso));
    return mapearListaUnidades(data);
}

export async function buscarArvoreUnidade(codigo: number): Promise<Unidade> {
    const data = await apiGet<UnidadeApiDto>(`/unidades/${codigo}/arvore`);
    return mapearUnidade(data);
}

export async function buscarReferenciaMapaVigente(codigo: number): Promise<MapaVigenteReferencia | null> {
    return (await apiGet<MapaVigenteReferencia | null>(`/unidades/${codigo}/mapa-vigente/referencia`)) ?? null;
}
