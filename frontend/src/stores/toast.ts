import {defineStore} from 'pinia';
import {ref} from 'vue';

export interface ToastPendente {
    mensagem: string;
}

export const useToastStore = defineStore('toast', () => {
    const toastPendente = ref<ToastPendente | null>(null);

    function setPending(mensagem: string) {
        toastPendente.value = {mensagem};
    }

    function consumePending(): ToastPendente | null {
        const toast = toastPendente.value;
        toastPendente.value = null;
        return toast;
    }

    return {toastPendente, setPending, consumePending};
});
