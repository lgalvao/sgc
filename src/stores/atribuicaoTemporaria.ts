import {defineStore} from 'pinia'
import {AtribuicaoTemporaria} from "@/types/tipos";

export const useAtribuicaoTemporariaStore = defineStore('atribuicaoTemporaria', {
    state: () => ({
        atribuicoes: [] as AtribuicaoTemporaria[]
    }),
    actions: {
        getAtribuicoesPorServidor(servidorId: number): AtribuicaoTemporaria[] {
            return this.atribuicoes.filter(a => a.servidorId === servidorId)
        },
        criarAtribuicao(novaAtribuicao: AtribuicaoTemporaria) {
            this.atribuicoes.push(novaAtribuicao);
        }
    }
})