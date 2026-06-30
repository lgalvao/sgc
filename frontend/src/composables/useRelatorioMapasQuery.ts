import {computed} from "vue";
import {useQuery, useQueryCache} from "@pinia/colada";
import {STALE_TIME_CONTROLADO_POR_INVALIDACAO} from "@/composables/cachePolicy";
import {usePerfilStore} from "@/stores/perfil";
import {buscarCodigosUnidadesComMapaVigente, buscarTodasUnidades} from "@/services/unidadeService";
import type {Unidade} from "@/types/tipos";
import {Perfil} from "@/types/tipos";

// Funções utilitárias puras de árvore
function aplicarElegibilidadeMapaVigente(unidades: Unidade[], codigosElegiveis: Set<number>): Unidade[] {
    return unidades.map(unidade => ({
        ...unidade,
        isElegivel: codigosElegiveis.has(unidade.codigo),
        filhas: unidade.filhas ? aplicarElegibilidadeMapaVigente(unidade.filhas, codigosElegiveis) : []
    }));
}

function filtrarArvorePorMapaVigente(unidades: Unidade[]): Unidade[] {
    return unidades
        .map((unidade): Unidade | null => {
            const filhasFiltradas = unidade.filhas ? filtrarArvorePorMapaVigente(unidade.filhas) : [];
            const manterUnidade = unidade.isElegivel === true || filhasFiltradas.length > 0;

            if (!manterUnidade) {
                return null;
            }

            return {
                ...unidade,
                filhas: filhasFiltradas
            };
        })
        .filter((unidade): unidade is Unidade => unidade !== null);
}

function buscarSubarvore(unidades: Unidade[], codigoRaiz: number): Unidade[] {
    for (const unidade of unidades) {
        if (unidade.codigo === codigoRaiz) {
            return [unidade];
        }

        const encontrada = buscarSubarvore(unidade.filhas ?? [], codigoRaiz);
        if (encontrada.length > 0) {
            return encontrada;
        }
    }
    return [];
}

function aplicarEscopoPerfil(unidades: Unidade[], perfilSelecionado: Perfil | null, unidadeSelecionada: number | null): Unidade[] {
    if (perfilSelecionado !== Perfil.GESTOR || !unidadeSelecionada) {
        return unidades;
    }
    return buscarSubarvore(unidades, unidadeSelecionada);
}

export function useRelatorioUnidadesComMapaQuery() {
    const perfilStore = usePerfilStore();
    const chave = computed(() => [
        "relatorios",
        "unidades-com-mapa",
        perfilStore.usuarioCodigo ?? "anon",
        String(perfilStore.perfilSelecionado ?? "sem-perfil"),
        String(perfilStore.unidadeSelecionada ?? "sem-unidade")
    ] as const);

    const query = useQuery<Unidade[], Error, Unidade[]>({
        key: () => [...chave.value],
        query: async () => {
            const [arvore, codigosComMapa] = await Promise.all([
                buscarTodasUnidades(),
                buscarCodigosUnidadesComMapaVigente()
            ]);
            const comElegibilidade = aplicarElegibilidadeMapaVigente(arvore, new Set(codigosComMapa));
            const filtrada = filtrarArvorePorMapaVigente(comElegibilidade);
            return aplicarEscopoPerfil(filtrada, perfilStore.perfilSelecionado, perfilStore.unidadeSelecionada);
        },
        enabled: () => !!perfilStore.perfilSelecionado,
        staleTime: STALE_TIME_CONTROLADO_POR_INVALIDACAO,
    });

    return {
        ...query,
        invalidar: () => useQueryCache().invalidateQueries({key: ["relatorios", "unidades-com-mapa"]})
    };
}
