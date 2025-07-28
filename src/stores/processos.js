import {defineStore} from 'pinia'
import processosMock from '../mocks/processos.json'

export const useProcessosStore = defineStore('processos', {
    state: () => ({
        processos: [...processosMock]
    }),
    actions: {
        adicionarProcesso(novo) {
            this.processos.push(novo)
        },
        atualizarSituacaoProcesso(processoId, novaSituacao) {
            const processo = this.processos.find(p => p.id === processoId);
            if (processo) {
                processo.situacao = novaSituacao;
            }
        },
        atualizarLocalizacaoProcesso(processoId, novaLocalizacao) {
            const processo = this.processos.find(p => p.id === processoId);
            if (processo) {
                processo.localizacao = novaLocalizacao;
            }
        }
    }
})