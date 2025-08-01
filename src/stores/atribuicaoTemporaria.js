import {defineStore} from 'pinia'

export const useAtribuicaoTemporariaStore = defineStore('atribuicaoTemporaria', {
    state: () => ({
        atribuicoes: []
    }),
    actions: {
        removerAtribuicao(unidade) {
            this.atribuicoes = this.atribuicoes.filter(a => a.unidade !== unidade)
        },
        getAtribuicaoPorUnidade(unidade) {
            return this.atribuicoes.find(a => a.unidade === unidade)
        },
        getAtribuicoesPorServidor(servidorId) {
            return this.atribuicoes.filter(a => a.servidorId === servidorId)
        }
    }
})