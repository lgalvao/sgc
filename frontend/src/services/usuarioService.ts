import apiClient from '../axios-setup';
import {AutenticacaoRequest, EntrarRequest, mapPerfilUnidadeToFrontend, PerfilUnidade, LoginResponse} from '@/mappers/sgrh';

export async function autenticar(request: AutenticacaoRequest): Promise<boolean> {
  try {
    const response = await apiClient.post<boolean>('/usuarios/autenticar', request);
    return response.data;
  } catch (error) {
    console.error('Erro ao autenticar:', error);
    throw error;
  }
}

export async function autorizar(tituloEleitoral: number): Promise<PerfilUnidade[]> {
  try {
    const response = await apiClient.post<any[]>('/usuarios/autorizar', tituloEleitoral, {
      headers: {
        'Content-Type': 'application/json',
      },
    });
    return response.data.map(mapPerfilUnidadeToFrontend);
  } catch (error) {
    console.error('Erro ao autorizar:', error);
    throw error;
  }
}

export async function entrar(request: EntrarRequest): Promise<LoginResponse> {
  try {
    const response = await apiClient.post<LoginResponse>('/usuarios/entrar', request);
    return response.data;
  } catch (error) {
    console.error('Erro ao entrar:', error);
    throw error;
  }
}