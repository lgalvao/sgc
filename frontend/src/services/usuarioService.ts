import {
    type AutenticacaoRequest,
    type EntrarRequest,
    type LoginResponse,
    mapPerfilUnidadeToFrontend,
    type PerfilUnidade,
} from "@/mappers/sgrh";
import type {Usuario} from "@/types/tipos";
import apiClient from "../axios-setup";

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
    return response.data.map(mapPerfilUnidadeToFrontend);
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
