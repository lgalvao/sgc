import { defineStore } from 'pinia'
import processoUnidadesMock from '../mocks/processoUnidades.json'

export const useProcessoUnidadesStore = defineStore('processoUnidades', {
  state: () => ({
    processoUnidades: processoUnidadesMock
  }),
  actions: {
    getSituacaoUnidadeNoProcesso(processoId, unidadeSigla) {
      const processo = this.processoUnidades.find(pu => pu.processoId === processoId);
      if (processo) {
        const unidade = processo.unidades.find(u => u.sigla === unidadeSigla);
        return unidade ? unidade.situacao : null;
      }
      return null;
    },
    getDataLimiteUnidadeNoProcesso(processoId, unidadeSigla) {
      const processo = this.processoUnidades.find(pu => pu.processoId === processoId);
      if (processo) {
        const unidade = processo.unidades.find(u => u.sigla === unidadeSigla);
        return unidade ? unidade.dataLimite : null;
      }
      return null;
    }
  }
})