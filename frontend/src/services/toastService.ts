import {useFeedbackStore} from "@/stores/feedback";

export const ToastService = {
    sucesso(titulo: string, mensagem: string) {
        const store = useFeedbackStore();
        store.show(titulo, mensagem, 'success');
    },
    erro(titulo: string, mensagem: string) {
        const store = useFeedbackStore();
        store.show(titulo, mensagem, 'danger');
    },
    aviso(titulo: string, mensagem: string) {
        const store = useFeedbackStore();
        store.show(titulo, mensagem, 'warning');
    },
    info(titulo: string, mensagem: string) {
        const store = useFeedbackStore();
        store.show(titulo, mensagem, 'info');
    }
};
