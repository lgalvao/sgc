import {defineStore} from 'pinia'
import servidoresMock from '../mocks/servidores.json'
import type {Servidor} from '@/types/tipos'

export const useServidoresStore = defineStore('servidores', {
    state: () => ({
        servidores: servidoresMock.map(s => ({
            ...s,
            unidade: {sigla: s.unidade}
        })) as unknown as Servidor[]
    }),
    getters: {
        getServidorById: (state) => (id: number): Servidor | undefined => {
            return state.servidores.find(s => s.codigo === id);
        }
    },
    actions: {}
})
