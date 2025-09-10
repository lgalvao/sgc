import {defineStore} from 'pinia'
import analisesMock from '../mocks/analises.json'
import type {AnaliseValidacao} from '@/types/tipos'
import {ResultadoAnalise} from '@/types/tipos'
import {generateUniqueId} from '@/utils/idGenerator'
import {parseDate} from '@/utils/dateUtils'

function mapResultadoAnalise(resultado: string): ResultadoAnalise {
    switch (resultado) {
        case 'Devolução':
            return ResultadoAnalise.DEVOLUCAO;
        case 'Aceite':
            return ResultadoAnalise.ACEITE;
        default:
            return ResultadoAnalise.ACEITE;
    }
}

function parseAnaliseDates(analise: Omit<AnaliseValidacao, 'dataHora' | 'resultado'> & {
    dataHora: string,
    resultado: string
}): AnaliseValidacao {
    return {
        ...analise,
        dataHora: parseDate(analise.dataHora) || new Date(),
        resultado: mapResultadoAnalise(analise.resultado),
    };
}

export const useAnalisesStore = defineStore('analises', {
    state: () => ({
        analises: analisesMock.map(parseAnaliseDates) as AnaliseValidacao[] // Initialize with mock data
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
        },
        removerAnalisesPorSubprocesso(idSubprocesso: number) {
            this.analises = this.analises.filter(analise => analise.idSubprocesso !== idSubprocesso);
        }
    }
})