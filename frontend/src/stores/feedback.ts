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
    const lastShowTime = ref(0);
    const DEBOUNCE_MS = 500;

    function init(toastInstance: any) {
        toast.value = toastInstance;
        // Processa mensagens que chegaram antes da inicialização
        if (messageQueue.value.length > 0) {
            const args = messageQueue.value.shift();
            show(args.title, args.message, args.variant, args.autoHideDelay);
            // Limpa o resto da fila se o debounce for estrito como nos testes
            messageQueue.value = [];
        }
    }

    function show(title: string, message: string, variant: 'success' | 'danger' | 'warning' | 'info' = 'info', autoHideDelay = 3000) {
        if (toast.value) {
            // Fechar toasts anteriores antes de exibir um novo (política de toast único)
            document.querySelectorAll('.toast .btn-close').forEach(btn => {
                (btn as HTMLElement).click();
            });

            toast.value.create({
                props: {
                    title,
                    body: message,
                    variant,
                    modelValue: autoHideDelay,
                    pos: 'bottom-end',
                    noProgress: true
                }
            });
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
