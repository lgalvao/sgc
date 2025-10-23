import axios from 'axios';
import router from './router';
import {useNotificacoesStore} from './stores/notificacoes';

const apiClient = axios.create({
  baseURL: 'http://localhost:10000/api',
  headers: {
    'Content-type': 'application/json',
  },
});

export const handleResponseError = (error: any) => {
  const notificacoesStore = useNotificacoesStore();
  if (error.response) {
    const { status, data } = error.response;
    // Do not show global popups for these statuses, they will be handled locally
    const isHandledInline = [400, 404, 409, 422].includes(status);

    if (isHandledInline) {
      // Just forward the error to the local handler
      return Promise.reject(error);
    }

    if (status === 401) {
      notificacoesStore.erro('Não Autorizado', 'Sua sessão expirou ou você não está autenticado. Faça login novamente.');
      router.push('/login');
    } else if (data && data.message) {
      // For other errors (like 500), show a generic popup
      notificacoesStore.erro('Erro Inesperado', data.message);
    } else {
      notificacoesStore.erro('Erro Inesperado', 'Ocorreu um erro. Tente novamente mais tarde.');
    }
  } else if (error.request) {
    notificacoesStore.erro('Erro de Rede', 'Não foi possível conectar ao servidor. Verifique sua conexão com a internet.');
  } else {
    notificacoesStore.erro('Erro', error.message);
  }
  return Promise.reject(error);
};

apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jwtToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

apiClient.interceptors.response.use(
  (response) => response,
  handleResponseError
);

export default apiClient;