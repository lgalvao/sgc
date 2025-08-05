import {defineStore} from 'pinia'
import servidoresMock from '../mocks/servidores.json'

/**
 * @typedef { import('../types/domain').Servidor } Servidor
 */

export const useServidoresStore = defineStore('servidores', {
    state: () => ({
        /** @type {Servidor[]} */
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
