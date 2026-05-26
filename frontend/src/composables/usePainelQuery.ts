import {computed} from "vue";
import {useQuery, useQueryCache} from "@pinia/colada";
import {usePerfilStore} from "@/stores/perfil";
import {obterBootstrap} from "@/services/painelService";
import type {PainelBootstrap} from "@/types/tipos";

export const CHAVE_QUERY_PAINEL = ["painel"] as const;

export function usePainelQuery() {
    const perfilStore = usePerfilStore();
    const chave = computed(() => [
        ...CHAVE_QUERY_PAINEL,
        perfilStore.usuarioCodigo ?? "anon",
        perfilStore.perfilSelecionado ?? "sem-perfil",
        perfilStore.unidadeSelecionada ?? "sem-unidade",
    ] as const);

    return useQuery<PainelBootstrap, Error, PainelBootstrap>({
        key: () => [...chave.value],
        query: () => obterBootstrap(),
        enabled: () => Boolean(perfilStore.perfilSelecionado && perfilStore.unidadeSelecionada),
        initialData: () => ({processos: [], alertas: []}),
        staleTime: Infinity,
    });
}

export function useInvalidacaoPainel() {
    const queryCache = useQueryCache();

    return {
        invalidarPainel: () => queryCache.invalidateQueries({key: CHAVE_QUERY_PAINEL}),
    };
}
