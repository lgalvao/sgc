import {defineStore} from 'pinia'
import type {Usuario} from '@/types/tipos'
import {buscarTodosUsuarios} from "@/services/usuarioService";

export const useUsuariosStore = defineStore('usuarios', {
    state: () => ({
        usuarios: [] as Usuario[],
        isLoading: false,
        error: null as string | null
    }),
    getters: {
        getUsuarioById: (state) => (id: number): Usuario | undefined => {
            return state.usuarios.find(u => u.codigo === id);
        }
    },
    actions: {
        async fetchUsuarios() {
            this.isLoading = true;
            this.error = null;
            try {
                const response = await buscarTodosUsuarios();
                this.usuarios = (response as any).map(u => ({
                    ...u,
                    unidade: {sigla: u.unidade}
                })) as any as Usuario[];
            } catch (err: any) {
                this.error = 'Falha ao carregar usuários: ' + err.message;
            } finally {
                this.isLoading = false;
            }
        },
    }
})
