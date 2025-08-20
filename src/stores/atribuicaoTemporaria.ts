import {defineStore} from 'pinia'
import {AtribuicaoTemporaria} from "@/types/tipos";

export const useAtribuicaoTemporariaStore = defineStore('atribuicaoTemporaria', {
    state: () => ({
        atribuicoes: [] as AtribuicaoTemporaria[]
    }),
    getters: {
        getAtribuicoesPorServidor: (state) => (servidorId: number): AtribuicaoTemporaria[] => {
            return state.atribuicoes.filter(a => Number(a.idServidor) === servidorId)
        },
    },
    actions: {
        criarAtribuicao(novaAtribuicao: AtribuicaoTemporaria) {
            this.atribuicoes.push(novaAtribuicao);
        }
    }
})