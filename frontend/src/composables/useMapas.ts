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

async function carregarMapa<T>(
    buscar: () => Promise<T>,
    atribuir: (resultado: T) => void,
    mensagemErro: string,
) {
    await executarSilencioso(async () => {
        atribuir(await buscar());
    }, mensagemErro);
}

async function buscarMapaVisualizacao(codSubprocesso: number) {
    if (!codSubprocesso) {
        return;
    }

    await carregarMapa(
        () => obterMapaVisualizacao(codSubprocesso),
        resultado => {
            mapaVisualizacao.value = resultado;
        },
        "Erro ao carregar mapa de visualização."
    );
}

async function buscarMapaCompleto(codSubprocesso: number) {
    await carregarMapa(
        () => obterMapaCompleto(codSubprocesso),
        resultado => {
            mapaCompleto.value = resultado;
        },
        "Erro ao carregar mapa completo."
    );
}

async function buscarMapaAjuste(codSubprocesso: number) {
    await carregarMapa(
        () => obterMapaAjuste(codSubprocesso),
        resultado => {
            mapaAjuste.value = resultado;
        },
        "Erro ao carregar mapa para ajuste."
    );
}

async function buscarImpactoMapa(codSubprocesso: number) {
    if (!codSubprocesso) {
        return;
    }

    await carregarMapa(
        () => verificarImpactosMapa(codSubprocesso),
        resultado => {
            impactoMapa.value = resultado;
        },
        "Erro ao verificar impactos."
    );
}

async function temMapaVigente(codigoUnidade: number): Promise<boolean> {
    return verificarMapaVigente(codigoUnidade);
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
