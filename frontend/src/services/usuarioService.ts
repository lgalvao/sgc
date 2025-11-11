import apiClient from '../axios-setup';
import {
    AutenticacaoRequest,
    EntrarRequest,
    LoginResponse,
    mapPerfilUnidadeToFrontend,
    PerfilUnidade
} from '@/mappers/sgrh';
import type { Usuario } from '@/types/tipos';

export async function autenticar(request: AutenticacaoRequest): Promise<boolean> {
  const response = await apiClient.post<boolean>('/usuarios/autenticar', request);
  return response.data;
}

export async function autorizar(tituloEleitoral: number): Promise<PerfilUnidade[]> {
    const response = await apiClient.post<any[]>('/usuarios/autorizar', tituloEleitoral, {
        headers: {
            'Content-Type': 'application/json',
        },
    });
    return response.data.map(mapPerfilUnidadeToFrontend);
}

export async function entrar(request: EntrarRequest): Promise<LoginResponse> {
  const response = await apiClient.post<LoginResponse>('/usuarios/entrar', request);
  return response.data;
}

export async function buscarTodosUsuarios() {
    const response = await apiClient.get('/usuarios');
    return response.data;
}

export async function buscarUsuariosPorUnidade(codigoUnidade: number): Promise<Usuario[]> {
    const response = await apiClient.get(`/unidades/${codigoUnidade}/usuarios`);
    return response.data;
}
