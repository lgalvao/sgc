import {defineStore} from 'pinia'
import mapasData from '../mocks/mapas.json' assert { type: "json" };
import type {Mapa} from '@/types/tipos'

function parseMapaDates(mapa: any): Mapa {
    return {
        ...mapa,
        dataCriacao: new Date(mapa.dataCriacao),
        dataDisponibilizacao: mapa.dataDisponibilizacao ? new Date(mapa.dataDisponibilizacao) : null,
        dataFinalizacao: mapa.dataFinalizacao ? new Date(mapa.dataFinalizacao) : null,
    };
}

export const useMapasStore = defineStore('mapas', {
    state: () => ({
        mapas: mapasData.map(parseMapaDates) as Mapa[]
    }),
    getters: {
        getMapaPorUnidade: (state) => (unidadeId: string): Mapa | undefined => {
            return state.mapas.find(m => m.unidade === unidadeId)
        },
        getMapaByUnidadeId: (state) => (unidadeId: string, processoId: number): Mapa | undefined => {
            return state.mapas.find(m => m.unidade === unidadeId && m.id === processoId)
        },
        getMapaVigentePorUnidade: (state) => (unidadeId: string): Mapa | undefined => {
            return state.mapas.find(m => m.unidade === unidadeId && m.situacao === 'em_andamento')
        },
        getMapaPorUnidadeEProcesso: (state) => (unidadeSigla: string, processoId: number): Mapa | undefined => {
            return state.mapas.find(m => m.unidade === unidadeSigla && m.processoId === processoId);
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