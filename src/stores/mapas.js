import {defineStore} from 'pinia'
import mapasMock from '../mocks/mapas.json'

export const useMapasStore = defineStore('mapas', {
    state: () => ({
        mapas: mapasMock // { id, unidade, competencias: [ { nome, atividades: [ { nome, conhecimentos: [string] } ] } ] }
    }),
    actions: {
        editarMapa(id, novosDados) {
            const idx = this.mapas.findIndex(m => m.id === id)
            if (idx !== -1) this.mapas[idx] = {...this.mapas[idx], ...novosDados}
        },
        getMapaPorUnidade(unidade) {
            return this.mapas.find(m => m.unidade === unidade)
        }
    }
}) 