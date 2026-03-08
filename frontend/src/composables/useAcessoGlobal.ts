import { computed, reactive } from 'vue';
import { usePerfilStore } from '@/stores/perfil';

/**
 * Hook to access global UI permissions based on the user's profile.
 * Replaces direct checks of 'ADMIN' or 'GESTOR' in components.
 */
export function useAcessoGlobal() {
    const perfilStore = usePerfilStore();

    // Check if user has global roles
    const isGlobalAdmin = computed(() => perfilStore.perfis.includes("ADMIN" as any));
    const isGlobalGestor = computed(() => perfilStore.perfis.includes("GESTOR" as any));

    // Specific generic UI permissions
    const podeCriarProcesso = computed(() => isGlobalAdmin.value);
    const podeAcessarTodasUnidades = computed(() => isGlobalAdmin.value);
    const podeVisualizarTabelaCtaVazio = computed(() => isGlobalAdmin.value);
    const podeHomologarBlocoGlobal = computed(() => isGlobalAdmin.value);

    return reactive({
        isGlobalAdmin,
        isGlobalGestor,
        podeCriarProcesso,
        podeAcessarTodasUnidades,
        podeVisualizarTabelaCtaVazio,
        podeHomologarBlocoGlobal
    });
}
