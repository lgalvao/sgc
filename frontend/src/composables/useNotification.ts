import {ref} from 'vue';

export type VarianteAlerta = 'danger' | 'warning' | 'success' | 'info';

export interface NotificacaoEstruturada {
    summary: string;
    details: string[];
}

export interface EstadoNotificacao {
    message?: string;
    notification?: NotificacaoEstruturada;
    variant: VarianteAlerta;
    dismissible?: boolean;
    stackTrace?: string;
}

export function useNotification() {
    const notificacao = ref<EstadoNotificacao | null>(null);

    function notify(message: string, variant: VarianteAlerta = 'danger', dismissible = true) {
        notificacao.value = {message, variant, dismissible};
    }

    function notifyStructured(
        summary: string,
        details: string[],
        variant: VarianteAlerta = 'danger',
        stackTrace?: string,
        dismissible = true,
    ) {
        notificacao.value = {notification: {summary, details}, variant, stackTrace, dismissible};
    }

    function clear() {
        notificacao.value = null;
    }

    return {notificacao, notify, notifyStructured, clear};
}
