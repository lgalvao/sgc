import { defineStore } from 'pinia'
import type { Atividade, Conhecimento } from '@/types/tipos'
import { TipoMudanca, useRevisaoStore } from './revisao'
import { useApi } from '@/composables/useApi'
import { mapSubprocessoCadastroToAtividades } from '@/mappers/atividadesMapper'
import type { SubprocessoCadastroDto } from '@/types/SubprocessoCadastroDto'

export const useAtividadesStore = defineStore('atividades', {
  state: () => ({
    atividades: [] as Atividade[],
    loading: false,
    error: null as string | null,
    atividadesSnapshot: [] as Atividade[] // Adicionado para armazenar o snapshot
  }),
  getters: {
    getAtividadesPorSubprocesso: (state) => (idSubprocesso: number): Atividade[] => {
      return state.atividades.filter((a) => a.idSubprocesso === idSubprocesso)
    }
  },
  actions: {
    async carregarAtividades(idSubprocesso: number) {
      // Evita recarregar se os dados para este subprocesso já existem
      if (this.atividades.some((a) => a.idSubprocesso === idSubprocesso)) {
        return
      }

      this.loading = true
      this.error = null
      const api = useApi()

      try {
        const data = await api.get<SubprocessoCadastroDto.SubprocessoCadastro>(
          `/subprocessos/${idSubprocesso}/cadastro`
        )
        const novasAtividades = mapSubprocessoCadastroToAtividades(data, idSubprocesso)

        // Remove as atividades antigas para este idSubprocesso e adiciona as novas
        const atividadesAtuais = this.atividades.filter(
          (a) => a.idSubprocesso !== idSubprocesso
        )
        this.atividades = [...atividadesAtuais, ...novasAtividades]
      } catch (err) {
        this.error = (err as Error).message
      } finally {
        this.loading = false
      }
    },

    setAtividades(idSubprocesso: number, novasAtividades: Atividade[]) {
      // Remove as atividades antigas para este idSubprocesso
      this.atividades = this.atividades.filter((a) => a.idSubprocesso !== idSubprocesso)

      // Adiciona as novas atividades
      this.atividades.push(...novasAtividades)
    },

    removerAtividade(atividadeId: number) {
      this.atividades = this.atividades.filter((a) => a.id !== atividadeId)
    },

    adicionarConhecimento(
      atividadeId: number,
      conhecimento: Conhecimento,
      impactedCompetencyIds: number[]
    ) {
      const revisaoStore = useRevisaoStore()
      const index = this.atividades.findIndex((a) => a.id === atividadeId)
      if (index !== -1) {
        const atividade = this.atividades[index]
        // A API irá gerar o ID, mas para reatividade imediata podemos usar um temporário
        conhecimento.id = Math.random()
        const updatedAtividade = {
          ...atividade,
          conhecimentos: [...atividade.conhecimentos, conhecimento]
        }
        this.atividades.splice(index, 1, updatedAtividade)
        revisaoStore.registrarMudanca({
          tipo: TipoMudanca.ConhecimentoAdicionado,
          idAtividade: atividade.id,
          descricaoAtividade: atividade.descricao,
          idConhecimento: conhecimento.id,
          descricaoConhecimento: conhecimento.descricao,
          competenciasImpactadasIds: impactedCompetencyIds
        })
      }
    },

    removerConhecimento(
      atividadeId: number,
      conhecimentoId: number,
      impactedCompetencyIds: number[]
    ) {
      const revisaoStore = useRevisaoStore()
      const atividade = this.atividades.find((a) => a.id === atividadeId)
      if (atividade) {
        const conhecimentoRemovido = atividade.conhecimentos.find((c) => c.id === conhecimentoId)
        atividade.conhecimentos = atividade.conhecimentos.filter((c) => c.id !== conhecimentoId)
        if (conhecimentoRemovido) {
          revisaoStore.registrarMudanca({
            tipo: TipoMudanca.ConhecimentoRemovido,
            idAtividade: atividade.id,
            descricaoAtividade: atividade.descricao,
            idConhecimento: conhecimentoRemovido.id,
            descricaoConhecimento: conhecimentoRemovido.descricao,
            competenciasImpactadasIds: impactedCompetencyIds
          })
        }
      }
    },

    setAtividadesSnapshot(snapshot: Atividade[]) {
      this.atividadesSnapshot = snapshot
    },

    reset() {
      this.atividades = []
      this.loading = false
      this.error = null
      this.atividadesSnapshot = []
    }
  }
})