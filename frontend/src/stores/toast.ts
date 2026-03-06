import {defineStore} from 'pinia';
import {ref} from 'vue';

export interface PendingToast {
    body: string;
}

export const useToastStore = defineStore('toast', () => {
    const pendingToast = ref<PendingToast | null>(null);

    function setPending(body: string) {
        pendingToast.value = {body};
    }

    function consumePending(): PendingToast | null {
        const toast = pendingToast.value;
        pendingToast.value = null;
        return toast;
    }

    return {pendingToast, setPending, consumePending};
});
