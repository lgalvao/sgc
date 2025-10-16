import { beforeEach, describe, expect, it, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useProcessosStore, mapSituacaoProcesso, mapTipoProcesso } from '../processos'
import { useSubprocessosStore } from '../subprocessos'
import { useApi } from '@/composables/useApi'
import { Processo, SituacaoProcesso, TipoProcesso } from '@/types/tipos'

// Mocking composables and stores
vi.mock('@/composables/useApi')
vi.mock('../subprocessos')

const mockApi = {
  get: vi.fn(),
  post: vi.fn()
}

const mockSubprocessosStore = {
  carregarSubprocessos: vi.fn()
}

describe('useProcessosStore', () => {
  let store: ReturnType<typeof useProcessosStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useProcessosStore()
    vi.mocked(useApi).mockReturnValue(mockApi)
    vi.mocked(useSubprocessosStore).mockReturnValue(mockSubprocessosStore)
    vi.clearAllMocks()
  })

  it('initial state is empty', () => {
    expect(store.processos).toEqual([])
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })

  describe('actions', () => {
    describe('carregarProcessos', () => {
      const mockProcessosDto = [
        {
          id: 1,
          descricao: 'Processo 1',
          tipo: 'MAPEAMENTO',
          situacao: 'CRIADO',
          dataLimite: '2024-01-01',
          dataFinalizacao: null
        },
        {
          id: 2,
          descricao: 'Processo 2',
          tipo: 'REVISAO',
          situacao: 'EM_ANDAMENTO',
          dataLimite: '2024-02-01',
          dataFinalizacao: null
        }
      ]

      it('should load and map processos from API', async () => {
        mockApi.get.mockResolvedValue(mockProcessosDto)

        await store.carregarProcessos()

        expect(store.loading).toBe(false)
        expect(store.error).toBeNull()
        expect(store.processos.length).toBe(2)
        expect(store.processos[0]).toEqual({
          id: 1,
          descricao: 'Processo 1',
          tipo: TipoProcesso.MAPEAMENTO,
          situacao: SituacaoProcesso.CRIADO,
          dataLimite: new Date('2024-01-01'),
          dataFinalizacao: null
        })
        expect(store.processos[1].tipo).toBe(TipoProcesso.REVISAO)
        expect(mockApi.get).toHaveBeenCalledWith('/processos')
      })

      it('should not fetch if processos are already loaded', async () => {
        store.processos = [{} as Processo] // Simulate already loaded
        await store.carregarProcessos()
        expect(mockApi.get).not.toHaveBeenCalled()
      })

      it('should handle API errors', async () => {
        const error = new Error('API Error')
        mockApi.get.mockRejectedValue(error)

        await store.carregarProcessos()

        expect(store.loading).toBe(false)
        expect(store.error).toBe('API Error')
        expect(store.processos).toEqual([])
      })
    })

    describe('carregarDetalhesProcesso', () => {
      it('should delegate to useSubprocessosStore to load subprocessos', async () => {
        const idProcesso = 123
        await store.carregarDetalhesProcesso(idProcesso)
        expect(mockSubprocessosStore.carregarSubprocessos).toHaveBeenCalledWith(idProcesso)
      })
    })

    describe('finalizarProcesso', () => {
      it('should call the API and update the process state on success', async () => {
        const processo: Processo = {
          id: 1,
          descricao: 'Processo Teste',
          tipo: TipoProcesso.MAPEAMENTO,
          situacao: SituacaoProcesso.EM_ANDAMENTO,
          dataLimite: new Date(),
          dataFinalizacao: null
        }
        store.processos = [processo]
        mockApi.post.mockResolvedValue({})

        await store.finalizarProcesso(1)

        expect(mockApi.post).toHaveBeenCalledWith('/processos/1/finalizar', {})
        const processoFinalizado = store.processos.find(p => p.id === 1)
        expect(processoFinalizado?.situacao).toBe(SituacaoProcesso.FINALIZADO)
        expect(processoFinalizado?.dataFinalizacao).toBeInstanceOf(Date)
      })

      it('should not change state if API call fails', async () => {
        const processo: Processo = {
          id: 1,
          descricao: 'Processo Teste',
          tipo: TipoProcesso.MAPEAMENTO,
          situacao: SituacaoProcesso.EM_ANDAMENTO,
          dataLimite: new Date(),
          dataFinalizacao: null
        }
        store.processos = [processo]
        const error = new Error('Finalization failed')
        mockApi.post.mockRejectedValue(error)

        await store.finalizarProcesso(1)

        const processoNaoFinalizado = store.processos.find(p => p.id === 1)
        expect(processoNaoFinalizado?.situacao).toBe(SituacaoProcesso.EM_ANDAMENTO)
        expect(processoNaoFinalizado?.dataFinalizacao).toBeNull()
      })

      it('should do nothing if process is not found', async () => {
        await store.finalizarProcesso(999)
        expect(mockApi.post).not.toHaveBeenCalled()
      })
    })

    describe('reset', () => {
      it('should reset the store to its initial state', () => {
        store.processos = [{} as Processo]
        store.loading = true
        store.error = 'An error'

        store.reset()

        expect(store.processos).toEqual([])
        expect(store.loading).toBe(false)
        expect(store.error).toBeNull()
      })
    })
  })

  describe('mappers', () => {
    it('mapTipoProcesso handles various inputs correctly', () => {
      expect(mapTipoProcesso('MAPEAMENTO')).toBe(TipoProcesso.MAPEAMENTO)
      expect(mapTipoProcesso('REVISAO')).toBe(TipoProcesso.REVISAO)
      expect(mapTipoProcesso('DIAGNOSTICO')).toBe(TipoProcesso.DIAGNOSTICO)
      expect(mapTipoProcesso('Mapeamento')).toBe(TipoProcesso.MAPEAMENTO)
      expect(mapTipoProcesso('Revisão')).toBe(TipoProcesso.REVISAO)
      expect(mapTipoProcesso('Diagnóstico')).toBe(TipoProcesso.DIAGNOSTICO)
      expect(mapTipoProcesso('INVALIDO')).toBe(TipoProcesso.MAPEAMENTO)
    })

    it('mapSituacaoProcesso handles various inputs correctly', () => {
      expect(mapSituacaoProcesso('CRIADO')).toBe(SituacaoProcesso.CRIADO)
      expect(mapSituacaoProcesso('EM_ANDAMENTO')).toBe(SituacaoProcesso.EM_ANDAMENTO)
      expect(mapSituacaoProcesso('FINALIZADO')).toBe(SituacaoProcesso.FINALIZADO)
      expect(mapSituacaoProcesso('Criado')).toBe(SituacaoProcesso.CRIADO)
      expect(mapSituacaoProcesso('Em andamento')).toBe(SituacaoProcesso.EM_ANDAMENTO)
      expect(mapSituacaoProcesso('Finalizado')).toBe(SituacaoProcesso.FINALIZADO)
      expect(mapSituacaoProcesso('INVALIDO')).toBe(SituacaoProcesso.CRIADO)
    })
  })
})
