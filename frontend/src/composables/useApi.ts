import { usePerfilStore } from '@/stores/perfil'

// Definição de tipos para as opções da requisição
interface RequestOptions extends RequestInit {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  body?: any
}

/**
 * Hook personalizado (composable) para realizar chamadas de API.
 * Ele encapsula a lógica do `fetch`, adiciona o cabeçalho de autenticação
 * e trata a serialização/desserialização de JSON.
 *
 * @returns Um objeto com métodos para os verbos HTTP (get, post, put, delete, patch).
 */
export function useApi() {
  const perfilStore = usePerfilStore()

  /**
   * Realiza uma requisição HTTP para a API.
   *
   * @param endpoint - O caminho do endpoint da API (ex: '/processos').
   * @param options - As opções da requisição (`method`, `body`, etc.).
   * @returns A resposta da API em formato JSON.
   * @throws Um erro se a resposta da rede não for 'ok'.
   */
  async function request<T>(endpoint: string, options: RequestOptions = {}): Promise<T> {
    const defaultHeaders: HeadersInit = {
      'Content-Type': 'application/json'
      // Futuramente, o token de autenticação será adicionado aqui
      // Authorization: `Bearer ${perfilStore.token}`
    }

    const config: RequestInit = {
      ...options,
      headers: {
        ...defaultHeaders,
        ...options.headers
      }
    }

    // Serializa o corpo da requisição se ele for um objeto
    if (config.body && typeof config.body === 'object') {
      config.body = JSON.stringify(config.body)
    }

    // Constrói a URL completa da API
    const url = `/api${endpoint}`

    const response = await fetch(url, config)

    if (!response.ok) {
      // Tenta extrair uma mensagem de erro do corpo da resposta
      const contentType = response.headers.get('content-type');
      let errorMessage = `Erro na requisição: ${response.statusText}`;

      if (contentType && contentType.includes('application/json')) {
        const errorData = await response.json().catch(() => ({}));
        errorMessage = errorData.message || JSON.stringify(errorData);
      } else {
        const textError = await response.text().catch(() => '');
        if (textError) {
          errorMessage = textError;
        }
      }

      throw new Error(errorMessage);
    }

    // Retorna a resposta como JSON se houver conteúdo
    if (response.status !== 204) {
      return response.json()
    }

    // Retorna nulo se a resposta for 204 No Content
    return null as T
  }

  // Funções auxiliares para cada método HTTP
  const get = <T>(endpoint: string, options?: RequestOptions) =>
    request<T>(endpoint, { ...options, method: 'GET' })
  const post = <T>(endpoint: string, body: unknown, options?: RequestOptions) =>
    request<T>(endpoint, { ...options, method: 'POST', body })
  const put = <T>(endpoint: string, body: unknown, options?: RequestOptions) =>
    request<T>(endpoint, { ...options, method: 'PUT', body })
  const patch = <T>(endpoint: string, body: unknown, options?: RequestOptions) =>
    request<T>(endpoint, { ...options, method: 'PATCH', body })
  const del = <T>(endpoint: string, options?: RequestOptions) =>
    request<T>(endpoint, { ...options, method: 'DELETE' })

  return {
    get,
    post,
    put,
    patch,
    delete: del
  }
}