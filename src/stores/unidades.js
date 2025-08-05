import {defineStore} from 'pinia'
import unidadesMock from '../mocks/unidades.json'

/**
 * @typedef { import('../types/domain').Unidade } Unidade
 */

export const useUnidadesStore = defineStore('unidades', {
    state: () => ({
        /** @type {Unidade[]} */
        unidades: [...unidadesMock]
    }),
    actions: {
        pesquisarUnidade(sigla, units = this.unidades) {
            for (const unit of units) {
                if (unit.sigla === sigla) return unit
                if (unit.filhas) {
                    const found = this.pesquisarUnidade(sigla, unit.filhas)
                    if (found) return found
                }
            }
            return null
        }
    }
}) 