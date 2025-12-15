import {defineStore} from "pinia";
import {computed, ref} from "vue";
import {buscarTodosUsuarios} from "@/services/usuarioService";
import type {Usuario} from "@/types/tipos";
import {type NormalizedError, normalizeError} from "@/utils/apiError";

export const useUsuariosStore = defineStore("usuarios", () => {
    const usuarios = ref<Usuario[]>([]);
    const isLoading = ref(false);
    const error = ref<string | null>(null);
    const lastError = ref<NormalizedError | null>(null);

    function clearError() {
        lastError.value = null;
        error.value = null;
    }

    const obterUsuarioPorId = computed(() => (id: number): Usuario | undefined => {
        return usuarios.value.find((u) => u.codigo === id);
    });

    async function buscarUsuarios() {
        isLoading.value = true;
        error.value = null;
        lastError.value = null;
        try {
            const response = await buscarTodosUsuarios();
            usuarios.value = (response as any).map((u: any) => ({
                ...u,
                unidade: {sigla: u.unidade},
            })) as Usuario[];
        } catch (err: any) {
            lastError.value = normalizeError(err);
            error.value = lastError.value.message;
        } finally {
            isLoading.value = false;
        }
    }

    return {
        usuarios,
        isLoading,
        error,
        lastError,
        clearError,
        obterUsuarioPorId,
        buscarUsuarios,
    };
});
