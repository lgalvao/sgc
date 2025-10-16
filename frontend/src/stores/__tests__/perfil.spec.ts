import { setActivePinia, createPinia } from 'pinia'
import { usePerfilStore } from '../perfil'
import { vi } from 'vitest'

// Mock do `useApi`
const mockApi = {
  post: vi.fn()
}
vi.mock('@/composables/useApi', () => ({
  useApi: () => mockApi
}))

describe('usePerfilStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    // Limpa o localStorage antes de cada teste
    localStorage.clear()
    // Reseta os mocks
    vi.clearAllMocks()
  })

  it('should initialize with a null user', () => {
    const perfilStore = usePerfilStore()
    expect(perfilStore.usuario).toBeNull()
    expect(perfilStore.estaAutenticado).toBe(false)
  })

  describe('autenticar action', () => {
    it('should authenticate, store loginResponse, and call entrar for single profile', async () => {
      const perfilStore = usePerfilStore()
      const mockLoginResponse = {
        nome: 'Usuario Teste',
        tituloEleitoral: '123',
        pares: [{ perfil: 'CHEFE', unidade: 'TJPE' }]
      }
      mockApi.post.mockResolvedValue(mockLoginResponse)

      await perfilStore.autenticar({ tituloEleitoral: '123', senha: '123' })

      expect(mockApi.post).toHaveBeenCalledWith('/login/autenticar', {
        tituloEleitoral: '123',
        senha: '123'
      })
      expect(perfilStore.loginResponse).toEqual(mockLoginResponse)
      expect(perfilStore.estaAutenticado).toBe(true)
      expect(perfilStore.usuario?.nome).toBe('Usuario Teste')
      expect(perfilStore.usuario?.perfil).toBe('CHEFE')
    })

    it('should authenticate and store loginResponse for multiple profiles', async () => {
      const perfilStore = usePerfilStore()
      const mockLoginResponse = {
        nome: 'Usuario Teste',
        tituloEleitoral: '123',
        pares: [
          { perfil: 'CHEFE', unidade: 'TJPE' },
          { perfil: 'ADMIN', unidade: 'SEDOC' }
        ]
      }
      mockApi.post.mockResolvedValue(mockLoginResponse)

      await perfilStore.autenticar({ tituloEleitoral: '123', senha: '123' })

      expect(perfilStore.loginResponse).toEqual(mockLoginResponse)
      expect(perfilStore.estaAutenticado).toBe(false) // Não deve logar automaticamente
    })

    it('should handle authentication errors', async () => {
      const perfilStore = usePerfilStore()
      mockApi.post.mockRejectedValue(new Error('Credenciais inválidas'))

      await expect(
        perfilStore.autenticar({ tituloEleitoral: '123', senha: '123' })
      ).rejects.toThrow('Credenciais inválidas')

      expect(perfilStore.erroAutenticacao).toBe('Credenciais inválidas')
      expect(perfilStore.estaAutenticado).toBe(false)
    })
  })

  describe('entrar action', () => {
    it('should set the user and store it in localStorage', async () => {
      const perfilStore = usePerfilStore()
      // Pre-condição: autenticar foi chamado
      perfilStore.loginResponse = {
        nome: 'Usuario Teste',
        tituloEleitoral: '123',
        pares: [{ perfil: 'CHEFE', unidade: 'TJPE' }]
      }

      await perfilStore.entrar({ perfil: 'CHEFE', unidade: 'TJPE' })

      expect(perfilStore.estaAutenticado).toBe(true)
      expect(perfilStore.usuario?.unidade).toBe('TJPE')
      expect(localStorage.getItem('usuario')).not.toBeNull()
      const storedUser = JSON.parse(localStorage.getItem('usuario')!)
      expect(storedUser.unidade).toBe('TJPE')
    })
  })

  describe('logout action', () => {
    it('should clear user data from state and localStorage', () => {
      const perfilStore = usePerfilStore()
      // Pre-condição: usuário logado
      perfilStore.usuario = {
        nome: 'Usuario Teste',
        tituloEleitoral: '123',
        perfil: 'CHEFE',
        unidade: 'TJPE',
        token: 'token'
      }
      localStorage.setItem('usuario', JSON.stringify(perfilStore.usuario))

      perfilStore.logout()

      expect(perfilStore.usuario).toBeNull()
      expect(perfilStore.estaAutenticado).toBe(false)
      expect(localStorage.getItem('usuario')).toBeNull()
    })
  })
})