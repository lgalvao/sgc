import {computed, type MaybeRefOrGetter, toValue} from "vue";
import {useQuery, useQueryCache} from "@pinia/colada";
import {usePerfilStore} from "@/stores/perfil";
import type {ImpactoMapa, MapaCompleto} from "@/types/tipos";
import {
    obterMapaCompleto as serviceObterMapaCompleto,
    verificarImpactosMapa as serviceVerificarImpactosMapa,
} from "@/services/subprocessoService";

export const CHAVE_QUERY_MAPA = ["mapa"] as const;
export const CHAVE_QUERY_IMPACTO_MAPA = ["impacto-mapa"] as const;

type ContextoSessaoMapa = readonly [string, string, string];

function criarContextoSessaoMapa(perfilStore: ReturnType<typeof usePerfilStore>): ContextoSessaoMapa {
    return [
        perfilStore.usuarioCodigo ?? "anon",
        String(perfilStore.perfilSelecionado ?? "sem-perfil"),
        String(perfilStore.unidadeSelecionada ?? "sem-unidade"),
    ] as const;
}

export function criarChaveMapa(codigoSubprocesso: number, contextoSessao: ContextoSessaoMapa) {
    return [...CHAVE_QUERY_MAPA, ...contextoSessao, codigoSubprocesso] as const;
}

export function criarChaveImpactoMapa(codigoSubprocesso: number, contextoSessao: ContextoSessaoMapa) {
    return [...CHAVE_QUERY_IMPACTO_MAPA, ...contextoSessao, codigoSubprocesso] as const;
}

export function useMapaQuery(codigoSubprocesso: MaybeRefOrGetter<number | null | undefined>) {
    const perfilStore = usePerfilStore();
    const chave = computed(() => {
        const codigoAtual = toValue(codigoSubprocesso);
        return typeof codigoAtual === "number"
            ? criarChaveMapa(codigoAtual, criarContextoSessaoMapa(perfilStore))
            : [...CHAVE_QUERY_MAPA, ...criarContextoSessaoMapa(perfilStore), "sem-codigo"] as const;
    });

    return useQuery<MapaCompleto | null, Error, MapaCompleto | null>({
        key: () => [...chave.value],
        query: async () => {
            const codigoAtual = toValue(codigoSubprocesso);
            if (typeof codigoAtual !== "number") {
                return null;
            }
            return serviceObterMapaCompleto(codigoAtual);
        },
        enabled: () => {
            const codigoAtual = toValue(codigoSubprocesso);
            return typeof codigoAtual === "number" && codigoAtual > 0 && !!perfilStore.perfilSelecionado;
        },
        staleTime: Infinity,
    });
}

export function useImpactoMapaQuery(codigoSubprocesso: MaybeRefOrGetter<number | null | undefined>) {
    const perfilStore = usePerfilStore();
    const chave = computed(() => {
        const codigoAtual = toValue(codigoSubprocesso);
        return typeof codigoAtual === "number"
            ? criarChaveImpactoMapa(codigoAtual, criarContextoSessaoMapa(perfilStore))
            : [...CHAVE_QUERY_IMPACTO_MAPA, ...criarContextoSessaoMapa(perfilStore), "sem-codigo"] as const;
    });

    return useQuery<ImpactoMapa | null, Error, ImpactoMapa | null>({
        key: () => [...chave.value],
        query: async () => {
            const codigoAtual = toValue(codigoSubprocesso);
            if (typeof codigoAtual !== "number") {
                return null;
            }
            return serviceVerificarImpactosMapa(codigoAtual);
        },
        enabled: false,
        staleTime: Infinity,
    });
}

export function useCacheMapa() {
    const perfilStore = usePerfilStore();

    function obterContextoSessao() {
        return criarContextoSessaoMapa(perfilStore);
    }

    function obterMapa(codigoSubprocesso: number): MapaCompleto | null {
        return useQueryCache().getQueryData(criarChaveMapa(codigoSubprocesso, obterContextoSessao())) ?? null;
    }

    function obterImpacto(codigoSubprocesso: number): ImpactoMapa | null {
        return useQueryCache().getQueryData(criarChaveImpactoMapa(codigoSubprocesso, obterContextoSessao())) ?? null;
    }

    function sincronizarMapa(codigoSubprocesso: number, mapa: MapaCompleto | null | undefined): void {
        useQueryCache().setQueryData(criarChaveMapa(codigoSubprocesso, obterContextoSessao()), mapa ?? null);
        invalidarImpacto(codigoSubprocesso);
    }

    function sincronizarImpacto(codigoSubprocesso: number, impacto: ImpactoMapa | null | undefined): void {
        useQueryCache().setQueryData(criarChaveImpactoMapa(codigoSubprocesso, obterContextoSessao()), impacto ?? null);
    }

    function invalidarMapa(codigoSubprocesso?: number): void {
        const queryCache = useQueryCache();
        if (typeof codigoSubprocesso === "number") {
            queryCache.invalidateQueries({key: criarChaveMapa(codigoSubprocesso, obterContextoSessao()), exact: true});
            invalidarImpacto(codigoSubprocesso);
            return;
        }

        queryCache.invalidateQueries({key: CHAVE_QUERY_MAPA});
        invalidarImpacto();
    }

    function invalidarImpacto(codigoSubprocesso?: number): void {
        const queryCache = useQueryCache();
        if (typeof codigoSubprocesso === "number") {
            queryCache.setQueryData(criarChaveImpactoMapa(codigoSubprocesso, obterContextoSessao()), null);
            queryCache.invalidateQueries({key: criarChaveImpactoMapa(codigoSubprocesso, obterContextoSessao()), exact: true}, false);
            return;
        }

        queryCache.invalidateQueries({key: CHAVE_QUERY_IMPACTO_MAPA}, false);
    }

    return {
        invalidarImpacto,
        invalidarMapa,
        obterImpacto,
        obterMapa,
        sincronizarImpacto,
        sincronizarMapa,
    };
}
