import { defineStore } from 'pinia'
import type { Unidade } from '@/types/tipos'
import { useApi } from '@/composables/useApi'

export const useUnidadesStore = defineStore('unidades', {
  state: () => ({
    unidades: [] as Unidade[],
    loading: false,
    error: null as string | null
  }),
  actions: {
    async carregarUnidades() {
      if (this.unidades.length > 0) return // Evita recarregar se já tiver dados

      this.loading = true
      this.error = null
      const api = useApi()

      try {
        // O backend retorna uma estrutura plana, o frontend espera uma árvore.
        // Por enquanto, vamos manter a estrutura que a API retorna.
        // A transformação para árvore, se necessária, será feita no mapper.
        const unidadesFromApi = await api.get<Unidade[]>('/unidades')
        this.unidades = unidadesFromApi
      } catch (error) {
        this.error = (error as Error).message
      } finally {
        this.loading = false
      }
    },

    pesquisarUnidade(sigla: string, units: Unidade[] = this.unidades): Unidade | null {
      for (const unit of units) {
        if (unit.sigla === sigla) return unit
        if (unit.filhas && unit.filhas.length > 0) {
          const found = this.pesquisarUnidade(sigla, unit.filhas)
          if (found) return found
        }
      }
      return null
    },

    getUnidadesSubordinadas(siglaUnidade: string): string[] {
      const unidadesEncontradas: string[] = []
      const stack: Unidade[] = []

      const unidadeRaiz = this.pesquisarUnidade(siglaUnidade)
      if (unidadeRaiz) {
        stack.push(unidadeRaiz)
        while (stack.length > 0) {
          const currentUnidade = stack.pop()!
          unidadesEncontradas.push(currentUnidade.sigla)
          if (currentUnidade.filhas) {
            for (let i = currentUnidade.filhas.length - 1; i >= 0; i--) {
              stack.push(currentUnidade.filhas[i])
            }
          }
        }
      }
      return unidadesEncontradas
    },

    getUnidadeSuperior(siglaUnidade: string): string | null {
      const stack: { unit: Unidade; parentSigla: string | null }[] = []

      for (const unidade of this.unidades) {
        stack.push({ unit: unidade, parentSigla: null })
      }

      while (stack.length > 0) {
        const { unit: currentUnidade, parentSigla: currentParentSigla } = stack.pop()!

        if (currentUnidade.sigla === siglaUnidade) {
          return currentParentSigla
        }

        if (currentUnidade.filhas) {
          for (let i = currentUnidade.filhas.length - 1; i >= 0; i--) {
            stack.push({ unit: currentUnidade.filhas[i], parentSigla: currentUnidade.sigla })
          }
        }
      }
      return null
    },

    getUnidadeImediataSuperior(siglaUnidade: string): string | null {
      return this.getUnidadeSuperior(siglaUnidade)
    }
  }
})