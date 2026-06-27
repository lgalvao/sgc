import {defineStore} from 'pinia';
import {ref} from 'vue';

export interface ToastPendente {
    mensagem: string;
    variante?: 'success' | 'danger' | 'warning' | 'info';
}

export const useToastStore = defineStore('toast', () => {
    const toastPendente = ref<ToastPendente | null>(null);

    function setPending(
        mensagem: string,
        variante: ToastPendente['variante'] = 'success',
    ) {
        toastPendente.value = {mensagem, variante};
    }

    function consumePending(): ToastPendente | null {
        const toast = toastPendente.value;
        toastPendente.value = null;
        return toast;
    }

    return {toastPendente, setPending, consumePending};
});
