import {defineStore} from "pinia";
import {ref, computed} from "vue";
import {buscarTodosUsuarios} from "@/services/usuarioService";
import type {Usuario} from "@/types/tipos";
import {useErrorHandler} from "@/composables/useErrorHandler";

export const useUsuariosStore = defineStore("usuarios", () => {
    const usuarios = ref<Usuario[]>([]);
    const isLoading = ref(false);
    const error = ref<string | null>(null);
    const { lastError, clearError: clearNormalizedError, withErrorHandling } = useErrorHandler();

    // Maps para lookup O(1)
    const usuariosPorTituloMap = computed(() => 
        new Map(usuarios.value.map(u => [u.tituloEleitoral, u]))
    );

    const usuariosPorCodigoMap = computed(() => 
        new Map(usuarios.value.map(u => [u.codigo, u]))
    );

    function clearError() {
        clearNormalizedError();
        error.value = null;
    }

    function obterUsuarioPorTitulo(titulo: string): Usuario | undefined {
        return usuariosPorTituloMap.value.get(titulo);
    }

    function obterUsuarioPorId(id: number): Usuario | undefined {
        return usuariosPorCodigoMap.value.get(id);
    }

    async function buscarUsuarios() {
        isLoading.value = true;
        error.value = null;
        await withErrorHandling(async () => {
            const response = await buscarTodosUsuarios();
            usuarios.value = (response as any).map((u: any) => ({
                ...u,
                unidade: {sigla: u.unidade},
            })) as Usuario[];
        }, () => {
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
