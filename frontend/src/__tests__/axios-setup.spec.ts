import { vi } from 'vitest';
import { useNotificacoesStore } from '../stores/notificacoes';

const mockPush = vi.fn();
vi.mock('../router', () => ({
  default: {
    push: mockPush,
  },
}));

const mockErro = vi.fn();
vi.mock('../stores/notificacoes', () => ({
  useNotificacoesStore: vi.fn(() => ({
    erro: mockErro,
  })),
}));

const { default: apiClient } = await import('../axios-setup');

describe('axios-setup interceptors', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should handle 401 error and redirect to login', async () => {
    const error = {
      response: {
        status: 401,
        data: { message: 'Unauthorized' },
      },
    };

    try {
      await apiClient.interceptors.response.handlers[0].rejected(error);
    } catch (e) {
      // Expected rejection
    }

    expect(useNotificacoesStore().erro).toHaveBeenCalledWith(
      'Não Autorizado',
      'Sua sessão expirou ou você não está autenticado. Faça login novamente.'
    );
    expect(mockPush).toHaveBeenCalledWith('/login');
  });

  it('should handle response error with a specific message', async () => {
    const error = {
      response: {
        status: 500,
        data: { message: 'Internal Server Error' },
      },
    };

    try {
      await apiClient.interceptors.response.handlers[0].rejected(error);
    } catch (e) {
      // Expected rejection
    }

    expect(useNotificacoesStore().erro).toHaveBeenCalledWith(
      'Erro na Requisição',
      'Internal Server Error'
    );
  });

  it('should handle response error with a generic message if none is provided', async () => {
    const error = {
      response: {
        status: 404,
        data: {}, // No message
      },
    };

    try {
      await apiClient.interceptors.response.handlers[0].rejected(error);
    } catch (e) {
      // Expected rejection
    }

    expect(useNotificacoesStore().erro).toHaveBeenCalledWith(
      'Erro na Requisição',
      'Ocorreu um erro inesperado. Tente novamente mais tarde.'
    );
  });

  it('should handle network request error', async () => {
    const error = {
      request: {}, // Indicates a request was made but no response was received
    };

    try {
      await apiClient.interceptors.response.handlers[0].rejected(error);
    } catch (e) {
      // Expected rejection
    }

    expect(useNotificacoesStore().erro).toHaveBeenCalledWith(
      'Erro de Rede',
      'Não foi possível conectar ao servidor. Verifique sua conexão com a internet.'
    );
  });

  it('should handle other types of errors', async () => {
    const error = new Error('Something went wrong');

    try {
      await apiClient.interceptors.response.handlers[0].rejected(error);
    } catch (e) {
      // Expected rejection
    }

    expect(useNotificacoesStore().erro).toHaveBeenCalledWith(
      'Erro',
      'Something went wrong'
    );
  });
});
