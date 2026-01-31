import apiClient from "@/axios-setup";

export interface Parametro {
    codigo?: number;
    chave: string;
    descricao: string;
    valor: string;
}

export async function buscarConfiguracoes(): Promise<Parametro[]> {
    const response = await apiClient.get<Parametro[]>("/configuracoes");
    return response.data;
}

export async function salvarConfiguracoes(parametros: Parametro[]): Promise<Parametro[]> {
    const response = await apiClient.post<Parametro[]>("/configuracoes", parametros);
    return response.data;
}
