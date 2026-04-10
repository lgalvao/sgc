import {ref} from "vue";
import {obterMapaCompleto, verificarImpactosMapa} from "@/services/subprocessoService";
import type {ImpactoMapa, MapaCompleto} from "@/types/tipos";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {useAsyncAction} from "@/composables/useAsyncAction";

const mapaCompleto = ref<MapaCompleto | null>(null);
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

async function buscarMapaCompleto(codSubprocesso: number) {
    await carregarMapa(
        () => obterMapaCompleto(codSubprocesso),
        resultado => {
            mapaCompleto.value = resultado;
        },
        "Erro ao carregar mapa completo."
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

const mapas = {
    mapaCompleto,
    impactoMapa,
    carregando,
    erro,
    lastError,
    clearError,
    buscarMapaCompleto,
    buscarImpactoMapa,
};

export function useMapas() {
    return mapas;
}
