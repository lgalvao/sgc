import {defineStore} from 'pinia';
import {ref} from 'vue';

export interface FeedbackMessage {
    title: string;
    message: string;
    variant: 'success' | 'danger' | 'warning' | 'info';
    show: boolean; // Mantido para compatibilidade de tipos se necessário, mas não usado logicamente
    autoHideDelay?: number;
}

// Interface simplificada do Toast Controller do bootstrap-vue-next
export interface ToastController {
    show: (options: any) => void;
}

export const useFeedbackStore = defineStore('feedback', () => {
    // Referência interna ao controller do Toast
    const toast = ref<ToastController | null>(null);
    const messageQueue = ref<any[]>([]);

    function init(toastInstance: any) {
        toast.value = toastInstance;
        // Processa mensagens que chegaram antes da inicialização
        while (messageQueue.value.length > 0) {
            const args = messageQueue.value.shift();
            show(args.title, args.message, args.variant, args.autoHideDelay);
        }
    }

    function show(title: string, message: string, variant: 'success' | 'danger' | 'warning' | 'info' = 'info', autoHideDelay = 3000) {
        if (toast.value) {
            toast.value.show({
                props: {
                    title,
                    body: message,
                    variant,
                    value: autoHideDelay,
                    pos: 'top-right'
                }
            });
        } else {
            // Se o toast ainda não foi injetado (ex: erro na inicialização do app), enfileira
            messageQueue.value.push({ title, message, variant, autoHideDelay });
        }
    }

    // Mantido para compatibilidade
    function close() {
        // No-op
    }

    return {
        show,
        close,
        init
    };
});

// Alias for compatibility if needed, or consumers should use useFeedbackStore
export const useNotificacoesStore = useFeedbackStore;
