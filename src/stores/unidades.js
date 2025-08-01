import {defineStore} from 'pinia'
import unidadesMock from '../mocks/unidades.json'

export const useUnidadesStore = defineStore('unidades', {
    state: () => ({
        unidades: [...unidadesMock]
    }),
    actions: {
        findUnit(sigla, units = this.unidades) {
            for (const unit of units) {
                if (unit.sigla === sigla) return unit
                if (unit.filhas) {
                    const found = this.findUnit(sigla, unit.filhas)
                    if (found) return found
                }
            }
            return null
        }
    }
}) 