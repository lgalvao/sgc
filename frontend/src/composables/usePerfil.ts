import {computed} from "vue";
import {usePerfilStore} from "@/stores/perfil";
import {useUnidadesStore} from "@/stores/unidades";
import {Perfil, type Unidade} from "@/types/tipos";

// Função auxiliar para achatar a hierarquia de unidades
function flattenUnidades(unidades: Unidade[]): Unidade[] {
    let flat: Unidade[] = [];
    unidades.forEach((u: Unidade) => {
        flat.push(u);
        if (u.filhas && u.filhas.length > 0) flat = flat.concat(flattenUnidades(u.filhas));
    });
    return flat;
}

export function usePerfil() {
    const perfilStore = usePerfilStore();
    const unidadesStore = useUnidadesStore();

    const unidadesFlat = computed<Unidade[]>(() => flattenUnidades(unidadesStore.unidades),);

    const perfilSelecionado = computed(() => perfilStore.perfilSelecionado);

    const unidadeSelecionada = computed(() => {
        if (perfilStore.unidadeSelecionadaSigla) {
            return perfilStore.unidadeSelecionadaSigla;
        }
        const unidadeObj = unidadesFlat.value.find(
            (u) => u.codigo === perfilStore.unidadeSelecionada,
        );
        return unidadeObj?.sigla || perfilStore.unidadeSelecionada;
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