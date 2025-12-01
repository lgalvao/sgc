import { useFeedbackStore } from "@/stores/feedback";

// Mantemos a exportação vazia de registerToast para não quebrar compatibilidade imediata,
// mas ela não faz mais nada.
export const registerToast = (_instance: any) => {
  // Deprecated: No longer needed with Pinia store
};

export const ToastService = {
  sucesso(titulo: string, mensagem: string) {
    console.log('[ToastService] sucesso:', titulo);
    try {
      const store = useFeedbackStore();
      store.show(titulo, mensagem, 'success');
    } catch (e) {
      console.error('[ToastService] Erro ao chamar store:', e);
    }
  },
  erro(titulo: string, mensagem: string) {
    console.log('[ToastService] erro:', titulo);
    try {
      const store = useFeedbackStore();
      store.show(titulo, mensagem, 'danger');
    } catch (e) {
      console.error('[ToastService] Erro ao chamar store:', e);
    }
  },
  aviso(titulo: string, mensagem: string) {
    console.log('[ToastService] aviso:', titulo);
    try {
      const store = useFeedbackStore();
      store.show(titulo, mensagem, 'warning');
    } catch (e) {
      console.error('[ToastService] Erro ao chamar store:', e);
    }
  },
  info(titulo: string, mensagem: string) {
    console.log('[ToastService] info:', titulo);
    try {
      const store = useFeedbackStore();
      store.show(titulo, mensagem, 'info');
    } catch (e) {
      console.error('[ToastService] Erro ao chamar store:', e);
    }
  }
};
