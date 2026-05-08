import {defineStore} from "pinia";
import {ref} from "vue";
import {
    buscarArvoreComElegibilidade,
    buscarArvoreUnidade,
    buscarReferenciaMapaVigente
} from "@/services/unidadeService";
import type {MapaVigenteReferencia, Unidade} from "@/types/tipos";

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
    // Mapa de promessas em andamento para deduplicar requisições paralelas
    const carregandoPromessas = new Map<string, Promise<Unidade[]>>();

    /**
     * Garante que a árvore de unidades para um determinado tipo de processo esteja no cache.
     */
    async function garantirArvoreElegibilidade(tipoProcesso: string, codProcesso?: number): Promise<Unidade[]> {
        const key = `${tipoProcesso}_${codProcesso ?? 'novo'}`;

        if (cacheArvoreElegibilidade.value.has(key)) {
            return cacheArvoreElegibilidade.value.get(key)!;
        }

        const promessaExistente = carregandoPromessas.get(key);
        if (promessaExistente) {
            return promessaExistente;
        }

        const promessa = buscarArvoreComElegibilidade(tipoProcesso, codProcesso)
            .then(unidades => {
                cacheArvoreElegibilidade.value.set(key, unidades);
                return unidades;
            })
            .finally(() => carregandoPromessas.delete(key));

        carregandoPromessas.set(key, promessa);
        return promessa;
    }

    function invalidarCache() {
        cacheArvoreElegibilidade.value.clear();
        cacheUnidades.value.clear();
        cacheMapasVigentes.value.clear();
        carregandoPromessas.clear();
    }

    async function obterUnidade(codigo: number, forcar = false): Promise<Unidade | null> {
        if (!forcar && cacheUnidades.value.has(codigo)) {
            return cacheUnidades.value.get(codigo)!;
        }
        const response = await buscarArvoreUnidade(codigo);
        if (!response) {
            return null;
        }
        if (response.codigo) {
            cacheUnidades.value.set(codigo, response);
            return response;
        }
        return null;
    }

    async function obterReferenciaMapaVigente(codigo: number, forcar = false): Promise<MapaVigenteReferencia | null> {
        if (!forcar && cacheMapasVigentes.value.has(codigo)) {
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
