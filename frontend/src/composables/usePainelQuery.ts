// @sgc-auditoria ignorar: arquivoMinusculo | padrão Pinia Colada: arquivo de domínio com chave de query + hook de invalidação — pequeno por design
import {computed} from "vue";
import {useQuery, useQueryCache} from "@pinia/colada";
import {STALE_TIME_CONTROLADO_POR_INVALIDACAO} from "@/composables/cachePolicy";
import {usePerfilStore} from "@/stores/perfil";
import {usePainelStore} from "@/stores/painel";
import {obterBootstrap} from "@/services/painelService";
import type {PainelBootstrap} from "@/types/tipos";

export const CHAVE_QUERY_PAINEL = ["painel"] as const;

export function usePainelQuery() {
    const perfilStore = usePerfilStore();
    const painelStore = usePainelStore();
    const chave = computed(() => [
        ...CHAVE_QUERY_PAINEL,
        perfilStore.usuarioCodigo ?? "anon",
        perfilStore.perfilSelecionado ?? "sem-perfil",
        perfilStore.unidadeSelecionada ?? "sem-unidade",
        painelStore.versaoInvalidacao,
    ] as const);

    return useQuery<PainelBootstrap, Error, PainelBootstrap>({
        key: () => [...chave.value],
        query: () => obterBootstrap(),
        enabled: false,
        staleTime: STALE_TIME_CONTROLADO_POR_INVALIDACAO,
    });
}

export function useInvalidacaoPainel() {
    return {
        invalidarPainel: () => useQueryCache().invalidateQueries({key: CHAVE_QUERY_PAINEL}),
    };
}
