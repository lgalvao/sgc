import { defineStore } from "pinia";
import { computed, ref } from "vue";
import { buscarTodosUsuarios } from "@/services/usuarioService";
import type { Usuario } from "@/types/tipos";

export const useUsuariosStore = defineStore("usuarios", () => {
    const usuarios = ref<Usuario[]>([]);
    const isLoading = ref(false);
    const error = ref<string | null>(null);

    const getUsuarioById = computed(() => (id: number): Usuario | undefined => {
        return usuarios.value.find((u) => u.codigo === id);
    });

    async function fetchUsuarios() {
        isLoading.value = true;
        error.value = null;
        try {
            const response = await buscarTodosUsuarios();
            usuarios.value = (response as any).map((u: any) => ({
                ...u,
                unidade: { sigla: u.unidade },
            })) as Usuario[];
        } catch (err: any) {
            error.value = "Falha ao carregar usuários: " + err.message;
        } finally {
            isLoading.value = false;
        }
    }

    return {
        usuarios,
        isLoading,
        error,
        getUsuarioById,
        fetchUsuarios,
    };
});
