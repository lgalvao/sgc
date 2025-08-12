import {defineStore} from 'pinia'
import unidadesMock from '../mocks/unidades.json'
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
        },
        caminhoAteSigla(this: any, sigla: string, units: Unidade[] = this.unidades, path: Unidade[] = []): Unidade[] | null {
            for (const unit of units) {
                const newPath = [...path, unit]
                if (unit.sigla === sigla) return newPath
                if (unit.filhas && unit.filhas.length) {
                    const foundPath = this.caminhoAteSigla(sigla, unit.filhas, newPath)
                    if (foundPath) return foundPath
                }
            }
            return null
        }
    }
}) 