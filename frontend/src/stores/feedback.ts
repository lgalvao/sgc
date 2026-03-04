import {defineStore} from 'pinia';
import {ref} from 'vue';

export type FeedbackVariant = 'success' | 'danger' | 'warning' | 'info';

export interface FeedbackMessage {
    title: string;
    message: string;
    variant: FeedbackVariant;
    show: boolean; // Mantido para compatibilidade de tipos se necessário, mas não usado logicamente
    autoHideDelay?: number;
}

export interface ToastOptions {
    props: {
        title: string;
        body: string;
        variant: FeedbackVariant;
        value: number;
        pos: 'top-right' | 'top-left' | 'bottom-right' | 'bottom-left' | 'top-center' | 'bottom-center';
        noProgress: boolean;
    };

    [key: string]: unknown;
}

export interface ToastController {
    create: (options: ToastOptions) => void;

    [key: string]: unknown;
}

export const useFeedbackStore = defineStore('feedback', () => {
    // Referência interna ao controller do Toast
    const toast = ref<ToastController | null>(null);
    const messageQueue = ref<{title: string; message: string; variant: FeedbackVariant; autoHideDelay: number}[]>([]);
    const isToastActive = ref(false);

    function init(toastInstance: ToastController) {
        toast.value = toastInstance;
        // Processa mensagens que chegaram antes da inicialização
        if (messageQueue.value.length > 0) {
            const args = messageQueue.value.shift();
            if (args) {
                show(args.title, args.message, args.variant, args.autoHideDelay);
            }
            // Limpa o resto da fila se o debounce for estrito como nos testes
            messageQueue.value = [];
        }
    }

    function show(title: string, message: string, variant: FeedbackVariant = 'info', autoHideDelay = 3000) {
        if (isToastActive.value) {
            return;
        }

        if (toast.value) {
            isToastActive.value = true;
            // Fechar toasts anteriores antes de exibir um novo (política de toast único)
            document.querySelectorAll('.toast .btn-close').forEach(btn => {
                (btn as HTMLElement).click();
            });

            toast.value.create({
                props: {
                    title,
                    body: message,
                    variant,
                    value: autoHideDelay,
                    pos: 'bottom-right',
                    noProgress: true
                }
            });

            // Simula o tempo que o toast fica ativo para o debounce
            setTimeout(() => {
                isToastActive.value = false;
            }, 100);
        } else {
            // Se o toast ainda não foi injetado (ex: erro na inicialização do app), enfileira
            messageQueue.value.push({title, message, variant, autoHideDelay});
        }
    }

    return {
        show,
        close,
        init
    };
});

// TODO Nao deixar nada nunca para compatibilidade!
// Mantido para compatibilidade
function close() {
}

// Alias for compatibility if needed, or consumers should use useFeedbackStore
export const useNotificacoesStore = useFeedbackStore;
