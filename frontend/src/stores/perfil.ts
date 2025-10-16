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
        // O backend real tem um fluxo em múltiplos passos
        await api.post('/usuarios/autenticar', credenciais)
        const perfisResponse = await api.post<PerfilUnidadeDto[]>('/usuarios/autorizar', credenciais.tituloEleitoral)

        // Simula a resposta do loginResponse que a UI espera
        this.loginResponse = {
            nome: "Usuário", // O nome não vem na resposta, pode ser ajustado no futuro
            tituloEleitoral: credenciais.tituloEleitoral,
            pares: perfisResponse
        }

        // Se houver apenas um par, já finaliza o login
        if (this.loginResponse.pares.length === 1) {
          await this.entrar(this.loginResponse.pares[0])
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

      const request = { // O DTO do backend espera `unidadeCodigo`
        tituloEleitoral: this.loginResponse.tituloEleitoral,
        perfil: par.perfil,
        unidadeCodigo: par.unidade.id // Ajustado para enviar o código
      }

      try {
        await api.post('/usuarios/entrar', request);

        // Simula a criação do objeto de usuário local após o sucesso
        const usuario: UsuarioAutenticado = {
          nome: this.loginResponse.nome,
          tituloEleitoral: this.loginResponse.tituloEleitoral,
          perfil: par.perfil,
          unidade: par.unidade.sigla, // Armazena a sigla na UI
          token: 'token-simulado' // O backend não retorna token, mantemos simulado
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