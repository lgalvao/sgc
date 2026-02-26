import type {Usuario} from "@/types/tipos";
import apiClient from "../axios-setup";

export interface AutenticacaoRequest {
    tituloEleitoral: string;
    senha: string;
}

export interface EntrarRequest {
    tituloEleitoral: string;
    perfil: string;
    unidadeCodigo: number;
}

export interface LoginResponse {
    tituloEleitoral: string;
    nome: string;
    perfil: string;
    unidadeCodigo: number;
    token: string;
}

export interface PerfilUnidade {
    perfil: string;
    unidade: {
        codigo: number;
        nome: string;
        sigla: string;
    };
    siglaUnidade: string;
}

export async function autenticar(
    request: AutenticacaoRequest,
): Promise<boolean> {
    const response = await apiClient.post<boolean>(
        "/usuarios/autenticar",
        request,
    );
    return response.data;
}

export async function autorizar(
    tituloEleitoral: string,
): Promise<PerfilUnidade[]> {
    const response = await apiClient.post<any[]>(
        "/usuarios/autorizar",
        { tituloEleitoral },
    );
    return response.data.map(mapPerfilUnidade);
}

export async function entrar(request: EntrarRequest): Promise<LoginResponse> {
    const response = await apiClient.post<LoginResponse>(
        "/usuarios/entrar",
        request,
    );
    return response.data;
}

export async function buscarTodosUsuarios() {
    const response = await apiClient.get("/usuarios");
    return response.data;
}

export async function buscarUsuariosPorUnidade(
    codigoUnidade: number,
): Promise<Usuario[]> {
    const response = await apiClient.get(`/unidades/${codigoUnidade}/usuarios`);
    return response.data;
}

export async function buscarUsuarioPorTitulo(
    titulo: string,
): Promise<Usuario> {
    const response = await apiClient.get(`/usuarios/${titulo}`);
    return response.data;
}

function mapPerfilUnidade(dto: any): PerfilUnidade {
    return {
        perfil: dto.perfil,
        unidade: {
            codigo: dto.unidade.codigo,
            nome: dto.unidade.nome,
            sigla: dto.unidade.sigla,
        },
        siglaUnidade: dto.siglaUnidade || dto.unidade.sigla,
    };
}
