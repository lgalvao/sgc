import {computed, ref, watch, toValue, type MaybeRefOrGetter} from "vue";
import {useQuery, useQueryCache} from "@pinia/colada";
import {usePerfilStore} from "@/stores/perfil";
import {
    buscarArvoreUnidade,
    buscarArvoreComElegibilidade,
    buscarReferenciaMapaVigente,
} from "@/services/unidadeService";
import type {MapaVigenteReferencia, Unidade} from "@/types/tipos";
import {normalizarErro} from "@/utils/apiError";

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
            return Number.isFinite(cod) && cod > 0;
        },
        initialData: () => null,
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
            return Number.isFinite(cod) && cod > 0;
        },
        initialData: () => ({unidade: null, mapaVigente: null}),
        staleTime: Infinity,
    });
}

export function useArvoreElegibilidadeQuery(
    tipoProcesso: MaybeRefOrGetter<string | null>,
    codProcesso: MaybeRefOrGetter<number | undefined>
) {
    return useQuery<Unidade[], Error, Unidade[]>({
        key: () => [...CHAVE_QUERY_ARVORE_ELEGIBILIDADE, toValue(tipoProcesso) ?? "nenhuma", String(toValue(codProcesso) ?? "novo")],
        query: () => buscarArvoreComElegibilidade(toValue(tipoProcesso)!, toValue(codProcesso)),
        enabled: () => !!toValue(tipoProcesso),
        initialData: () => [],
        staleTime: Infinity,
    });
}

export function useUnidade(codigoUnidade: MaybeRefOrGetter<number>) {
    const query = useDadosTelaUnidadeQuery(codigoUnidade);
    const erroLocal = ref<string | null>(null);

    const unidade = ref<Unidade | null>(null);
    const mapaVigente = ref<MapaVigenteReferencia | null>(null);
    const carregando = computed(() => query.isLoading.value);

    watch(() => query.data.value, (newData) => {
        unidade.value = newData?.unidade ?? null;
        mapaVigente.value = newData?.mapaVigente ?? null;
    }, { immediate: true });

    watch(() => query.error.value, (newError) => {
        if (newError) {
            erroLocal.value = normalizarErro(newError).mensagem;
        } else {
            erroLocal.value = null;
        }
    });

    let promessaCarregamento: Promise<void> | null = null;
    let promessaRecarregamento: Promise<void> | null = null;

    async function carregar(): Promise<void> {
        if (promessaCarregamento) {
            return promessaCarregamento;
        }
        erroLocal.value = null;
        promessaCarregamento = (async () => {
            try {
                await query.refetch(true);
            } catch (e: unknown) {
                erroLocal.value = normalizarErro(e).mensagem;
            } finally {
                promessaCarregamento = null;
            }
        })();
        return promessaCarregamento;
    }

    async function recarregar(): Promise<void> {
        if (promessaRecarregamento) {
            return promessaRecarregamento;
        }
        erroLocal.value = null;
        promessaRecarregamento = (async () => {
            try {
                await query.refresh(true);
            } catch (e: unknown) {
                erroLocal.value = normalizarErro(e).mensagem;
            } finally {
                promessaRecarregamento = null;
            }
        })();
        return promessaRecarregamento;
    }

    return {
        unidade,
        mapaVigente,
        carregando,
        erro: erroLocal,
        carregar,
        recarregar,
    };
}

export function useInvalidacaoUnidade() {
    return {
        invalidarUnidade: () => useQueryCache().invalidateQueries({key: CHAVE_QUERY_UNIDADE}),
        invalidarDadosTelaUnidade: () => useQueryCache().invalidateQueries({key: CHAVE_QUERY_DADOS_TELA_UNIDADE}),
        invalidarArvoreElegibilidade: () => useQueryCache().invalidateQueries({key: CHAVE_QUERY_ARVORE_ELEGIBILIDADE}),
    };
}

