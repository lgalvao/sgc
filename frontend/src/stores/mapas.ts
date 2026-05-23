import {defineStore} from "pinia";
import {ref, type Ref} from "vue";
import type {ImpactoMapa, MapaCompleto} from "@/types/tipos";
import {
    obterMapaCompleto as serviceObterMapaCompleto,
    verificarImpactosMapa as serviceVerificarImpactosMapa,
} from "@/services/subprocessoService";

interface EstadoMapasRefs {
    cacheMapaCompleto: Ref<Map<number, MapaCompleto>>;
    codigosMapaInvalidos: Ref<Set<number>>;
    mapaCompleto: Ref<MapaCompleto | null>;
    impactoMapa: Ref<ImpactoMapa | null>;
    codigoMapaAtual: Ref<number | null>;
    codigoImpactoAtual: Ref<number | null>;
}

interface CarregamentosMapas {
    mapa: Map<number, Promise<MapaCompleto>>;
    impacto: Map<number, Promise<ImpactoMapa>>;
}

interface ContextoMapas {
    estado: EstadoMapasRefs;
    carregamentos: CarregamentosMapas;
}

function sincronizarMapaAtual(estado: EstadoMapasRefs, codigo: number | null): void {
    estado.codigoMapaAtual.value = codigo;
    estado.mapaCompleto.value = codigo === null ? null : (estado.cacheMapaCompleto.value.get(codigo) ?? null);
}

function sincronizarImpactoAtual(
    estado: EstadoMapasRefs,
    codigo: number | null,
    impacto: ImpactoMapa | null = null,
): void {
    estado.codigoImpactoAtual.value = codigo;
    estado.impactoMapa.value = codigo === null ? null : impacto;
}

function obterMapaCompletoCache(estado: EstadoMapasRefs, codigoSubprocesso: number): MapaCompleto | null {
    return estado.cacheMapaCompleto.value.get(codigoSubprocesso) ?? null;
}

function obterImpactoMapaCache(estado: EstadoMapasRefs, codigoSubprocesso: number): ImpactoMapa | null {
    return estado.codigoImpactoAtual.value === codigoSubprocesso ? estado.impactoMapa.value : null;
}

function dadosMapaValidos(estado: EstadoMapasRefs, codigoSubprocesso: number): boolean {
    return estado.cacheMapaCompleto.value.has(codigoSubprocesso)
        && !estado.codigosMapaInvalidos.value.has(codigoSubprocesso);
}

function dadosImpactoValidos(estado: EstadoMapasRefs, codigoSubprocesso: number): boolean {
    return estado.codigoImpactoAtual.value === codigoSubprocesso && estado.impactoMapa.value !== null;
}

function invalidarImpacto(
    estado: EstadoMapasRefs,
    carregamentos: CarregamentosMapas,
    codigoSubprocesso?: number,
): void {
    if (typeof codigoSubprocesso === "number") {
        carregamentos.impacto.delete(codigoSubprocesso);
        if (estado.codigoImpactoAtual.value === codigoSubprocesso) {
            sincronizarImpactoAtual(estado, null);
        }
        return;
    }

    carregamentos.impacto.clear();
    sincronizarImpactoAtual(estado, null);
}

function definirMapaCompleto(contexto: ContextoMapas, codigoSubprocesso: number, mapa: MapaCompleto | null): void {
    if (mapa) {
        contexto.estado.cacheMapaCompleto.value.set(codigoSubprocesso, mapa);
    } else {
        contexto.estado.cacheMapaCompleto.value.delete(codigoSubprocesso);
    }

    contexto.estado.codigosMapaInvalidos.value.delete(codigoSubprocesso);
    invalidarImpacto(contexto.estado, contexto.carregamentos, codigoSubprocesso);
    sincronizarMapaAtual(contexto.estado, mapa ? codigoSubprocesso : null);
}

function definirImpactoMapa(estado: EstadoMapasRefs, codigoSubprocesso: number, impacto: ImpactoMapa | null): void {
    sincronizarImpactoAtual(estado, codigoSubprocesso, impacto);
}

function criarCarregamentoMapa(
    estado: EstadoMapasRefs,
    carregamentos: CarregamentosMapas,
    codigoSubprocesso: number,
): Promise<MapaCompleto> {
    const contexto = {estado, carregamentos};
    const promessaCarregamento = serviceObterMapaCompleto(codigoSubprocesso)
        .then((mapa) => {
            definirMapaCompleto(contexto, codigoSubprocesso, mapa);
            return mapa;
        })
        .finally(() => carregamentos.mapa.delete(codigoSubprocesso));

    carregamentos.mapa.set(codigoSubprocesso, promessaCarregamento);
    return promessaCarregamento;
}

