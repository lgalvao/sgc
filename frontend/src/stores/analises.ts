import {defineStore} from 'pinia'
import analisesMock from '../mocks/analises.json'
import type {AnaliseValidacao} from '@/types/tipos'
import {ResultadoAnalise} from '@/types/tipos'
import {generateUniqueId, parseDate} from '@/utils'

export function mapResultadoAnalise(resultado: string): string {
    switch (resultado) {
        case 'Devolução':
            return 'Devolução';
        case 'Aceite':
            return 'Aceite';
        default:
            return 'Aceite';
    }
}

export function parseAnaliseDates(analise: any): AnaliseValidacao {
    return {
        ...analise,
        dataHora: parseDate(analise.dataHora)?.toISOString() || new Date().toISOString(),
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
                .sort((a, b) => new Date(b.dataHora).getTime() - new Date(a.dataHora).getTime())
        }
    },
    actions: {
        registrarAnalise(payload: Omit<AnaliseValidacao, 'codigo'>) {
            const novaAnalise: AnaliseValidacao = {
                ...payload,
                codigo: generateUniqueId()
            }
            this.analises.push(novaAnalise)
            return novaAnalise
        },
        removerAnalisesPorSubprocesso(idSubprocesso: number) {
            this.analises = this.analises.filter(analise => analise.idSubprocesso !== idSubprocesso);
        }
    }
})