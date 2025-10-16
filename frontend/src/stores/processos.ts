import { defineStore } from 'pinia'
import { Processo, SituacaoProcesso, TipoProcesso } from '@/types/tipos'
import { useApi } from '@/composables/useApi'
import { parseDate } from '@/utils'
import { useSubprocessosStore } from './subprocessos' // Importar o store de subprocessos

// Funções de mapeamento de enums
export function mapTipoProcesso(tipo: string): TipoProcesso {
  const MAPPINGS: Record<string, TipoProcesso> = {
    MAPEAMENTO: TipoProcesso.MAPEAMENTO,
    REVISAO: TipoProcesso.REVISAO,
    DIAGNOSTICO: TipoProcesso.DIAGNOSTICO,
    Mapeamento: TipoProcesso.MAPEAMENTO,
    Revisão: TipoProcesso.REVISAO,
    'Diagnóstico': TipoProcesso.DIAGNOSTICO
  }
  return MAPPINGS[tipo] || TipoProcesso.MAPEAMENTO
}

export function mapSituacaoProcesso(situacao: string): SituacaoProcesso {
  const MAPPINGS: Record<string, SituacaoProcesso> = {
    CRIADO: SituacaoProcesso.CRIADO,
    EM_ANDAMENTO: SituacaoProcesso.EM_ANDAMENTO,
    FINALIZADO: SituacaoProcesso.FINALIZADO,
    'Criado': SituacaoProcesso.CRIADO,
    'Em andamento': SituacaoProcesso.EM_ANDAMENTO,
    'Finalizado': SituacaoProcesso.FINALIZADO
  }
  return MAPPINGS[situacao] || SituacaoProcesso.CRIADO
}

// DTO do backend para a lista de Processos
interface ProcessoDto {
  id: number
  descricao: string
  tipo: string
  situacao: string
  dataLimite: string
  dataFinalizacao: string | null
}

export const useProcessosStore = defineStore('processos', {
  state: () => ({
    processos: [] as Processo[],
    loading: false,
    error: null as string | null
  }),

  actions: {
    /**
     * Carrega a lista de processos da API.
     */
    async carregarProcessos() {
      if (this.processos.length > 0) return // Evita recargas desnecessárias

      this.loading = true
      this.error = null
      const api = useApi()

      try {
        const processosDto = await api.get<ProcessoDto[]>('/processos')
        this.processos = processosDto.map(
          (dto): Processo => ({
            id: dto.id,
            descricao: dto.descricao,
            tipo: mapTipoProcesso(dto.tipo),
            situacao: mapSituacaoProcesso(dto.situacao),
            dataLimite: parseDate(dto.dataLimite) || new Date(),
            dataFinalizacao: dto.dataFinalizacao ? parseDate(dto.dataFinalizacao) : null
          })
        )
      } catch (error) {
        this.error = (error as Error).message
      } finally {
        this.loading = false
      }
    },

    /**
     * Carrega os detalhes de um processo específico, incluindo seus subprocessos.
     * Esta ação agora delega o carregamento de subprocessos para o store dedicado.
     */
    async carregarDetalhesProcesso(idProcesso: number) {
      const subprocessosStore = useSubprocessosStore()
      await subprocessosStore.carregarSubprocessos(idProcesso)
    },

    /**
     * Ação para finalizar um processo.
     * Esta é uma chamada de API que altera o estado no backend.
     * Após a chamada, o estado local é atualizado para refletir a mudança.
     */
    async finalizarProcesso(idProcesso: number) {
      const processo = this.processos.find((p) => p.id === idProcesso)
      if (!processo) return

      // Idealmente, haveria um estado de 'saving' ou 'updating' aqui
      const api = useApi()
      try {
        // A API não retorna o processo atualizado, então atualizamos o estado local manualmente
        await api.post(`/processos/${idProcesso}/finalizar`, {})
        processo.situacao = SituacaoProcesso.FINALIZADO
        processo.dataFinalizacao = new Date()
      } catch (error) {
        // Tratar erro de finalização
        console.error('Erro ao finalizar o processo:', error)
        // Opcional: reverter o estado ou mostrar uma notificação de erro
      }
    },

    // Ações de mock (adicionar, editar, remover) foram removidas.
    // A interação com a API para essas operações será implementada futuramente.

    reset() {
      this.processos = []
      this.loading = false
      this.error = null
    }
  }
})