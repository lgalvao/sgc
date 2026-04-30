import {defineStore} from "pinia";
import {ref} from "vue";
import {
  buscarArvoreUnidade,
  buscarArvoreComElegibilidade,
  buscarReferenciaMapaVigente,
  mapUnidadesArray,
  mapUnidade
} from "@/services/unidadeService";
import type {MapaVigenteReferencia, Unidade} from "@/types/tipos";
import {logger} from "@/utils";

/**
 * Store de cache para dados da estrutura organizacional.
 *
 * Cacheia a árvore de unidades com elegibilidade, que é custosa para o backend
 * e não muda frequentemente durante a sessão de edição.
 */
export const useUnidadeStore = defineStore("unidade", () => {
    // Mapa de cache indexado por `${tipoProcesso}_${codProcesso ?? 'novo'}`
    const cacheArvoreElegibilidade = ref<Map<string, Unidade[]>>(new Map());
    const cacheUnidades = ref<Map<number, Unidade>>(new Map());
    const cacheMapasVigentes = ref<Map<number, MapaVigenteReferencia | null>>(new Map());
    const carregando = ref(new Set<string>());

    /**
     * Garante que a árvore de unidades para um determinado tipo de processo esteja no cache.
     */
    async function garantirArvoreElegibilidade(tipoProcesso: string, codProcesso?: number): Promise<Unidade[]> {
        const key = `${tipoProcesso}_${codProcesso ?? 'novo'}`;

        if (cacheArvoreElegibilidade.value.has(key)) {
            return cacheArvoreElegibilidade.value.get(key)!;
        }

        // Evita chamadas paralelas para a mesma chave
        if (carregando.value.has(key)) {
            while (carregando.value.has(key)) {
                await new Promise(resolve => setTimeout(resolve, 50));
            }
            return cacheArvoreElegibilidade.value.get(key) || [];
        }

        carregando.value.add(key);
        try {
            const response = await buscarArvoreComElegibilidade(tipoProcesso, codProcesso);
            const unidadesMapeadas = mapUnidadesArray(Array.isArray(response) ? response : []);
            cacheArvoreElegibilidade.value.set(key, unidadesMapeadas);
            return unidadesMapeadas;
        } catch (error) {
            logger.error(`Erro ao buscar árvore de unidades (${key}):`, error);
            return [];
        } finally {
            carregando.value.delete(key);
        }
    }

    function invalidarCache() {
        cacheArvoreElegibilidade.value.clear();
        cacheUnidades.value.clear();
        cacheMapasVigentes.value.clear();
    }

    async function obterUnidade(codigo: number): Promise<Unidade> {
        if (cacheUnidades.value.has(codigo)) {
            return cacheUnidades.value.get(codigo)!;
        }
        const response = await buscarArvoreUnidade(codigo);
        const unidade = mapUnidade(response);
        cacheUnidades.value.set(codigo, unidade);
        return unidade;
    }

    async function obterReferenciaMapaVigente(codigo: number): Promise<MapaVigenteReferencia | null> {
        if (cacheMapasVigentes.value.has(codigo)) {
            return cacheMapasVigentes.value.get(codigo)!;
        }
        const mapa = await buscarReferenciaMapaVigente(codigo);
        cacheMapasVigentes.value.set(codigo, mapa);
        return mapa;
    }

    return {
        cacheArvoreElegibilidade,
        cacheUnidades,
        cacheMapasVigentes,
        garantirArvoreElegibilidade,
        obterUnidade,
        obterReferenciaMapaVigente,
        invalidarCache
    };
});
