import { defineStore } from 'pinia'
import { Perfil } from '@/types/tipos'
import { useApi } from '@/composables/useApi'

// Tipos para os DTOs da API de autenticação
interface LoginRequest {
  tituloEleitoral: string
  senha?: string // Senha é opcional no backend por enquanto
}

// DTOs alinhados com o backend
interface LoginRequest {
  tituloEleitoral: string;
  senha?: string;
}

interface PerfilUnidadeDto {
  perfil: Perfil;
  unidade: string;
  unidadeCodigo: number;
}

interface LoginResponse {
  nome: string;
  tituloEleitoral: string;
  pares: PerfilUnidadeDto[];
}

interface EntrarRequest {
  tituloEleitoral: string;
  perfil: Perfil;
  unidadeCodigo: number;
}

interface UsuarioAutenticado {
  nome: string;
  tituloEleitoral: string;
  perfil: Perfil;
  unidade: string;
  token: string;
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
      this.autenticando = true;
      this.erroAutenticacao = null;
      const api = useApi();

      try {
        await api.post('/usuarios/autenticar', credenciais);
        const response = await api.get<LoginResponse>(`/usuarios/autorizar/${credenciais.tituloEleitoral}`);
        this.loginResponse = response;

        if (response.pares.length === 1) {
          await this.entrar(response.pares[0]);
        }
      } catch (error) {
        this.erroAutenticacao = (error as Error).message;
        throw error;
      } finally {
        this.autenticando = false;
      }
    },

    async entrar(par: PerfilUnidadeDto) {
      if (!this.loginResponse) {
        throw new Error('Autenticação não iniciada.');
      }

      this.autenticando = true;
      this.erroAutenticacao = null;
      const api = useApi();

      const request: EntrarRequest = {
        tituloEleitoral: this.loginResponse.tituloEleitoral,
        perfil: par.perfil,
        unidadeCodigo: par.unidadeCodigo,
      };

      try {
        const usuario = await api.post<UsuarioAutenticado>('/usuarios/entrar', request);
        this.usuario = usuario;
        localStorage.setItem('usuario', JSON.stringify(usuario));
      } catch (error) {
        this.erroAutenticacao = (error as Error).message;
        throw error;
      } finally {
        this.autenticando = false;
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