import {defineStore} from 'pinia'
import unidadesMock from '../mocks/unidades.json' assert {type: 'json'};
import type {Unidade} from '@/types/tipos'

export const useUnidadesStore = defineStore('unidades', {
    state: () => ({
        unidades: unidadesMock as unknown as Unidade[]
    }),
    actions: {
        pesquisarUnidade(this: any, sigla: string, units: Unidade[] = this.unidades): Unidade | null {
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