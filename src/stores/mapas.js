import {defineStore} from 'pinia'
import mapasData from '../mocks/mapas.json'

/**
 * @typedef { import('../types/domain').Mapa } Mapa
 */

export const useMapasStore = defineStore('mapas', {
    state: () => ({
        /** @type {Mapa[]} */
        mapas: mapasData
    }),
    getters: {
        getMapaByUnidadeId: (state) => (unidadeId, processoId) => {
            return state.mapas.find(m => m.unidadeId === unidadeId && m.processoId === processoId)
        },
        getMapaVigentePorUnidade: (state) => (unidadeId) => {
            return state.mapas.find(m => m.unidadeId === unidadeId && m.situacao === 'em_andamento')
        }
    },
    actions: {
        editarMapa(id, novosDados) {
            const idx = this.mapas.findIndex(m => m.id === id)
            if (idx !== -1) this.mapas[idx] = {...this.mapas[idx], ...novosDados}
        }
    }
})