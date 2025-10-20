import apiClient from '../axios-setup';
import {mapProcessoResumoDtoToFrontend} from '../mappers/processos';
import type { ProcessoResumo } from '@/types/tipos';
import {Alerta, mapAlertaDtoToFrontend} from '../mappers/alertas';

export interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number; // current page number
  size: number; // page size
  first: boolean;
  last: boolean;
  empty: boolean;
}

export async function listarProcessos(perfil: string, unidade?: number, page: number = 0, size: number = 20): Promise<Page<ProcessoResumo>> {
  try {
    const response = await apiClient.get<Page<any>>('/painel/processos', {
      params: {
        perfil,
        unidade,
        page,
        size,
      },
    });
    return {
      ...response.data,
      content: response.data.content.map(mapProcessoResumoDtoToFrontend),
    };
  } catch (error) {
    console.error('Erro ao listar processos:', error);
    throw error;
  }
}

export async function listarAlertas(usuarioTitulo?: string, unidade?: number, page: number = 0, size: number = 20): Promise<Page<Alerta>> {
  try {
    const response = await apiClient.get<Page<any>>('/painel/alertas', {
      params: {
        usuarioTitulo,
        unidade,
        page,
        size,
      },
    });
    return {
      ...response.data,
      content: response.data.content.map(mapAlertaDtoToFrontend),
    };
  } catch (error) {
    console.error('Erro ao listar alertas:', error);
    throw error;
  }
}