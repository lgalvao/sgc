import { defineStore } from 'pinia'
import { Perfil } from '@/types/tipos'
import { useApi } from '@/composables/useApi'

// Tipos para os DTOs da API de autenticação
interface LoginRequest {
  tituloEleitoral: string
  senha?: string // Senha é opcional no backend por enquanto
}

interface PerfilUnidadeDto {
  perfil: Perfil
  unidade: string
}

interface LoginResponse {
  nome: string
  tituloEleitoral: string
  pares: PerfilUnidadeDto[]
}

interface EntrarRequest {
  tituloEleitoral: string
  perfil: Perfil
  unidade: string
}

interface UsuarioAutenticado {
  nome: string
  tituloEleitoral: string
  perfil: Perfil
  unidade: string
  token: string // O backend ainda não envia, mas já vamos prever
}

export const usePerfilStore = defineStore('perfil', {
  state: () => ({
    // Estado de autenticação
    usuario: JSON.parse(
      localStorage.getItem('usuario') || 'null'
    ) as UsuarioAutenticado | null,
    autenticando: false,
    erroAutenticacao: null as string | null,

    // Estado do fluxo de múltiplos perfis
    loginResponse: null as LoginResponse | null
  }),

  getters: {
    estaAutenticado: (state) => !!state.usuario,
    perfilSelecionado: (state) => state.usuario?.perfil || null,
    unidadeSelecionada: (state) => state.usuario?.unidade || null,
    servidorId: (state) => (state.usuario ? Number(state.usuario.tituloEleitoral) : null)
  },

  actions: {
    /**
     * Passo 1 do Login: Autentica o usuário e busca os perfis disponíveis.
     */
    async autenticar(credenciais: LoginRequest) {
      this.autenticando = true
      this.erroAutenticacao = null
      const api = useApi()

      try {
        const response = await api.post<LoginResponse>('/login/autenticar', credenciais)
        this.loginResponse = response

        // Se houver apenas um par, já finaliza o login
        if (response.pares.length === 1) {
          await this.entrar(response.pares[0])
        }
        // Se houver múltiplos pares, a UI irá solicitar a seleção
      } catch (error) {
        this.erroAutenticacao = (error as Error).message
        throw error
      } finally {
        this.autenticando = false
      }
    },

    /**
     * Passo 2 do Login: Confirma o perfil/unidade escolhido e finaliza o login.
     */
    async entrar(par: PerfilUnidadeDto) {
      if (!this.loginResponse) {
        throw new Error('Autenticação não iniciada.')
      }

      this.autenticando = true
      this.erroAutenticacao = null
      const api = useApi()

      const request: EntrarRequest = {
        tituloEleitoral: this.loginResponse.tituloEleitoral,
        perfil: par.perfil,
        unidade: par.unidade
      }

      try {
        // O backend ainda não tem o endpoint 'entrar', vamos simular a resposta
        // const response = await api.post<UsuarioAutenticado>('/login/entrar', request);
        const usuario: UsuarioAutenticado = {
          nome: this.loginResponse.nome,
          tituloEleitoral: this.loginResponse.tituloEleitoral,
          perfil: par.perfil,
          unidade: par.unidade,
          token: 'token-simulado' // Token simulado
        }

        this.usuario = usuario
        localStorage.setItem('usuario', JSON.stringify(this.usuario))
      } catch (error) {
        this.erroAutenticacao = (error as Error).message
        throw error
      } finally {
        this.autenticando = false
      }
    },

    /**
     * Realiza o logout do usuário.
     */
    logout() {
      this.usuario = null
      this.loginResponse = null
      localStorage.removeItem('usuario')
      // Idealmente, invalidar o token no backend também
      // await api.post('/logout');
    }
  }
})