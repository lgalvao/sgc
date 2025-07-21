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
        // Métodos para editar/remover podem ser adicionados conforme necessário
    }
}) 