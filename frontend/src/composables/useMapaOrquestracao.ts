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

export function useMapaOrquestracao(
    props: MapaOrquestracaoProps
) {
    const subprocessoStore = useSubprocessoStore();
    const mapasStore = useMapasStore();

    const carregandoInicial = ref(true);
    const codigoSubprocesso = ref<number | null>(null);
    const unidade = ref<Unidade | null>(null);
    const carregamentoInicialConcluido = ref(false);

    function sincronizarEstadoInicialContexto(data: ContextoEdicaoSubprocesso) {
        unidade.value = data.unidade;
        mapasStore.definirMapaCompleto(data.detalhes.codigo, data.mapa);
    }

    async function carregarContextoInicial() {
        try {
            let contexto: ContextoEdicaoSubprocesso | null = null;

            if (typeof props.codSubprocesso === "number") {
                contexto = await subprocessoStore.garantirContextoEdicao(props.codSubprocesso, false);
            } else {
                const resultado = await subprocessoStore.garantirContextoEdicaoPorProcessoEUnidade(
                    Number(props.codProcesso),
                    props.sigla,
                    false,
                );
                if (resultado) {
                    contexto = resultado.contexto;
                }
            }

            if (!contexto) {
                logger.error("Falha grave ao resolver subprocesso para o mapa.");
                return false;
            }

            codigoSubprocesso.value = contexto.detalhes.codigo;
            sincronizarEstadoInicialContexto(contexto);

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
        sincronizarEstadoInicialContexto
    };
}
