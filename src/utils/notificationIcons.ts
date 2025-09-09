import {TipoNotificacao} from '@/stores/notificacoes';

export const iconeTipo = (tipo: TipoNotificacao): string => {
    switch (tipo) {
        case 'success':
            return 'bi bi-check-circle-fill text-success';
        case 'error':
            return 'bi bi-exclamation-triangle-fill text-danger';
        case 'warning':
            return 'bi bi-exclamation-triangle-fill text-warning';
        case 'info':
            return 'bi bi-info-circle-fill text-info';
        case 'email':
            return 'bi bi-envelope-fill text-primary';
        default:
            return 'bi bi-bell-fill';
    }
};
