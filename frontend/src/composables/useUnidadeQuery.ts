import {computed, type MaybeRefOrGetter, toValue} from "vue";
import {useQuery, useQueryCache} from "@pinia/colada";
import {usePerfilStore} from "@/stores/perfil";
import {
    buscarArvoreComElegibilidade,
    buscarArvoreUnidade,
    buscarReferenciaMapaVigente,
} from "@/services/unidadeService";
import type {MapaVigenteReferencia, Unidade} from "@/types/tipos";

export interface DadosTelaUnidade {
    unidade: Unidade | null;
    mapaVigente: MapaVigenteReferencia | null;
}

export const CHAVE_QUERY_UNIDADE = ["unidade"] as const;
export const CHAVE_QUERY_DADOS_TELA_UNIDADE = ["dados-tela-unidade"] as const;
export const CHAVE_QUERY_ARVORE_ELEGIBILIDADE = ["unidade", "arvore-elegibilidade"] as const;

type ContextoSessaoUnidade = readonly [string, string, string];

function criarContextoSessaoUnidade(perfilStore: ReturnType<typeof usePerfilStore>): ContextoSessaoUnidade {
    return [
        perfilStore.usuarioCodigo ?? "anon",
        String(perfilStore.perfilSelecionado ?? "sem-perfil"),
        String(perfilStore.unidadeSelecionada ?? "sem-unidade"),
    ] as const;
}

export function criarChaveUnidade(codigoUnidade: number, contextoSessao: ContextoSessaoUnidade) {
    return [...CHAVE_QUERY_UNIDADE, ...contextoSessao, codigoUnidade] as const;
}

export function criarChaveDadosTelaUnidade(codigoUnidade: number, contextoSessao: ContextoSessaoUnidade) {
    return [...CHAVE_QUERY_DADOS_TELA_UNIDADE, ...contextoSessao, codigoUnidade] as const;
}

export function useUnidadeQuery(codigoUnidade: MaybeRefOrGetter<number>) {
    const perfilStore = usePerfilStore();
    const chave = computed(() =>
        criarChaveUnidade(toValue(codigoUnidade), criarContextoSessaoUnidade(perfilStore))
    );

    return useQuery<Unidade | null, Error, Unidade | null>({
        key: () => [...chave.value],
        query: async () => {
            const cod = toValue(codigoUnidade);
            const response = await buscarArvoreUnidade(cod);
            return response?.codigo ? response : null;
        },
        enabled: () => {
            const cod = toValue(codigoUnidade);
            return Number.isFinite(cod) && cod > 0 && !!perfilStore.perfilSelecionado;
        },
        staleTime: Infinity,
    });
}

export function useDadosTelaUnidadeQuery(codigoUnidade: MaybeRefOrGetter<number>) {
    const perfilStore = usePerfilStore();
    const chave = computed(() =>
        criarChaveDadosTelaUnidade(toValue(codigoUnidade), criarContextoSessaoUnidade(perfilStore))
    );

    return useQuery<DadosTelaUnidade, Error, DadosTelaUnidade>({
        key: () => [...chave.value],
        query: async () => {
            const cod = toValue(codigoUnidade);
            const [unidade, mapaVigente] = await Promise.all([
                buscarArvoreUnidade(cod),
                buscarReferenciaMapaVigente(cod),
            ]);

            return {
                unidade: unidade?.codigo ? unidade : null,
                mapaVigente,
            };
        },
        enabled: () => {
            const cod = toValue(codigoUnidade);
            return Number.isFinite(cod) && cod > 0 && !!perfilStore.perfilSelecionado;
        },
        staleTime: Infinity,
    });
}

export function useArvoreElegibilidadeQuery(
    tipoProcesso: MaybeRefOrGetter<string | null>,
    codProcesso: MaybeRefOrGetter<number | undefined>
) {
    const perfilStore = usePerfilStore();
    return useQuery<Unidade[], Error, Unidade[]>({
        key: () => [...CHAVE_QUERY_ARVORE_ELEGIBILIDADE, toValue(tipoProcesso) ?? "nenhuma", String(toValue(codProcesso) ?? "novo")],
        query: () => {
            const tipo = toValue(tipoProcesso);
            if (!tipo) return Promise.resolve([]);
            return buscarArvoreComElegibilidade(tipo, toValue(codProcesso));
        },
        enabled: () => !!toValue(tipoProcesso) && !!perfilStore.perfilSelecionado,
        initialData: () => [],
        staleTime: Infinity,
    });
}

export function useInvalidacaoUnidade() {
    return {
        invalidarUnidade: () => useQueryCache().invalidateQueries({key: CHAVE_QUERY_UNIDADE}),
        invalidarDadosTelaUnidade: () => useQueryCache().invalidateQueries({key: CHAVE_QUERY_DADOS_TELA_UNIDADE}),
        invalidarArvoreElegibilidade: () => useQueryCache().invalidateQueries({key: CHAVE_QUERY_ARVORE_ELEGIBILIDADE}),
    };
}
