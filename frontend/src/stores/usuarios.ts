import {defineStore} from "pinia";
import {ref, computed} from "vue";
import {
    buscarTodosUsuarios,
    buscarUsuariosPorUnidade as serviceBuscarUsuariosPorUnidade,
    buscarUsuarioPorTitulo as serviceBuscarUsuarioPorTitulo,
} from "@/services/usuarioService";
import type {Usuario} from "@/types/tipos";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {useSingleLoading} from "@/composables/useLoadingManager";

export const useUsuariosStore = defineStore("usuarios", () => {
    const usuarios = ref<Usuario[]>([]);
    const loading = useSingleLoading();
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
        error.value = null;
        await loading.withLoading(async () => {
            await withErrorHandling(async () => {
                const response = await buscarTodosUsuarios();
                usuarios.value = (response as any).map((u: any) => ({
                    ...u,
                    unidade: {sigla: u.unidade},
                })) as Usuario[];
            }, () => {
                error.value = lastError.value?.message || "Erro ao buscar usuários";
            });
        });
    }

    async function buscarUsuariosPorUnidade(codigoUnidade: number): Promise<Usuario[]> {
        error.value = null;
        return loading.withLoading(async () => {
            return withErrorHandling(async () => {
                const response = await serviceBuscarUsuariosPorUnidade(codigoUnidade);
                return response;
            }, () => {
                error.value = lastError.value?.message || "Erro ao buscar usuários da unidade";
            }) as Promise<Usuario[]>;
        });
    }

    async function buscarUsuarioPorTitulo(titulo: string): Promise<Usuario | undefined> {
        error.value = null;
        return loading.withLoading(async () => {
            return withErrorHandling(async () => {
                return await serviceBuscarUsuarioPorTitulo(titulo);
            }, () => {
                error.value = lastError.value?.message || "Erro ao buscar usuário";
            }) as Promise<Usuario | undefined>;
        });
    }

    return {
        usuarios,
        isLoading: loading.isLoading,
        error,
        lastError,
        clearError,
        obterUsuarioPorTitulo,
        obterUsuarioPorId,
        buscarUsuarios,
        buscarUsuariosPorUnidade,
        buscarUsuarioPorTitulo,
    };
});
