// Service singleton to bridge non-component code (Stores, Axios) to BootstrapVueNext Toasts

// Define a minimal interface for the Toast Controller methods we use
interface ToastController {
  show(options: { title: string; body: string; props?: { variant: string; value: boolean } }): void;
}

let toastInstance: ToastController | null = null;

export const registerToast = (instance: any) => {
  toastInstance = instance;
};

export const ToastService = {
  sucesso(titulo: string, mensagem: string) {
    toastInstance?.show({
      title: titulo,
      body: mensagem,
      props: { variant: 'success', value: true }
    });
  },
  erro(titulo: string, mensagem: string) {
    toastInstance?.show({
      title: titulo,
      body: mensagem,
      props: { variant: 'danger', value: true }
    });
  },
  aviso(titulo: string, mensagem: string) {
    toastInstance?.show({
      title: titulo,
      body: mensagem,
      props: { variant: 'warning', value: true }
    });
  },
  info(titulo: string, mensagem: string) {
    toastInstance?.show({
      title: titulo,
      body: mensagem,
      props: { variant: 'info', value: true }
    });
  }
};
