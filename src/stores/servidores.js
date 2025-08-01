import {defineStore} from 'pinia'
import servidoresMock from '../mocks/servidores.json'

export const useServidoresStore = defineStore('servidores', {
    state: () => ({
        servidores: [...servidoresMock]
    }),
    getters: {
        getServidorByEmail: (state) => (email) => {
            return state.servidores.find(s => s.email.toLowerCase() === email.toLowerCase());
        },
        getServidorById: (state) => (id) => {
            return state.servidores.find(s => s.id === id);
        }
    },
    actions: {}
})