function criarCarregamentoImpacto(
    estado: EstadoMapasRefs,
    carregamentos: CarregamentosMapas,
    codigoSubprocesso: number,
): Promise<ImpactoMapa> {
    const promessaCarregamento = serviceVerificarImpactosMapa(codigoSubprocesso)
        .then((impacto) => {
            definirImpactoMapa(estado, codigoSubprocesso, impacto);
            return impacto;
        })
        .finally(() => carregamentos.impacto.delete(codigoSubprocesso));

    carregamentos.impacto.set(codigoSubprocesso, promessaCarregamento);
    return promessaCarregamento;
}

function invalidarMapa(
    estado: EstadoMapasRefs,
    carregamentos: CarregamentosMapas,
    codigoSubprocesso?: number,
): void {
    if (typeof codigoSubprocesso === "number") {
        estado.codigosMapaInvalidos.value.add(codigoSubprocesso);
        carregamentos.mapa.delete(codigoSubprocesso);
        invalidarImpacto(estado, carregamentos, codigoSubprocesso);
        return;
    }

    estado.codigosMapaInvalidos.value = new Set(estado.cacheMapaCompleto.value.keys());
    carregamentos.mapa.clear();
    invalidarImpacto(estado, carregamentos);
}

function resetarMapas(estado: EstadoMapasRefs, carregamentos: CarregamentosMapas): void {
    estado.cacheMapaCompleto.value.clear();
    estado.codigosMapaInvalidos.value.clear();
    carregamentos.mapa.clear();
    carregamentos.impacto.clear();
    sincronizarMapaAtual(estado, null);
    sincronizarImpactoAtual(estado, null);
}

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
    const estado: EstadoMapasRefs = {
        cacheMapaCompleto,
        codigosMapaInvalidos,
        mapaCompleto,
        impactoMapa,
        codigoMapaAtual,
        codigoImpactoAtual,
    };
    const carregamentos: CarregamentosMapas = {
        mapa: new Map<number, Promise<MapaCompleto>>(),
        impacto: new Map<number, Promise<ImpactoMapa>>(),
    };
    const contexto = {estado, carregamentos};

    async function garantirMapaCompleto(codigoSubprocesso: number): Promise<MapaCompleto> {
        if (dadosMapaValidos(estado, codigoSubprocesso)) {
            sincronizarMapaAtual(estado, codigoSubprocesso);
            return estado.cacheMapaCompleto.value.get(codigoSubprocesso)!;
        }

        const carregamentoExistente = carregamentos.mapa.get(codigoSubprocesso);
        if (carregamentoExistente) {
            sincronizarMapaAtual(estado, codigoSubprocesso);
            return carregamentoExistente;
        }

        return criarCarregamentoMapa(estado, carregamentos, codigoSubprocesso);
    }

    async function garantirImpactoMapa(codigoSubprocesso: number): Promise<ImpactoMapa> {
        if (dadosImpactoValidos(estado, codigoSubprocesso)) {
            return estado.impactoMapa.value!;
        }

        const carregamentoExistente = carregamentos.impacto.get(codigoSubprocesso);
        if (carregamentoExistente) {
            return carregamentoExistente;
        }

        return criarCarregamentoImpacto(estado, carregamentos, codigoSubprocesso);
    }

    function invalidarImpactoStore(codigoSubprocesso?: number): void {
        invalidarImpacto(estado, carregamentos, codigoSubprocesso);
    }

    function definirMapaCompletoStore(codigoSubprocesso: number, mapa: MapaCompleto | null): void {
        definirMapaCompleto(contexto, codigoSubprocesso, mapa);
    }

    function definirImpactoMapaStore(codigoSubprocesso: number, impacto: ImpactoMapa | null): void {
        definirImpactoMapa(estado, codigoSubprocesso, impacto);
    }

    function invalidar(codigoSubprocesso?: number): void {
        invalidarMapa(estado, carregamentos, codigoSubprocesso);
    }

    function resetar(): void {
        resetarMapas(estado, carregamentos);
    }

    return {
        cacheMapaCompleto,
        codigosMapaInvalidos,
        mapaCompleto,
        impactoMapa,
        codigoMapaAtual,
        codigoImpactoAtual,
        obterMapaCompletoCache: (codigoSubprocesso: number) => obterMapaCompletoCache(estado, codigoSubprocesso),
        obterImpactoMapaCache: (codigoSubprocesso: number) => obterImpactoMapaCache(estado, codigoSubprocesso),
        dadosMapaValidos: (codigoSubprocesso: number) => dadosMapaValidos(estado, codigoSubprocesso),
        dadosImpactoValidos: (codigoSubprocesso: number) => dadosImpactoValidos(estado, codigoSubprocesso),
        definirMapaCompleto: definirMapaCompletoStore,
        definirImpactoMapa: definirImpactoMapaStore,
        garantirMapaCompleto,
        garantirImpactoMapa,
        invalidarImpacto: invalidarImpactoStore,
        invalidar,
        resetar,
    };
});
