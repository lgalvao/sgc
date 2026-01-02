import apiClient from "../axios-setup";

export interface AdministradorDto {
    tituloEleitoral: string;
    nome: string;
    matricula: string;
    unidadeCodigo: number;
    unidadeSigla: string;
}

export async function listarAdministradores(): Promise<AdministradorDto[]> {
    const response = await apiClient.get<AdministradorDto[]>("/administradores");
    return response.data;
}

export async function adicionarAdministrador(usuarioTitulo: string): Promise<AdministradorDto> {
    const response = await apiClient.post<AdministradorDto>("/administradores", {
        usuarioTitulo
    });
    return response.data;
}

export async function removerAdministrador(usuarioTitulo: string): Promise<void> {
    await apiClient.post(`/administradores/${usuarioTitulo}/remover`);
}
