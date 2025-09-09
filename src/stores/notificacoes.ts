import {defineStore} from 'pinia';
import {ref} from 'vue';

export type TipoNotificacao = 'success' | 'error' | 'warning' | 'info' | 'email';

export interface EmailContent {
  assunto: string;
  destinatario: string;
  corpo: string;
}

export interface Notificacao {
  id: string;
  tipo: TipoNotificacao;
  titulo: string;
  mensagem: string;
  emailContent?: EmailContent; // Para notificações de email
  duracao?: number; // em milissegundos
  timestamp: Date;
}

export const useNotificacoesStore = defineStore('notificacoes', () => {
  const notificacoes = ref<Notificacao[]>([]);

  const adicionarNotificacao = (notificacao: Omit<Notificacao, 'id' | 'timestamp'>) => {
    const novaNotificacao: Notificacao = {
      ...notificacao,
        id: Date.now().toString() + Math.random().toString(36).slice(2, 11),
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

  const email = (assunto: string, destinatario: string, corpo: string) => {
    const titulo = `E-mail enviado: ${assunto}`;
    const mensagem = `Para: ${destinatario}`;
    return adicionarNotificacao({
      tipo: 'email',
      titulo,
      mensagem,
      emailContent: { assunto, destinatario, corpo },
      duracao: 10000 // 10 segundos para emails
    });
  };

  return {
    notificacoes,
    adicionarNotificacao,
    removerNotificacao,
    limparTodas,
    sucesso,
    erro,
    aviso,
    info,
    email
  };
});