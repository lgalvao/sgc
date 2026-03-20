import {ref} from "vue";
import {
    obterMapaAjuste,
    obterMapaCompleto,
    obterMapaVisualizacao,
    verificarImpactosMapa,
    verificarMapaVigente
} from "@/services/subprocessoService";
import type {ImpactoMapa, MapaAjuste, MapaCompleto, MapaVisualizacao} from "@/types/tipos";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {useAsyncAction} from "@/composables/useAsyncAction";

const mapaVisualizacao = ref<MapaVisualizacao | null>(null);
const mapaCompleto = ref<MapaCompleto | null>(null);
const mapaAjuste = ref<MapaAjuste | null>(null);
const impactoMapa = ref<ImpactoMapa | null>(null);
const {lastError, clearError} = useErrorHandler();
const {carregando, erro, executarSilencioso} = useAsyncAction();

async function buscarMapaVisualizacao(codSubprocesso: number) {
    await executarSilencioso(async () => {
        if (codSubprocesso) {
            mapaVisualizacao.value = await obterMapaVisualizacao(codSubprocesso);
        }
    }, "Erro ao carregar mapa de visualização.");
}

async function buscarMapaCompleto(codSubprocesso: number) {
    await executarSilencioso(async () => {
        mapaCompleto.value = await obterMapaCompleto(codSubprocesso);
    }, "Erro ao carregar mapa completo.");
}

async function buscarMapaAjuste(codSubprocesso: number) {
    await executarSilencioso(async () => {
        mapaAjuste.value = await obterMapaAjuste(codSubprocesso);
    }, "Erro ao carregar mapa para ajuste.");
}

async function buscarImpactoMapa(codSubprocesso: number) {
    await executarSilencioso(async () => {
        if (codSubprocesso) {
            impactoMapa.value = await verificarImpactosMapa(codSubprocesso);
        }
    }, "Erro ao verificar impactos.");
}

async function temMapaVigente(codigoUnidade: number): Promise<boolean> {
    try {
        return await verificarMapaVigente(codigoUnidade);
    } catch {
        return false;
    }
}

const mapas = {
    mapaVisualizacao,
    mapaCompleto,
    mapaAjuste,
    impactoMapa,
    carregando,
    erro,
    lastError,
    clearError,
    buscarMapaVisualizacao,
    buscarMapaCompleto,
    buscarMapaAjuste,
    buscarImpactoMapa,
    temMapaVigente,
};

export function useMapas() {
    return mapas;
}
