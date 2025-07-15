import { defineStore } from 'pinia'

export const useAtribuicaoTemporariaStore = defineStore('atribuicaoTemporaria', {
  state: () => ({
    atribuicoes: [] // { unidade, nomeResponsavel, dataInicio, dataTermino }
  }),
  actions: {
    adicionarAtribuicao(atribuicao) {
      this.atribuicoes.push(atribuicao)
    },
    editarAtribuicao(unidade, novosDados) {
      const idx = this.atribuicoes.findIndex(a => a.unidade === unidade)
      if (idx !== -1) this.atribuicoes[idx] = { ...this.atribuicoes[idx], ...novosDados }
    },
    removerAtribuicao(unidade) {
      this.atribuicoes = this.atribuicoes.filter(a => a.unidade !== unidade)
    },
    getAtribuicaoPorUnidade(unidade) {
      return this.atribuicoes.find(a => a.unidade === unidade)
    }
  }
}) 