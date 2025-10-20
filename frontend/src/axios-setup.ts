import axios from 'axios';
import router from './router';
import {useNotificacoesStore} from './stores/notificacoes';

const apiClient = axios.create({
  baseURL: 'http://localhost:10000/api',
  headers: {
    'Content-type': 'application/json',
  },
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const notificacoesStore = useNotificacoesStore();
    if (error.response) {
      const { status, data } = error.response;
      if (status === 401) {
        notificacoesStore.erro('Não Autorizado', 'Sua sessão expirou ou você não está autenticado. Faça login novamente.');
        router.push('/login');
      } else if (data && data.message) {
        notificacoesStore.erro('Erro na Requisição', data.message);
      } else {
        notificacoesStore.erro('Erro na Requisição', 'Ocorreu um erro inesperado. Tente novamente mais tarde.');
      }
    } else if (error.request) {
      notificacoesStore.erro('Erro de Rede', 'Não foi possível conectar ao servidor. Verifique sua conexão com a internet.');
    } else {
      notificacoesStore.erro('Erro', error.message);
    }
    return Promise.reject(error);
  }
);

export default apiClient;