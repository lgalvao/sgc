import { computed } from "vue";
import { usePerfilStore } from "@/stores/perfil";
import { useUnidadesStore } from "@/stores/unidades";
import { useUsuariosStore } from "@/stores/usuarios";
import { type Unidade } from "@/types/tipos";

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
    const usuariosStore = useUsuariosStore();
    const unidadesStore = useUnidadesStore();

    const unidadesFlat = computed<Unidade[]>(() => flattenUnidades(unidadesStore.unidades),);

    const servidorLogado = computed(() => {
        const usuario = usuariosStore.obterUsuarioPorId(perfilStore.servidorId);
        if (!usuario) return null;
        return {
            ...usuario,
            perfil: perfilStore.perfilSelecionado,
            unidade: perfilStore.unidadeSelecionada,
        };
    });

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

    return {
        servidorLogado,
        perfilSelecionado,
        unidadeSelecionada,
    };
}