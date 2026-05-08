import {defineStore} from "pinia";
import {ref} from "vue";
import type {ImpactoMapa, MapaCompleto} from "@/types/tipos";
import {
    obterMapaCompleto as serviceObterMapaCompleto,
    verificarImpactosMapa as serviceVerificarImpactosMapa,
} from "@/services/subprocessoService";

/**
 * Store de cache de sessão para dados do mapa de competências.
 *
 * Política:
 * - escopo: sessão autenticada;
 * - chave: código do subprocesso;
 * - validade: sem TTL; reaproveita ao reativar a view enquanto o subprocesso
 *   não for invalidado explicitamente;
 * - invalidação: qualquer ação de workflow/mutação que altere o mapa marca o
 *   snapshot como stale, mas não o apaga imediatamente;
 * - reset: logout, troca de perfil e limpeza explícita de sessão removem todo
 *   o estado do store.
 *
 * Mantemos o último snapshot acessado em refs simples por compatibilidade com
 * testes e composables legados, mas o cache efetivo é indexado por subprocesso.
 */
export const useMapasStore = defineStore("mapas", () => {
    const cacheMapaCompleto = ref<Map<number, MapaCompleto>>(new Map());
    const codigosMapaInvalidos = ref<Set<number>>(new Set());
    const mapaCompleto = ref<MapaCompleto | null>(null);
    const impactoMapa = ref<ImpactoMapa | null>(null);
    const codigoMapaAtual = ref<number | null>(null);
    const codigoImpactoAtual = ref<number | null>(null);
    const carregamentosMapa = new Map<number, Promise<MapaCompleto>>();
    const carregamentosImpacto = new Map<number, Promise<ImpactoMapa>>();

    function sincronizarMapaAtual(codigo: number | null): void {
        codigoMapaAtual.value = codigo;
        mapaCompleto.value = codigo === null ? null : (cacheMapaCompleto.value.get(codigo) ?? null);
    }

    function sincronizarImpactoAtual(codigo: number | null, impacto: ImpactoMapa | null = null): void {
        codigoImpactoAtual.value = codigo;
        impactoMapa.value = codigo === null ? null : impacto;
    }

    function obterMapaCompletoCache(codigoSubprocesso: number): MapaCompleto | null {
        return cacheMapaCompleto.value.get(codigoSubprocesso) ?? null;
    }

    function obterImpactoMapaCache(codigoSubprocesso: number): ImpactoMapa | null {
        return codigoImpactoAtual.value === codigoSubprocesso ? impactoMapa.value : null;
    }

    function dadosMapaValidos(codigoSubprocesso: number): boolean {
        return cacheMapaCompleto.value.has(codigoSubprocesso)
            && !codigosMapaInvalidos.value.has(codigoSubprocesso);
    }

    function dadosImpactoValidos(codigoSubprocesso: number): boolean {
        return codigoImpactoAtual.value === codigoSubprocesso && impactoMapa.value !== null;
    }

    function invalidarImpacto(codigoSubprocesso?: number): void {
        if (typeof codigoSubprocesso === "number") {
            carregamentosImpacto.delete(codigoSubprocesso);
            if (codigoImpactoAtual.value === codigoSubprocesso) {
                sincronizarImpactoAtual(null);
            }
            return;
        }

        carregamentosImpacto.clear();
        sincronizarImpactoAtual(null);
    }

    function definirMapaCompleto(codigoSubprocesso: number, mapa: MapaCompleto | null): void {
        if (mapa) {
            cacheMapaCompleto.value.set(codigoSubprocesso, mapa);
        } else {
            cacheMapaCompleto.value.delete(codigoSubprocesso);
        }
        codigosMapaInvalidos.value.delete(codigoSubprocesso);
        invalidarImpacto(codigoSubprocesso);
        sincronizarMapaAtual(mapa ? codigoSubprocesso : null);
    }

    function definirImpactoMapa(codigoSubprocesso: number, impacto: ImpactoMapa | null): void {
        sincronizarImpactoAtual(codigoSubprocesso, impacto);
    }

    async function garantirMapaCompleto(codigoSubprocesso: number): Promise<MapaCompleto> {
        if (dadosMapaValidos(codigoSubprocesso)) {
            sincronizarMapaAtual(codigoSubprocesso);
            return cacheMapaCompleto.value.get(codigoSubprocesso)!;
        }

        const carregamentoExistente = carregamentosMapa.get(codigoSubprocesso);
        if (carregamentoExistente) {
            sincronizarMapaAtual(codigoSubprocesso);
            return carregamentoExistente;
        }

        const promessaCarregamento = serviceObterMapaCompleto(codigoSubprocesso)
            .then(mapa => {
                definirMapaCompleto(codigoSubprocesso, mapa);
                return mapa;
            })
            .finally(() => carregamentosMapa.delete(codigoSubprocesso));

        carregamentosMapa.set(codigoSubprocesso, promessaCarregamento);
        return promessaCarregamento;
    }

    async function garantirImpactoMapa(codigoSubprocesso: number): Promise<ImpactoMapa> {
        const carregamentoExistente = carregamentosImpacto.get(codigoSubprocesso);
        if (carregamentoExistente) {
            return carregamentoExistente;
        }

        const promessaCarregamento = serviceVerificarImpactosMapa(codigoSubprocesso)
            .then(impacto => {
                definirImpactoMapa(codigoSubprocesso, impacto);
                return impacto;
            })
            .finally(() => carregamentosImpacto.delete(codigoSubprocesso));

        carregamentosImpacto.set(codigoSubprocesso, promessaCarregamento);
        return promessaCarregamento;
    }

    function invalidar(codigoSubprocesso?: number): void {
        if (typeof codigoSubprocesso === "number") {
            codigosMapaInvalidos.value.add(codigoSubprocesso);
            carregamentosMapa.delete(codigoSubprocesso);
            invalidarImpacto(codigoSubprocesso);
            return;
        }

        codigosMapaInvalidos.value = new Set(cacheMapaCompleto.value.keys());
        carregamentosMapa.clear();
        invalidarImpacto();
    }

    function resetar(): void {
        cacheMapaCompleto.value.clear();
        codigosMapaInvalidos.value.clear();
        carregamentosMapa.clear();
        carregamentosImpacto.clear();
        sincronizarMapaAtual(null);
        sincronizarImpactoAtual(null);
    }

    return {
        cacheMapaCompleto,
        codigosMapaInvalidos,
        mapaCompleto,
        impactoMapa,
        codigoMapaAtual,
        codigoImpactoAtual,
        obterMapaCompletoCache,
        obterImpactoMapaCache,
        dadosMapaValidos,
        dadosImpactoValidos,
        definirMapaCompleto,
        definirImpactoMapa,
        garantirMapaCompleto,
        garantirImpactoMapa,
        invalidarImpacto,
        invalidar,
        resetar,
    };
});
