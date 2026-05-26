import {defineStore} from "pinia";
import {ref, type Ref} from "vue";
import type {ImpactoMapa, MapaCompleto} from "@/types/tipos";
import {
    obterMapaCompleto as serviceObterMapaCompleto,
    verificarImpactosMapa as serviceVerificarImpactosMapa,
} from "@/services/subprocessoService";

interface EstadoMapasRefs {
    mapasPorSubprocesso: Ref<Map<number, MapaCompleto>>;
    mapasPendentesAtualizacao: Ref<Set<number>>;
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
    estado.mapaCompleto.value = codigo === null ? null : (estado.mapasPorSubprocesso.value.get(codigo) ?? null);
}

function sincronizarImpactoAtual(
    estado: EstadoMapasRefs,
    codigo: number | null,
    impacto: ImpactoMapa | null = null,
): void {
    estado.codigoImpactoAtual.value = codigo;
    estado.impactoMapa.value = codigo === null ? null : impacto;
}

function obterMapa(estado: EstadoMapasRefs, codigoSubprocesso: number): MapaCompleto | null {
    return estado.mapasPorSubprocesso.value.get(codigoSubprocesso) ?? null;
}

function obterImpacto(estado: EstadoMapasRefs, codigoSubprocesso: number): ImpactoMapa | null {
    return estado.codigoImpactoAtual.value === codigoSubprocesso ? estado.impactoMapa.value : null;
}

function mapaDisponivel(estado: EstadoMapasRefs, codigoSubprocesso: number): boolean {
    return estado.mapasPorSubprocesso.value.has(codigoSubprocesso)
        && !estado.mapasPendentesAtualizacao.value.has(codigoSubprocesso);
}

function impactoDisponivel(estado: EstadoMapasRefs, codigoSubprocesso: number): boolean {
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

function sincronizarMapa(contexto: ContextoMapas, codigoSubprocesso: number, mapa: MapaCompleto | null): void {
    if (mapa) {
        contexto.estado.mapasPorSubprocesso.value.set(codigoSubprocesso, mapa);
    } else {
        contexto.estado.mapasPorSubprocesso.value.delete(codigoSubprocesso);
    }

    contexto.estado.mapasPendentesAtualizacao.value.delete(codigoSubprocesso);
    invalidarImpacto(contexto.estado, contexto.carregamentos, codigoSubprocesso);
    sincronizarMapaAtual(contexto.estado, mapa ? codigoSubprocesso : null);
}

function sincronizarImpacto(estado: EstadoMapasRefs, codigoSubprocesso: number, impacto: ImpactoMapa | null): void {
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
            sincronizarMapa(contexto, codigoSubprocesso, mapa);
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
            sincronizarImpacto(estado, codigoSubprocesso, impacto);
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
        estado.mapasPendentesAtualizacao.value.add(codigoSubprocesso);
        carregamentos.mapa.delete(codigoSubprocesso);
        invalidarImpacto(estado, carregamentos, codigoSubprocesso);
        return;
    }

    estado.mapasPendentesAtualizacao.value = new Set(estado.mapasPorSubprocesso.value.keys());
    carregamentos.mapa.clear();
    invalidarImpacto(estado, carregamentos);
}

function resetarMapas(estado: EstadoMapasRefs, carregamentos: CarregamentosMapas): void {
    estado.mapasPorSubprocesso.value.clear();
    estado.mapasPendentesAtualizacao.value.clear();
    carregamentos.mapa.clear();
    carregamentos.impacto.clear();
    sincronizarMapaAtual(estado, null);
    sincronizarImpactoAtual(estado, null);
}

/**
 * Store de sessão para dados do mapa de competências.
 *
 * Política:
 * - escopo: sessão autenticada;
 * - chave: código do subprocesso;
 * - reaproveitamento: dados já resolvidos por subprocesso são reutilizados
 *   enquanto não houver marcação de atualização pendente;
 * - invalidação: qualquer ação de workflow/mutação que altere o mapa marca o
 *   dado como pendente de atualização, mas não o apaga imediatamente;
 * - reset: logout, troca de perfil e limpeza explícita de sessão removem todo
 *   o estado do store.
 *
 * Mantemos o último mapa acessado em refs simples para consumo reativo das
 * telas, enquanto a coleção efetiva segue indexada por subprocesso.
 */
export const useMapasStore = defineStore("mapas", () => {
    const mapasPorSubprocesso = ref<Map<number, MapaCompleto>>(new Map());
    const mapasPendentesAtualizacao = ref<Set<number>>(new Set());
    const mapaCompleto = ref<MapaCompleto | null>(null);
    const impactoMapa = ref<ImpactoMapa | null>(null);
    const codigoMapaAtual = ref<number | null>(null);
    const codigoImpactoAtual = ref<number | null>(null);
    const estado: EstadoMapasRefs = {
        mapasPorSubprocesso,
        mapasPendentesAtualizacao,
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

    async function carregarMapa(codigoSubprocesso: number): Promise<MapaCompleto> {
        if (mapaDisponivel(estado, codigoSubprocesso)) {
            sincronizarMapaAtual(estado, codigoSubprocesso);
            return estado.mapasPorSubprocesso.value.get(codigoSubprocesso)!;
        }

        const carregamentoExistente = carregamentos.mapa.get(codigoSubprocesso);
        if (carregamentoExistente) {
            sincronizarMapaAtual(estado, codigoSubprocesso);
            return carregamentoExistente;
        }

        return criarCarregamentoMapa(estado, carregamentos, codigoSubprocesso);
    }

    async function carregarImpacto(codigoSubprocesso: number): Promise<ImpactoMapa> {
        if (impactoDisponivel(estado, codigoSubprocesso)) {
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

    function sincronizarMapaStore(codigoSubprocesso: number, mapa: MapaCompleto | null): void {
        sincronizarMapa(contexto, codigoSubprocesso, mapa);
    }

    function sincronizarImpactoStore(codigoSubprocesso: number, impacto: ImpactoMapa | null): void {
        sincronizarImpacto(estado, codigoSubprocesso, impacto);
    }

    function invalidar(codigoSubprocesso?: number): void {
        invalidarMapa(estado, carregamentos, codigoSubprocesso);
    }

    function marcarMapaParaAtualizacao(codigoSubprocesso: number): void {
        invalidar(codigoSubprocesso);
    }

    function resetar(): void {
        resetarMapas(estado, carregamentos);
    }

    return {
        mapasPorSubprocesso,
        mapasPendentesAtualizacao,
        mapaCompleto,
        impactoMapa,
        codigoMapaAtual,
        codigoImpactoAtual,
        obterMapa: (codigoSubprocesso: number) => obterMapa(estado, codigoSubprocesso),
        obterImpacto: (codigoSubprocesso: number) => obterImpacto(estado, codigoSubprocesso),
        mapaDisponivel: (codigoSubprocesso: number) => mapaDisponivel(estado, codigoSubprocesso),
        impactoDisponivel: (codigoSubprocesso: number) => impactoDisponivel(estado, codigoSubprocesso),
        sincronizarMapa: sincronizarMapaStore,
        sincronizarImpacto: sincronizarImpactoStore,
        carregarMapa,
        carregarImpacto,
        marcarMapaParaAtualizacao,
        invalidarImpacto: invalidarImpactoStore,
        invalidar,
        resetar,
    };
});
