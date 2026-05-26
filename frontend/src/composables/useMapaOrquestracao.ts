import {getCurrentInstance, onActivated, onMounted, ref} from "vue";
import type {ContextoEdicaoSubprocesso, Unidade} from "@/types/tipos";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useMapasStore} from "@/stores/mapas";

interface MapaOrquestracaoProps {
    codProcesso: number | string;
    sigla: string;
    codSubprocesso?: number;
}

function criarEstado() {
    return {
        carregandoInicial: ref(true),
        codigoSubprocesso: ref<number | null>(null),
        unidade: ref<Unidade | null>(null),
        carregamentoInicialConcluido: ref(false),
    };
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

export function useMapaOrquestracao(props: MapaOrquestracaoProps) {
    const {carregandoInicial, codigoSubprocesso, unidade, carregamentoInicialConcluido} = criarEstado();
    const subprocessoStore = useSubprocessoStore();
    const mapasStore = useMapasStore();

    function sincronizarEstadoInicialContexto(data: ContextoEdicaoSubprocesso) {
        unidade.value = data.unidade;
        mapasStore.definirMapaCompleto(data.detalhes.codigo, data.mapa);
    }

    async function carregarContextoInicial() {
        const contexto = await buscarContextoEdicao(props, subprocessoStore);
        if (!contexto) {
            carregandoInicial.value = false;
            return false;
        }
        codigoSubprocesso.value = contexto.detalhes.codigo;
        sincronizarEstadoInicialContexto(contexto);
        carregandoInicial.value = false;
        return true;
    }

    if (getCurrentInstance()) {
        onMounted(() => {
            carregamentoInicialConcluido.value = true;
        });

        onActivated(async () => {
            if (!carregamentoInicialConcluido.value) {
                return;
            }
            const codigo = codigoSubprocesso.value;
            if (typeof codigo === "number"
                && subprocessoStore.dadosEdicaoValidos(codigo)
                && mapasStore.dadosMapaValidos(codigo)) {
                return;
            }
            await carregarContextoInicial();
        });
    }

    return {
        carregandoInicial,
        codigoSubprocesso,
        unidade,
        carregarContextoInicial,
        sincronizarEstadoInicialContexto,
    };
}
