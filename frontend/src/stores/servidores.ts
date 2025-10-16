import { defineStore } from 'pinia';
import { useApi } from '@/composables/useApi';
import type { Servidor } from '@/types/tipos';

export const useServidoresStore = defineStore('servidores', {
    state: () => ({
        items: [] as Servidor[],
        loading: false,
        error: null as string | null,
    }),
    getters: {
        getServidorById: (state) => (id: number): Servidor | undefined => {
            return state.items.find(s => s.id === id);
        },
    },
    actions: {
        async fetchServidores() {
            this.loading = true;
            this.error = null;
            const api = useApi();
            try {
                const { data } = await api.get('/api/servidores');
                this.items = data as Servidor[];
            } catch (error: any) {
                this.error = 'Falha ao buscar servidores.';
            } finally {
                this.loading = false;
            }
        },
    }
});