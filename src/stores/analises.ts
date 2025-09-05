import {defineStore} from 'pinia'
import type {AnaliseValidacao} from '@/types/tipos'
import {generateUniqueId} from '@/utils/idGenerator'

export const useAnalisesStore = defineStore('analises', {
    state: () => ({
        analises: [] as AnaliseValidacao[]
    }),
    getters: {
        getAnalisesPorSubprocesso: (state) => (idSubprocesso: number): AnaliseValidacao[] => {
            return state.analises
                .filter(analise => analise.idSubprocesso === idSubprocesso)
                .sort((a, b) => b.dataHora.getTime() - a.dataHora.getTime())
        }
    },
    actions: {
        registrarAnalise(payload: Omit<AnaliseValidacao, 'id'>) {
            const novaAnalise: AnaliseValidacao = {
                ...payload,
                id: generateUniqueId()
            }
            this.analises.push(novaAnalise)
            return novaAnalise
        }
    }
})