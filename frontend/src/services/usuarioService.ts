import apiClient from '../axios-setup';
import {
    AutenticacaoRequest,
    EntrarRequest,
    LoginResponse,
    mapPerfilUnidadeToFrontend,
    PerfilUnidade
} from '@/mappers/sgrh';

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