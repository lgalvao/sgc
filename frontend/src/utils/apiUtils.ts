/**
 * Utilitários genéricos para chamadas à API.
 *
 * Fornece funções wrapper para operações comuns de API,
 * encapsulando o padrão de chamada e extração de dados.
 */

import type {AxiosResponse} from 'axios';
import apiClient from '@/axios-setup';

/**
 * Realiza uma requisição GET genérica.
 *
 * @example
 * const processo = await apiGet<Processo>(`/processos/${id}`);
 * const processos = await apiGet<Processo[]>('/processos', { status: 'ATIVO' });
 */
export async function apiGet<T>(url: string, params?: Record<string, any>): Promise<T> {
    const response: AxiosResponse<T> = await apiClient.get(url, {params});
    return response.data;
}

/**
 * Realiza uma requisição POST genérica.
 *
 * @example
 * const processo = await apiPost<Processo, CriarProcessoRequest>('/processos', request);
 * await apiPost<void>(`/processos/${id}/finalizar`);
 */
export async function apiPost<T = void, D = any>(url: string, data?: D): Promise<T> {
    const response: AxiosResponse<T> = await apiClient.post(url, data);
    return response.data;
}

