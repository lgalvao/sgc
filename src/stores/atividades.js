import { defineStore } from 'pinia';
import atividadesMock from '../mocks/atividades.json';

/**
 * @typedef { import('../types/domain').Atividade } Atividade
 * @typedef { import('../types/domain').Conhecimento } Conhecimento
 */

export const useAtividadesStore = defineStore('atividades', {
  state: () => ({
    /** @type {Atividade[]} */
    atividades: atividadesMock
  }),
  getters: {
    getAtividadesPorProcessoUnidade: (state) => (processoUnidadeId) => {
      return state.atividades.filter(a => a.processoUnidadeId === processoUnidadeId);
    }
  },
  actions: {
    setAtividades(processoUnidadeId, novasAtividades) {
      // Remove as atividades antigas para este processoUnidadeId
      this.atividades = this.atividades.filter(a => a.processoUnidadeId !== processoUnidadeId);
      // Adiciona as novas atividades
      this.atividades.push(...novasAtividades);
    },
    adicionarAtividade(atividade) {
      this.atividades.push(atividade);
    },
    removerAtividade(atividadeId) {
      this.atividades = this.atividades.filter(a => a.id !== atividadeId);
    },
    adicionarConhecimento(atividadeId, conhecimento) {
      const atividade = this.atividades.find(a => a.id === atividadeId);
      if (atividade) {
        atividade.conhecimentos.push(conhecimento);
      }
    },
    removerConhecimento(atividadeId, conhecimentoId) {
      const atividade = this.atividades.find(a => a.id === atividadeId);
      if (atividade) {
        atividade.conhecimentos = atividade.conhecimentos.filter(c => c.id !== conhecimentoId);
      }
    }
  }
});
