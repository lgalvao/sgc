import {defineStore} from 'pinia'
import {AtribuicaoTemporaria} from "@/types/tipos";
import atribuicoesMock from '../mocks/atribuicoes.json';

export const useAtribuicaoTemporariaStore = defineStore('atribuicaoTemporaria', {
    state: () => ({
        atribuicoes: atribuicoesMock.map(a => ({
            ...a,
            dataInicio: new Date(a.dataInicio).toISOString(),
            dataFim: new Date(a.dataTermino).toISOString(),
            dataTermino: new Date(a.dataTermino).toISOString(),
        })) as AtribuicaoTemporaria[]
    }),
    getters: {
        getAtribuicoesPorServidor: (state) => (servidorId: number): AtribuicaoTemporaria[] => {
            return state.atribuicoes.filter(a => a.idServidor === servidorId)
        },
        getAtribuicoesPorUnidade: (state) => (unidadeSigla: string): AtribuicaoTemporaria[] => {
            return state.atribuicoes.filter(a => a.unidade === unidadeSigla)
        },
    },
    actions: {
        criarAtribuicao(novaAtribuicao: AtribuicaoTemporaria) {
            this.atribuicoes.push(novaAtribuicao);
        }
    }
})