import {defineStore} from 'pinia'
import type {Servidor} from '@/types/tipos'
import {ServidoresService} from "@/services/servidoresService";

export const useServidoresStore = defineStore('servidores', {
    state: () => ({
        servidores: [] as Servidor[],
        isLoading: false,
        error: null as string | null
    }),
    getters: {
        getServidorById: (state) => (id: number): Servidor | undefined => {
            return state.servidores.find(s => s.codigo === id);
        }
    },
    actions: {
        async fetchServidores() {
            this.isLoading = true;
            this.error = null;
            try {
                // TODO: Substituir pela chamada real da API
                const response = await ServidoresService.buscarTodosServidores();
                this.servidores = (response as any).data.map(s => ({
                    ...s,
                    unidade: {sigla: s.unidade}
                })) as unknown as Servidor[];
            } catch (err: any) {
                this.error = 'Falha ao carregar servidores: ' + err.message;
            } finally {
                this.isLoading = false;
            }
        },
    }
})
