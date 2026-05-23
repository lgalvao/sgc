import type {Ref} from "vue";
import {getCurrentInstance, onActivated, onMounted, ref} from "vue";
import type {ContextoEdicaoSubprocesso, Unidade} from "@/types/tipos";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useMapasStore} from "@/stores/mapas";
import logger from "@/utils/logger";

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

export function useMapaOrquestracao(
    props: MapaOrquestracaoProps
) {
    const subprocessoStore = useSubprocessoStore();
    const mapasStore = useMapasStore();

    const carregandoInicial = ref(true);
    const codigoSubprocesso = ref<number | null>(null);
    const unidade = ref<Unidade | null>(null);
    const carregamentoInicialConcluido = ref(false);
    const estado = {carregandoInicial, codigoSubprocesso, unidade, carregamentoInicialConcluido};
    const dependencias = {subprocessoStore, mapasStore};

    async function carregarContextoInicial() {
        try {
            const contexto = await buscarContextoEdicao(props, subprocessoStore);

            if (!contexto) {
                logger.error("Falha grave ao resolver subprocesso para o mapa.");
                return false;
            }

            codigoSubprocesso.value = contexto.detalhes.codigo;
            sincronizarEstadoInicialContexto(estado, dependencias, contexto);

            return true;
        } catch (e) {
            logger.error("Erro ao carregar contexto inicial do mapa", e);
            return false;
        } finally {
            carregandoInicial.value = false;
        }
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
        sincronizarEstadoInicialContexto: (data: ContextoEdicaoSubprocesso) =>
            sincronizarEstadoInicialContexto(estado, dependencias, data),
    };
}
