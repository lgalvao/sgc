import {defineStore} from 'pinia'
import mapasData from '../mocks/mapas.json' assert {type: 'json'};
import type {Mapa} from '@/types/tipos'
import {parseDate} from '@/utils/dateUtils'

function parseMapaDates(mapa: Omit<Mapa, 'dataCriacao' | 'dataDisponibilizacao' | 'dataFinalizacao'> & { dataCriacao: string, dataDisponibilizacao?: string | null, dataFinalizacao?: string | null }): Mapa {
    return {
        ...mapa,
        dataCriacao: parseDate(mapa.dataCriacao) || new Date(),
        dataDisponibilizacao: mapa.dataDisponibilizacao ? parseDate(mapa.dataDisponibilizacao) : null,
        dataFinalizacao: mapa.dataFinalizacao ? parseDate(mapa.dataFinalizacao) : null,
    };
}

export const useMapasStore = defineStore('mapas', {
    state: () => ({
        mapas: mapasData.map(parseMapaDates) as Mapa[]
    }),
    getters: {
        getMapaByUnidadeId: (state) => (unidadeId: string, idProcesso: number): Mapa | undefined => {
            return state.mapas.find(m => m.unidade === unidadeId && m.idProcesso === idProcesso)
        },
        getMapaVigentePorUnidade: (state) => (unidadeId: string): Mapa | undefined => {
            return state.mapas.find(m => m.unidade === unidadeId && m.situacao === 'vigente')
        }
    },
    actions: {
        adicionarMapa(mapa: Mapa) {
            this.mapas.push(mapa);
        },
        editarMapa(id: number, novosDados: Partial<Mapa>) {
            const idx = this.mapas.findIndex(m => m.id === id)
            if (idx !== -1) this.mapas[idx] = {...this.mapas[idx], ...novosDados}
        },
        definirMapaComoVigente(unidadeId: string, idProcesso: number) {
            // Primeiro, desmarcar qualquer mapa vigente anterior para esta unidade
            this.mapas.forEach(mapa => {
                if (mapa.unidade === unidadeId && mapa.situacao === 'vigente') {
                    mapa.situacao = 'disponibilizado';
                }
            });

            // Definir o mapa do processo como vigente
            const mapaIndex = this.mapas.findIndex(m => m.unidade === unidadeId && m.idProcesso === idProcesso);
            if (mapaIndex !== -1) {
                this.mapas[mapaIndex].situacao = 'vigente';
                this.mapas[mapaIndex].dataFinalizacao = new Date();
            }
        }
    }
})