import { defineStore } from 'pinia'
import { Movimentacao, Subprocesso } from '@/types/tipos'
import { parseDate } from '@/utils'
import { SITUACOES_SUBPROCESSO } from '@/constants/situacoes'
import { useApi } from '@/composables/useApi'
import { generateUniqueId } from '../utils'

// DTO do backend para a lista de subprocessos (unidades participantes)
interface SubprocessoDto {
  nome: string
  sigla: string
  codUnidade: number
  codUnidadeSuperior: number | null
  situacaoSubprocesso: keyof typeof SITUACOES_SUBPROCESSO
  dataLimite: string | null
}

// DTO para o response completo do endpoint de detalhes do processo
interface ProcessoDetalheDto {
  unidades: SubprocessoDto[]
  // outras propriedades do processo...
}

function parseSubprocesso(dto: SubprocessoDto, idProcesso: number): Subprocesso {
  const dataLimite = dto.dataLimite ? parseDate(dto.dataLimite) : new Date()
  return {
    id: dto.codUnidade, // Usando codUnidade como ID único para o subprocesso
    idProcesso: idProcesso,
    unidade: dto.sigla,
    situacao: SITUACOES_SUBPROCESSO[dto.situacaoSubprocesso] || SITUACOES_SUBPROCESSO.NAO_INICIADO,
    unidadeAtual: dto.sigla,
    unidadeAnterior: null, // A API não fornece essa informação diretamente na lista
    dataLimiteEtapa1: dataLimite,
    dataFimEtapa1: null, // A API não fornece
    dataLimiteEtapa2: null, // A API não fornece
    dataFimEtapa2: null, // A API não fornece
    sugestoes: undefined,
    observacoes: undefined,
    movimentacoes: [],
    analises: [],
    idMapaCopiado: undefined
  }
}

export const useSubprocessosStore = defineStore('subprocessos', {
  state: () => ({
    subprocessos: [] as Subprocesso[],
    loading: false,
    error: null as string | null
  }),

  getters: {
    getUnidadesDoProcesso: (state) => (idProcesso: number): Subprocesso[] => {
      return state.subprocessos.filter((pu) => pu.idProcesso === idProcesso)
    },
    getSubprocessosElegiveisAceiteBloco:
      (state) => (idProcesso: number, siglaUnidadeUsuario: string) => {
        return state.subprocessos.filter(
          (pu) =>
            pu.idProcesso === idProcesso &&
            pu.unidadeAtual === siglaUnidadeUsuario &&
            (pu.situacao === SITUACOES_SUBPROCESSO.CADASTRO_DISPONIBILIZADO ||
              pu.situacao === SITUACOES_SUBPROCESSO.REVISAO_CADASTRO_DISPONIBILIZADA)
        )
      },
    getSubprocessosElegiveisHomologacaoBloco: (state) => (idProcesso: number) => {
      return state.subprocessos.filter(
        (pu) =>
          pu.idProcesso === idProcesso &&
          (pu.situacao === SITUACOES_SUBPROCESSO.CADASTRO_DISPONIBILIZADO ||
            pu.situacao === SITUACOES_SUBPROCESSO.REVISAO_CADASTRO_DISPONIBILIZADA)
      )
    },
    getMovementsForSubprocesso: (state) => (idSubprocesso: number) => {
      const subprocesso = state.subprocessos.find((sp) => sp.id === idSubprocesso)
      return subprocesso
        ? subprocesso.movimentacoes.sort(
            (a: Movimentacao, b: Movimentacao) => b.dataHora.getTime() - a.dataHora.getTime()
          )
        : []
    }
  },

  actions: {
    async carregarSubprocessos(idProcesso: number) {
      // Evita recarregar se os dados para este processo já existem
      if (this.subprocessos.some((sp) => sp.idProcesso === idProcesso)) {
        return
      }

      this.loading = true
      this.error = null
      const api = useApi()

      try {
        const data = await api.get<ProcessoDetalheDto>(`/processos/${idProcesso}/detalhes`)
        const novosSubprocessos = data.unidades.map((dto) => parseSubprocesso(dto, idProcesso))

        // Adiciona os novos subprocessos ao estado, evitando duplicatas
        const subprocessosAtuais = this.subprocessos.filter((sp) => sp.idProcesso !== idProcesso)
        this.subprocessos = [...subprocessosAtuais, ...novosSubprocessos]
      } catch (err) {
        this.error = (err as Error).message
        this.subprocessos = [] // Limpa em caso de erro para evitar dados inconsistentes
      } finally {
        this.loading = false
      }
    },

    reset() {
      this.subprocessos = []
      this.error = null
      this.loading = false
    }
  }
})