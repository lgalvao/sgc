import {defineStore} from 'pinia'
import mapasData from '../mocks/mapas.json' assert {type: 'json'};
import type {Mapa} from '@/types/tipos'
import { parseDate } from '@/utils/dateUtils'

function parseMapaDates(mapa: any): Mapa {
    return {
        ...mapa,
        dataCriacao: parseDate(mapa.dataCriacao) || new Date(),
        dataDisponibilizacao: parseDate(mapa.dataDisponibilizacao),
        dataFinalizacao: parseDate(mapa.dataFinalizacao),
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
            return state.mapas.find(m => m.unidade === unidadeId && m.situacao === 'em_andamento')
        }
    },
    actions: {
        adicionarMapa(mapa: Mapa) {
            this.mapas.push(mapa);
        },
        editarMapa(id: number, novosDados: Partial<Mapa>) {
            const idx = this.mapas.findIndex(m => m.id === id)
            if (idx !== -1) this.mapas[idx] = {...this.mapas[idx], ...novosDados}
        }
    }
})