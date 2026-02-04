import {defineStore} from 'pinia';
import {ref} from 'vue';

export interface FeedbackMessage {
    title: string;
    message: string;
    variant: 'success' | 'danger' | 'warning' | 'info';
    show: boolean; // Mantido para compatibilidade de tipos se necessário, mas não usado logicamente
    autoHideDelay?: number;
}

export interface ToastController {
    create: (options: any) => void;
}

export const useFeedbackStore = defineStore('feedback', () => {
    // Referência interna ao controller do Toast
    const toast = ref<ToastController | null>(null);
    const messageQueue = ref<any[]>([]);
    const currentToast = ref<any>(null);

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
            // If there's already an active toast, skip (debounce)
            if (currentToast.value) {
                return;
            }
            currentToast.value = toast.value.create({
                props: {
                    title,
                    body: message,
                    variant,
                    value: autoHideDelay,
                    pos: 'top-right',
                    noProgress: true,
                    onHidden: () => {
                        // Reset currentToast when the toast is hidden
                        currentToast.value = null;
                    }
                }
            });
        } else {
            // Se o toast ainda não foi injetado (ex: erro na inicialização do app), enfileira
            messageQueue.value.push({ title, message, variant, autoHideDelay });
        }
    }

    return {
        show,
        close,
        init
    };
});

// Mantido para compatibilidade
function close() {
    // No-op
}

// Alias for compatibility if needed, or consumers should use useFeedbackStore
export const useNotificacoesStore = useFeedbackStore;
