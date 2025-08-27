import { defineStore } from 'pinia';
import { ref } from 'vue';

export type TipoNotificacao = 'success' | 'error' | 'warning' | 'info';

export interface Notificacao {
  id: string;
  tipo: TipoNotificacao;
  titulo: string;
  mensagem: string;
  duracao?: number; // em milissegundos
  timestamp: Date;
}

export const useNotificacoesStore = defineStore('notificacoes', () => {
  const notificacoes = ref<Notificacao[]>([]);

  const adicionarNotificacao = (notificacao: Omit<Notificacao, 'id' | 'timestamp'>) => {
    const novaNotificacao: Notificacao = {
      ...notificacao,
      id: Date.now().toString() + Math.random().toString(36).substr(2, 9),
      timestamp: new Date(),
      duracao: notificacao.duracao ?? 5000
    };

    notificacoes.value.push(novaNotificacao);

    // Auto-remover após a duração especificada
    if (novaNotificacao.duracao && novaNotificacao.duracao > 0) {
      setTimeout(() => {
        removerNotificacao(novaNotificacao.id);
      }, novaNotificacao.duracao);
    }

    return novaNotificacao.id;
  };

  const removerNotificacao = (id: string) => {
    const index = notificacoes.value.findIndex(n => n.id === id);
    if (index > -1) {
      notificacoes.value.splice(index, 1);
    }
  };

  const limparTodas = () => {
    notificacoes.value = [];
  };

  // Métodos convenientes para tipos específicos
  const sucesso = (titulo: string, mensagem: string, duracao?: number) => {
    return adicionarNotificacao({ tipo: 'success', titulo, mensagem, duracao });
  };

  const erro = (titulo: string, mensagem: string, duracao?: number) => {
    return adicionarNotificacao({ tipo: 'error', titulo, mensagem, duracao });
  };

  const aviso = (titulo: string, mensagem: string, duracao?: number) => {
    return adicionarNotificacao({ tipo: 'warning', titulo, mensagem, duracao });
  };

  const info = (titulo: string, mensagem: string, duracao?: number) => {
    return adicionarNotificacao({ tipo: 'info', titulo, mensagem, duracao });
  };

  return {
    notificacoes,
    adicionarNotificacao,
    removerNotificacao,
    limparTodas,
    sucesso,
    erro,
    aviso,
    info
  };
});