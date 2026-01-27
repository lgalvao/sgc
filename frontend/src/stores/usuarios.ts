import {defineStore} from "pinia";
import {computed, ref} from "vue";
import {buscarTodosUsuarios} from "@/services/usuarioService";
import type {Usuario} from "@/types/tipos";
import {useErrorHandler} from "@/composables/useErrorHandler";

export const useUsuariosStore = defineStore("usuarios", () => {
    const usuarios = ref<Usuario[]>([]);
    const isLoading = ref(false);
    const error = ref<string | null>(null);
    const { lastError, clearError: clearNormalizedError, withErrorHandling } = useErrorHandler();

    function clearError() {
        clearNormalizedError();
        error.value = null;
    }

    const obterUsuarioPorTitulo = computed(() => (titulo: string): Usuario | undefined => {
        return usuarios.value.find((u) => u.tituloEleitoral === titulo);
    });

    const obterUsuarioPorId = computed(() => (id: number): Usuario | undefined => {
        return usuarios.value.find((u) => u.codigo === id);
    });

    async function buscarUsuarios() {
        isLoading.value = true;
        error.value = null;
        await withErrorHandling(async () => {
            const response = await buscarTodosUsuarios();
            usuarios.value = (response as any).map((u: any) => ({
                ...u,
                unidade: {sigla: u.unidade},
            })) as Usuario[];
        }).catch(() => {
            error.value = lastError.value?.message || "Erro ao buscar usuários";
        }).finally(() => {
            isLoading.value = false;
        });
    }

    return {
        usuarios,
        isLoading,
        error,
        lastError,
        clearError,
        obterUsuarioPorTitulo,
        obterUsuarioPorId,
        buscarUsuarios,
    };
});
