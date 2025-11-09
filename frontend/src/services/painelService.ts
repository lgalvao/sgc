import apiClient from '../axios-setup';
import {mapProcessoResumoDtoToFrontend} from '@/mappers/processos';
import type {ProcessoResumo} from '@/types/tipos';
import {Alerta, mapAlertaDtoToFrontend} from '@/mappers/alertas';

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
  const params: any = {
    perfil,
    page,
    size,
  };
  if (unidade !== undefined && unidade !== null) {
    params.unidade = unidade;
  }
  const response = await apiClient.get<Page<any>>('/painel/processos', {
    params,
  });
  return {
    ...response.data,
    content: response.data.content.map(mapProcessoResumoDtoToFrontend),
  };
}

export async function listarAlertas(usuarioTitulo?: string, unidade?: number, page: number = 0, size: number = 20): Promise<Page<Alerta>> {
  const params: any = {
    page,
    size,
  };
  if (usuarioTitulo !== undefined && usuarioTitulo !== null) {
    params.usuarioTitulo = usuarioTitulo;
  }
  if (unidade !== undefined && unidade !== null) {
    params.unidade = unidade;
  }
  const response = await apiClient.get<Page<any>>('/painel/alertas', {
    params,
  });
  return {
    ...response.data,
    content: response.data.content.map(mapAlertaDtoToFrontend),
  };
}