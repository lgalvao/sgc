import apiClient from '@/axios-setup';
import {mapMapaDtoToModel} from '@/mappers/mapas';
import type {Mapa, SalvarMapaRequest} from '@/types/tipos';

export const listarMapas = async (): Promise<Mapa[]> => {
    const response = await apiClient.get('/mapas');
    return response.data.map(mapMapaDtoToModel);
};

export const obterMapa = async (id: number): Promise<Mapa> => {
    const response = await apiClient.get(`/mapas/${id}`);
    return mapMapaDtoToModel(response.data);
};

export const criarMapa = async (mapa: SalvarMapaRequest): Promise<Mapa> => {
    const response = await apiClient.post('/mapas', mapa);
    return mapMapaDtoToModel(response.data);
};

export const atualizarMapa = async (codMapa: number, mapa: Mapa): Promise<Mapa> => {
    const response = await apiClient.post(`/mapas/${codMapa}/atualizar`, mapa);
    return mapMapaDtoToModel(response.data);
};

export const excluirMapa = async (codMapa: number): Promise<void> => {
    await apiClient.post(`/mapas/${codMapa}/excluir`);
};