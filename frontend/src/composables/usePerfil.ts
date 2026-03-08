import {computed} from "vue";
import {usePerfilStore} from "@/stores/perfil";
import {Perfil} from "@/types/tipos";

export function usePerfil() {
    const perfilStore = usePerfilStore();

    const perfilSelecionado = computed(() => perfilStore.perfilSelecionado);

    const unidadeSelecionada = computed(() => {
        return perfilStore.unidadeSelecionadaSigla || perfilStore.unidadeSelecionada;
    });

    const isAdmin = computed(() => perfilStore.perfilSelecionado === Perfil.ADMIN);
    const isGestor = computed(() => perfilStore.perfilSelecionado === Perfil.GESTOR);
    const isChefe = computed(() => perfilStore.perfilSelecionado === Perfil.CHEFE);
    const isServidor = computed(() => perfilStore.perfilSelecionado === Perfil.SERVIDOR);

    const podeAcessoGeralAdminGestor = computed(() => isAdmin.value || isGestor.value);
    const podeCriarProcesso = computed(() => isAdmin.value || isGestor.value);
    const podeAcessarTodasUnidades = computed(() => isAdmin.value);
    const podeVisualizarTabelaCtaVazio = computed(() => isAdmin.value || isGestor.value);

    // Aliases for specific blocks or legacy naming if needed
    const isGlobalAdmin = isAdmin;
    const isGlobalGestor = isGestor;
    const podeHomologarBlocoGlobal = isAdmin;

    return {
        perfilSelecionado,
        unidadeSelecionada,
        isAdmin,
        isGestor,
        isChefe,
        isServidor,
        podeAcessoGeralAdminGestor,
        podeCriarProcesso,
        podeAcessarTodasUnidades,
        podeVisualizarTabelaCtaVazio,
        isGlobalAdmin,
        isGlobalGestor,
        podeHomologarBlocoGlobal,
    };
}