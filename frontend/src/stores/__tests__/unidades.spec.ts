import { setActivePinia, createPinia } from 'pinia'
import { useUnidadesStore } from '../unidades'
import { vi } from 'vitest'
import unidadesMock from '../../mocks/unidades.json'
import { Unidade } from '@/types/tipos'

// Mock do `useApi`
const mockApi = {
  get: vi.fn()
}
vi.mock('@/composables/useApi', () => ({
  useApi: () => mockApi
}))

describe('useUnidadesStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    // Mock da resposta da API
    mockApi.get.mockResolvedValue(unidadesMock as Unidade[])
  })

  it('should not have unidades on initialization', () => {
    const unidadesStore = useUnidadesStore()
    expect(unidadesStore.unidades.length).toBe(0)
  })

  it('should load unidades from the API', async () => {
    const unidadesStore = useUnidadesStore()
    await unidadesStore.carregarUnidades()
    expect(mockApi.get).toHaveBeenCalledWith('/unidades')
    expect(unidadesStore.unidades.length).toBeGreaterThan(0)
    expect(unidadesStore.unidades[0].sigla).toBe('SEDOC')
  })

  it('should not fetch unidades if they are already loaded', async () => {
    const unidadesStore = useUnidadesStore()
    await unidadesStore.carregarUnidades()
    expect(mockApi.get).toHaveBeenCalledTimes(1)
    // Chamar de novo
    await unidadesStore.carregarUnidades()
    expect(mockApi.get).toHaveBeenCalledTimes(1) // Não deve chamar a API novamente
  })

  describe('actions using loaded data', () => {
    let unidadesStore: ReturnType<typeof useUnidadesStore>

    beforeEach(async () => {
      unidadesStore = useUnidadesStore()
      await unidadesStore.carregarUnidades()
    })

    it('pesquisarUnidade should find a unit by sigla', () => {
      const unidade = unidadesStore.pesquisarUnidade('SEDESENV')
      expect(unidade).toBeDefined()
      expect(unidade?.nome).toBe('Seção de Desenvolvimento de Sistemas')
    })

    it('getUnidadesSubordinadas should return subordinate units', () => {
      const subordinadas = unidadesStore.getUnidadesSubordinadas('STIC')
      expect(subordinadas).toContain('COSIS')
      expect(subordinadas).toContain('SEDESENV')
    })

    it('getUnidadeSuperior should return the superior unit', () => {
      const superior = unidadesStore.getUnidadeSuperior('SEDESENV')
      expect(superior).toBe('COSIS')
    })
  })
})