import { defineStore } from 'pinia'
import unidadesMock from '../mocks/unidades.json'

export const useUnidadesStore = defineStore('unidades', {
  state: () => ({
    unidades: [...unidadesMock]
  }),
  actions: {
    // Métodos para manipular unidades podem ser adicionados aqui
  }
}) 