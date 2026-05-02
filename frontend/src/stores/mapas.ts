import {defineStore} from "pinia";
import {ref} from "vue";
import type {ImpactoMapa, MapaCompleto} from "@/types/tipos";
import {
    obterMapaCompleto as serviceObterMapaCompleto,
    verificarImpactosMapa as serviceVerificarImpactosMapa,
} from "@/services/subprocessoService";
import {logger} from "@/utils";

/**
 * Store de cache de sessão para dados do mapa de competências.
 *
 * Política:
 * - escopo: sessão autenticada;
 * - chave: código do subprocesso;
 * - validade: sem TTL; reaproveita ao reativar a view enquanto o subprocesso
 *   não for invalidado explicitamente;
 * - invalidação: logout, troca de perfil, SSE organizacional e qualquer ação
 *   de workflow/mutação que altere o mapa.
 *
 * Mantemos o último snapshot acessado em refs simples por compatibilidade com
 * testes e composables legados, mas o cache efetivo é indexado por subprocesso.
 */
export const useMapasStore = defineStore("mapas", () => {
    const cacheMapaCompleto = ref<Map<number, MapaCompleto>>(new Map());
    const cacheImpactoMapa = ref<Map<number, ImpactoMapa | null>>(new Map());
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

    function sincronizarImpactoAtual(codigo: number | null): void {
        codigoImpactoAtual.value = codigo;
        impactoMapa.value = codigo === null ? null : (cacheImpactoMapa.value.get(codigo) ?? null);
    }

    function obterMapaCompletoCache(codigoSubprocesso: number): MapaCompleto | null {
        return cacheMapaCompleto.value.get(codigoSubprocesso) ?? null;
    }

    function obterImpactoMapaCache(codigoSubprocesso: number): ImpactoMapa | null {
        return cacheImpactoMapa.value.get(codigoSubprocesso) ?? null;
    }

    function dadosMapaValidos(codigoSubprocesso: number): boolean {
        return cacheMapaCompleto.value.has(codigoSubprocesso);
    }

    function dadosImpactoValidos(codigoSubprocesso: number): boolean {
        return cacheImpactoMapa.value.has(codigoSubprocesso);
    }

    function invalidarImpacto(codigoSubprocesso?: number): void {
        if (typeof codigoSubprocesso === "number") {
            cacheImpactoMapa.value.delete(codigoSubprocesso);
            carregamentosImpacto.delete(codigoSubprocesso);
            if (codigoImpactoAtual.value === codigoSubprocesso) {
                sincronizarImpactoAtual(null);
            }
            return;
        }

        cacheImpactoMapa.value.clear();
        carregamentosImpacto.clear();
        sincronizarImpactoAtual(null);
    }

    function definirMapaCompleto(codigoSubprocesso: number, mapa: MapaCompleto | null): void {
        if (mapa) {
            cacheMapaCompleto.value.set(codigoSubprocesso, mapa);
        } else {
            cacheMapaCompleto.value.delete(codigoSubprocesso);
        }
        invalidarImpacto(codigoSubprocesso);
        sincronizarMapaAtual(mapa ? codigoSubprocesso : null);
    }

    function definirImpactoMapa(codigoSubprocesso: number, impacto: ImpactoMapa | null): void {
        cacheImpactoMapa.value.set(codigoSubprocesso, impacto);
        sincronizarImpactoAtual(codigoSubprocesso);
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

        const promessaCarregamento = (async () => {
            try {
                const mapa = await serviceObterMapaCompleto(codigoSubprocesso);
                definirMapaCompleto(codigoSubprocesso, mapa);
                return mapa;
            } catch (erro) {
                logger.error(`Erro ao carregar mapa completo do subprocesso ${codigoSubprocesso}:`, erro);
                throw erro;
            } finally {
                carregamentosMapa.delete(codigoSubprocesso);
            }
        })();

        carregamentosMapa.set(codigoSubprocesso, promessaCarregamento);
        return promessaCarregamento;
    }

    async function garantirImpactoMapa(codigoSubprocesso: number): Promise<ImpactoMapa> {
        if (dadosImpactoValidos(codigoSubprocesso)) {
            sincronizarImpactoAtual(codigoSubprocesso);
            return cacheImpactoMapa.value.get(codigoSubprocesso)! as ImpactoMapa;
        }

        const carregamentoExistente = carregamentosImpacto.get(codigoSubprocesso);
        if (carregamentoExistente) {
            sincronizarImpactoAtual(codigoSubprocesso);
            return carregamentoExistente;
        }

        const promessaCarregamento = (async () => {
            try {
                const impacto = await serviceVerificarImpactosMapa(codigoSubprocesso);
                definirImpactoMapa(codigoSubprocesso, impacto);
                return impacto;
            } catch (erro) {
                logger.error(`Erro ao verificar impactos do mapa ${codigoSubprocesso}:`, erro);
                throw erro;
            } finally {
                carregamentosImpacto.delete(codigoSubprocesso);
            }
        })();

        carregamentosImpacto.set(codigoSubprocesso, promessaCarregamento);
        return promessaCarregamento;
    }

    function invalidar(codigoSubprocesso?: number): void {
        if (typeof codigoSubprocesso === "number") {
            cacheMapaCompleto.value.delete(codigoSubprocesso);
            carregamentosMapa.delete(codigoSubprocesso);
            if (codigoMapaAtual.value === codigoSubprocesso) {
                sincronizarMapaAtual(null);
            }
            invalidarImpacto(codigoSubprocesso);
            return;
        }

        cacheMapaCompleto.value.clear();
        carregamentosMapa.clear();
        sincronizarMapaAtual(null);
        invalidarImpacto();
    }

    return {
        cacheMapaCompleto,
        cacheImpactoMapa,
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
    };
});
