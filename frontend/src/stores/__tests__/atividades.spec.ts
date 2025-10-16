import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAtividadesStore } from '../atividades'
import { useApi } from '@/composables/useApi'
import type { SubprocessoCadastroDto } from '@/types/SubprocessoCadastroDto'
import type { Atividade } from '@/types/tipos'

// Mock the useApi composable
vi.mock('@/composables/useApi', () => ({
  useApi: vi.fn()
}))

const mockSubprocessoCadastro: SubprocessoCadastroDto.SubprocessoCadastro = {
  subprocessoId: 1,
  unidadeSigla: 'TEST',
  atividades: [
    {
      id: 1,
      descricao: 'Atividade de Teste 1',
      conhecimentos: [{ id: 101, descricao: 'Conhecimento de Teste 1' }]
    },
    {
      id: 2,
      descricao: 'Atividade de Teste 2',
      conhecimentos: []
    }
  ]
}

describe('useAtividadesStore', () => {
  let store: ReturnType<typeof useAtividadesStore>
  const mockApi = {
    get: vi.fn()
  }

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.mocked(useApi).mockReturnValue(mockApi)
    store = useAtividadesStore()
    store.$reset() // Reset store state before each test
    mockApi.get.mockClear()
  })

  it('should initialize with correct default state', () => {
    expect(store.atividades).toEqual([])
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })

  describe('actions', () => {
    describe('carregarAtividades', () => {
      it('should fetch and map atividades from the API', async () => {
        mockApi.get.mockResolvedValue(mockSubprocessoCadastro)

        await store.carregarAtividades(1)

        expect(store.loading).toBe(false)
        expect(store.error).toBeNull()
        expect(mockApi.get).toHaveBeenCalledWith('/subprocessos/1/cadastro')

        const atividades = store.getAtividadesPorSubprocesso(1)
        expect(atividades).toHaveLength(2)
        expect(atividades[0].id).toBe(1)
        expect(atividades[0].descricao).toBe('Atividade de Teste 1')
        expect(atividades[0].idSubprocesso).toBe(1)
        expect(atividades[0].conhecimentos[0].descricao).toBe('Conhecimento de Teste 1')
      })

      it('should not fetch if data for the subprocesso already exists', async () => {
        // Pre-populate the store
        store.atividades = [
          {
            id: 1,
            idSubprocesso: 1,
            descricao: 'Atividade Existente',
            conhecimentos: []
          }
        ]

        await store.carregarAtividades(1)

        expect(mockApi.get).not.toHaveBeenCalled()
        expect(store.loading).toBe(false)
      })

      it('should handle API errors', async () => {
        const errorMessage = 'Failed to fetch'
        mockApi.get.mockRejectedValue(new Error(errorMessage))

        await store.carregarAtividades(2)

        expect(store.loading).toBe(false)
        expect(store.error).toBe(errorMessage)
        expect(store.atividades).toEqual([])
      })

      it('should set loading state correctly', async () => {
        mockApi.get.mockResolvedValue(mockSubprocessoCadastro)
        const promise = store.carregarAtividades(1)
        expect(store.loading).toBe(true)
        await promise
        expect(store.loading).toBe(false)
      })

      it('should correctly add new atividades without removing ones from other subprocessos', async () => {
        // Pre-populate with data from another subprocesso
        const atividadeExistente: Atividade = {
          id: 99,
          idSubprocesso: 9,
          descricao: 'Atividade de Outro Processo',
          conhecimentos: []
        }
        store.atividades = [atividadeExistente]

        mockApi.get.mockResolvedValue(mockSubprocessoCadastro)
        await store.carregarAtividades(1)

        expect(store.atividades).toHaveLength(3) // 1 existing + 2 new
        expect(store.atividades).toEqual(
          expect.arrayContaining([expect.objectContaining(atividadeExistente)])
        )
        expect(store.getAtividadesPorSubprocesso(1)).toHaveLength(2)
      })
    })
  })
})