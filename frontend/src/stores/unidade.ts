import {defineStore} from "pinia";
import {ref} from "vue";
import {
    buscarArvoreComElegibilidade,
    buscarArvoreUnidade,
    buscarReferenciaMapaVigente
} from "@/services/unidadeService";
import type {MapaVigenteReferencia, Unidade} from "@/types/tipos";

interface DadosTelaUnidade {
    unidade: Unidade | null;
    mapaVigente: MapaVigenteReferencia | null;
}

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

    function invalidar() {
        cacheArvoreElegibilidade.value.clear();
        cacheUnidades.value.clear();
        cacheMapasVigentes.value.clear();
        carregandoPromessas.clear();
    }

    function resetar() {
        invalidar();
    }

    async function carregarUnidadeDoCache(codigo: number): Promise<Unidade | null> {
        if (cacheUnidades.value.has(codigo)) {
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

    async function recarregarUnidadeNaOrigem(codigo: number): Promise<Unidade | null> {
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

    async function carregarReferenciaMapaVigenteDoCache(codigo: number): Promise<MapaVigenteReferencia | null> {
        if (cacheMapasVigentes.value.has(codigo)) {
            return cacheMapasVigentes.value.get(codigo)!;
        }
        const mapa = await buscarReferenciaMapaVigente(codigo);
        cacheMapasVigentes.value.set(codigo, mapa);
        return mapa;
    }

    async function recarregarReferenciaMapaVigenteNaOrigem(codigo: number): Promise<MapaVigenteReferencia | null> {
        const mapa = await buscarReferenciaMapaVigente(codigo);
        cacheMapasVigentes.value.set(codigo, mapa);
        return mapa;
    }

    async function obterUnidade(codigo: number): Promise<Unidade | null> {
        return carregarUnidadeDoCache(codigo);
    }

    async function recarregarUnidade(codigo: number): Promise<Unidade | null> {
        return recarregarUnidadeNaOrigem(codigo);
    }

    async function obterReferenciaMapaVigente(codigo: number): Promise<MapaVigenteReferencia | null> {
        return carregarReferenciaMapaVigenteDoCache(codigo);
    }

    async function recarregarReferenciaMapaVigente(codigo: number): Promise<MapaVigenteReferencia | null> {
        return recarregarReferenciaMapaVigenteNaOrigem(codigo);
    }

    function possuiDadosTelaUnidade(codigo: number): boolean {
        return cacheUnidades.value.has(codigo) && cacheMapasVigentes.value.has(codigo);
    }

    async function obterDadosTelaUnidade(codigo: number): Promise<DadosTelaUnidade> {
        const [unidade, mapaVigente] = await Promise.all([
            obterUnidade(codigo),
            obterReferenciaMapaVigente(codigo),
        ]);
        return {unidade, mapaVigente};
    }

    async function recarregarDadosTelaUnidade(codigo: number): Promise<DadosTelaUnidade> {
        const [unidade, mapaVigente] = await Promise.all([
            recarregarUnidade(codigo),
            recarregarReferenciaMapaVigente(codigo),
        ]);
        return {unidade, mapaVigente};
    }

    return {
        cacheArvoreElegibilidade,
        cacheUnidades,
        cacheMapasVigentes,
        garantirArvoreElegibilidade,
        possuiDadosTelaUnidade,
        obterDadosTelaUnidade,
        recarregarDadosTelaUnidade,
        obterUnidade,
        recarregarUnidade,
        obterReferenciaMapaVigente,
        recarregarReferenciaMapaVigente,
        invalidar,
        resetar,
    };
});
