import { beforeEach, describe, expect, it, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useSubprocessosStore } from '../subprocessos'
import { useApi } from '@/composables/useApi'
import { SITUACOES_SUBPROCESSO } from '@/constants/situacoes'

// Mocking composables
vi.mock('@/composables/useApi')

const mockApi = {
  get: vi.fn(),
  post: vi.fn()
}

describe('useSubprocessosStore', () => {
  let store: ReturnType<typeof useSubprocessosStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useSubprocessosStore()
    vi.mocked(useApi).mockReturnValue(mockApi)
    vi.clearAllMocks()
  })

  it('initial state is correct', () => {
    expect(store.subprocessos).toEqual([])
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })

  describe('actions', () => {
    describe('carregarSubprocessos', () => {
      const mockProcessoDetalheDto = {
        unidades: [
          {
            codUnidade: 101,
            sigla: 'SIGLA1',
            nome: 'Unidade 1',
            codUnidadeSuperior: null,
            situacaoSubprocesso: 'NAO_INICIADO',
            dataLimite: '2024-12-31'
          },
          {
            codUnidade: 102,
            sigla: 'SIGLA2',
            nome: 'Unidade 2',
            codUnidadeSuperior: 101,
            situacaoSubprocesso: 'CADASTRO_DISPONIBILIZADO',
            dataLimite: '2024-11-30'
          }
        ]
        // ... outras propriedades do processo
      }

      it('should load and map subprocessos from the API', async () => {
        mockApi.get.mockResolvedValue(mockProcessoDetalheDto)
        const idProcesso = 1

        await store.carregarSubprocessos(idProcesso)

        expect(store.loading).toBe(false)
        expect(store.error).toBeNull()
        expect(store.subprocessos.length).toBe(2)
        expect(store.subprocessos[0]).toEqual({
          id: 101,
          idProcesso: 1,
          unidade: 'SIGLA1',
          situacao: SITUACOES_SUBPROCESSO.NAO_INICIADO,
          unidadeAtual: 'SIGLA1',
          unidadeAnterior: null,
          dataLimiteEtapa1: new Date('2024-12-31'),
          dataFimEtapa1: null,
          dataLimiteEtapa2: null,
          dataFimEtapa2: null,
          sugestoes: undefined,
          observacoes: undefined,
          movimentacoes: [],
          analises: [],
          idMapaCopiado: undefined
        })
        expect(store.subprocessos[1].situacao).toBe(SITUACOES_SUBPROCESSO.CADASTRO_DISPONIBILIZADO)
        expect(mockApi.get).toHaveBeenCalledWith(`/processos/${idProcesso}/detalhes`)
      })

      it('should not fetch if subprocessos for the given idProcesso are already loaded', async () => {
        const idProcesso = 1
        store.subprocessos = [{ id: 101, idProcesso } as any]
        await store.carregarSubprocessos(idProcesso)
        expect(mockApi.get).not.toHaveBeenCalled()
      })

      it('should handle API errors correctly', async () => {
        const error = new Error('Failed to fetch')
        mockApi.get.mockRejectedValue(error)
        const idProcesso = 1

        await store.carregarSubprocessos(idProcesso)

        expect(store.loading).toBe(false)
        expect(store.error).toBe('Failed to fetch')
        expect(store.subprocessos).toEqual([])
      })
    })

    describe('reset', () => {
      it('should reset the store to its initial state', () => {
        store.subprocessos = [{} as any]
        store.loading = true
        store.error = 'Some error'

        store.reset()

        expect(store.subprocessos).toEqual([])
        expect(store.loading).toBe(false)
        expect(store.error).toBeNull()
      })
    })
  })

  describe('getters', () => {
    beforeEach(() => {
      // Pre-populate the store with some data for getter tests
      store.subprocessos = [
        { id: 1, idProcesso: 1, unidade: 'U1', situacao: SITUACOES_SUBPROCESSO.CADASTRO_DISPONIBILIZADO, unidadeAtual: 'U1' },
        { id: 2, idProcesso: 1, unidade: 'U2', situacao: SITUACOES_SUBPROCESSO.REVISAO_CADASTRO_DISPONIBILIZADA, unidadeAtual: 'U2' },
        { id: 3, idProcesso: 2, unidade: 'U3', situacao: SITUACOES_SUBPROCESSO.MAPA_CRIADO, unidadeAtual: 'U3' }
      ] as any[]
    })

    it('getUnidadesDoProcesso should filter subprocessos by idProcesso', () => {
      const result = store.getUnidadesDoProcesso(1)
      expect(result.length).toBe(2)
      expect(result[0].unidade).toBe('U1')
      expect(result[1].unidade).toBe('U2')
    })

    it('getSubprocessosElegiveisAceiteBloco should filter correctly', () => {
      const result = store.getSubprocessosElegiveisAceiteBloco(1, 'U1')
      expect(result.length).toBe(1)
      expect(result[0].unidade).toBe('U1')
    })

    it('getSubprocessosElegiveisHomologacaoBloco should filter correctly', () => {
      const result = store.getSubprocessosElegiveisHomologacaoBloco(1)
      expect(result.length).toBe(2)
      expect(result.map(sp => sp.unidade)).toEqual(['U1', 'U2'])
    })

    it('getMovementsForSubprocesso should return sorted movements', () => {
      const now = new Date()
      const yesterday = new Date(now.getTime() - 86400000)
      store.subprocessos = [
        {
          id: 1,
          idProcesso: 1,
          unidade: 'U1',
          situacao: 'A',
          unidadeAtual: 'U1',
          movimentacoes: [
            { id: 10, dataHora: yesterday },
            { id: 11, dataHora: now }
          ]
        }
      ] as any[]

      const result = store.getMovementsForSubprocesso(1)
      expect(result.length).toBe(2)
      expect(result[0].id).toBe(11) // Most recent first
      expect(result[1].id).toBe(10)
    })

    it('getMovementsForSubprocesso should return empty array if no subprocesso found', () => {
        const result = store.getMovementsForSubprocesso(999)
        expect(result).toEqual([])
    })
  })
})