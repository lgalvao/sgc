import type {Ref} from "vue";
import {getCurrentInstance, onActivated, onMounted, ref} from "vue";
import type {ContextoEdicaoSubprocesso, Unidade} from "@/types/tipos";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useMapasStore} from "@/stores/mapas";

interface MapaOrquestracaoProps {
    codProcesso: number | string;
    sigla: string;
    codSubprocesso?: number;
}

interface EstadoMapaOrquestracao {
    carregandoInicial: Ref<boolean>;
    codigoSubprocesso: Ref<number | null>;
    unidade: Ref<Unidade | null>;
    carregamentoInicialConcluido: Ref<boolean>;
}

interface DependenciasMapaOrquestracao {
    subprocessoStore: ReturnType<typeof useSubprocessoStore>;
    mapasStore: ReturnType<typeof useMapasStore>;
}

function criarEstado() {
    return {
        carregandoInicial: ref(true),
        codigoSubprocesso: ref<number | null>(null),
        unidade: ref<Unidade | null>(null),
        carregamentoInicialConcluido: ref(false),
    };
}

function sincronizarEstadoInicialContexto(
    estado: EstadoMapaOrquestracao,
    deps: DependenciasMapaOrquestracao,
    data: ContextoEdicaoSubprocesso,
) {
    estado.unidade.value = data.unidade;
    deps.mapasStore.definirMapaCompleto(data.detalhes.codigo, data.mapa);
}

async function buscarContextoEdicao(
    props: MapaOrquestracaoProps,
    subprocessoStore: ReturnType<typeof useSubprocessoStore>,
): Promise<ContextoEdicaoSubprocesso | null> {
    if (typeof props.codSubprocesso === "number") {
        return subprocessoStore.garantirContextoEdicao(props.codSubprocesso, false);
    }

    const resultado = await subprocessoStore.garantirContextoEdicaoPorProcessoEUnidade(
        Number(props.codProcesso),
        props.sigla,
        false,
    );
    return resultado?.contexto ?? null;
}

function registrarRecargas(
    estado: EstadoMapaOrquestracao,
    dependencias: DependenciasMapaOrquestracao,
    carregarContextoInicial: () => Promise<boolean>,
) {
    onMounted(() => {
        estado.carregamentoInicialConcluido.value = true;
    });

    onActivated(async () => {
        if (!estado.carregamentoInicialConcluido.value) {
            return;
        }
        const codigo = estado.codigoSubprocesso.value;
        if (typeof codigo === "number"
            && dependencias.subprocessoStore.dadosEdicaoValidos(codigo)
            && dependencias.mapasStore.dadosMapaValidos(codigo)) {
            return;
        }
        await carregarContextoInicial();
    });
}

export function useMapaOrquestracao(props: MapaOrquestracaoProps) {
    const estado = criarEstado();
    const dependencias = {
        subprocessoStore: useSubprocessoStore(),
        mapasStore: useMapasStore(),
    };
    async function carregarContextoInicial() {
        const contexto = await buscarContextoEdicao(props, dependencias.subprocessoStore);
        if (!contexto) {
            estado.carregandoInicial.value = false;
            return false;
        }
        estado.codigoSubprocesso.value = contexto.detalhes.codigo;
        sincronizarEstadoInicialContexto(estado, dependencias, contexto);
        estado.carregandoInicial.value = false;
        return true;
    }

    if (getCurrentInstance()) {
        registrarRecargas(estado, dependencias, carregarContextoInicial);
    }

    return {
        carregandoInicial: estado.carregandoInicial,
        codigoSubprocesso: estado.codigoSubprocesso,
        unidade: estado.unidade,
        carregarContextoInicial,
        sincronizarEstadoInicialContexto: (data: ContextoEdicaoSubprocesso) =>
            sincronizarEstadoInicialContexto(estado, dependencias, data),
    };
}
