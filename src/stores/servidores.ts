import {defineStore} from 'pinia'
import servidoresMock from '../mocks/servidores.json'
import type {Servidor} from '@/types/tipos'

export const useServidoresStore = defineStore('servidores', {
    state: () => ({
        servidores: servidoresMock as Servidor[]
    }),
    getters: {
        getServidorByEmail: (state) => (email: string): Servidor | undefined => {
            return state.servidores.find(s => s.email && s.email.toLowerCase() === email.toLowerCase());
        },
        getServidorById: (state) => (id: number): Servidor | undefined => {
            return state.servidores.find(s => s.id === id);
        }
    },
    actions: {}
})
