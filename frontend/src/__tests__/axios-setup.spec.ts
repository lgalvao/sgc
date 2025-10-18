import { vi } from 'vitest';
import { useNotificacoesStore } from '@/stores/notificacoes';

// Mocking the router and the store
vi.mock('@/router', () => ({
  default: {
    push: vi.fn(),
  },
}));

vi.mock('@/stores/notificacoes', () => ({
  useNotificacoesStore: vi.fn(() => ({
    erro: vi.fn(),
  })),
}));

describe('axios-setup', () => {
  let mockErro: ReturnType<typeof useNotificacoesStore>['erro'];

  beforeEach(async () => {
    // Reset modules to ensure mocks are clean for each test
    vi.resetModules();
    // Dynamically import the module *after* mocks are set up
    const { useNotificacoesStore } = await import('@/stores/notificacoes');
    mockErro = vi.fn();
    (useNotificacoesStore as vi.Mock).mockReturnValue({ erro: mockErro });
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should redirect to /login and show error on 401 response', async () => {
    const router = (await import('@/router')).default;
    const apiClient = (await import('@/axios-setup')).default;
    const error = {
      response: {
        status: 401,
        data: {},
      },
    };

    // Simulate the interceptor being called
    try {
      await apiClient.interceptors.response.handlers[0].rejected(error);
    } catch (e) {
      // expected
    }

    expect(mockErro).toHaveBeenCalledWith(
      'Não Autorizado',
      'Sua sessão expirou ou você não está autenticado. Por favor, faça login novamente.'
    );
    expect(router.push).toHaveBeenCalledWith('/login');
  });

  it('should show error with data message on response error', async () => {
    const apiClient = (await import('@/axios-setup')).default;
    const error = {
      response: {
        status: 400,
        data: { message: 'Bad Request' },
      },
    };

    try {
      await apiClient.interceptors.response.handlers[0].rejected(error);
    } catch (e) {
      // expected
    }

    expect(mockErro).toHaveBeenCalledWith('Erro na Requisição', 'Bad Request');
  });

  it('should show generic error on response error without data message', async () => {
    const apiClient = (await import('@/axios-setup')).default;
    const error = {
      response: {
        status: 500,
        data: {},
      },
    };

    try {
      await apiClient.interceptors.response.handlers[0].rejected(error);
    } catch (e) {
      // expected
    }

    expect(mockErro).toHaveBeenCalledWith('Erro na Requisição', 'Ocorreu um erro inesperado. Tente novamente mais tarde.');
  });

  it('should show network error on request error', async () => {
    const apiClient = (await import('@/axios-setup')).default;
    const error = {
      request: {},
    };

    try {
      await apiClient.interceptors.response.handlers[0].rejected(error);
    } catch (e) {
      // expected
    }

    expect(mockErro).toHaveBeenCalledWith('Erro de Rede', 'Não foi possível conectar ao servidor. Verifique sua conexão com a internet.');
  });

  it('should show generic error for other errors', async () => {
    const apiClient = (await import('@/axios-setup')).default;
    const error = new Error('Something went wrong');

    try {
      await apiClient.interceptors.response.handlers[0].rejected(error);
    } catch (e) {
      // expected
    }

    expect(mockErro).toHaveBeenCalledWith('Erro', 'Something went wrong');
  });
});
