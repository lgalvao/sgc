import {ref, type Ref} from "vue";
import type {ContextoEdicaoSubprocesso, MapaVisualizacao, Unidade} from "@/types/tipos";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useMapasStore} from "@/stores/mapas";
import {diagnosticarCarregamentoContextoSubprocessoInicial} from "@/composables/useContextoSubprocesso";
import {obterMapaVisualizacao} from "@/services/subprocessoService";
import logger from "@/utils/logger";

interface MapaOrquestracaoProps {
    codProcesso: number | string;
    sigla: string;
    codSubprocesso?: number;
}

export function useMapaOrquestracao(
    props: MapaOrquestracaoProps,
    mapaSomenteLeitura: Ref<MapaVisualizacao | null>
) {
    const subprocessoStore = useSubprocessoStore();
    const mapasStore = useMapasStore();

    const carregandoInicial = ref(true);
    const codigoSubprocesso = ref<number | null>(null);
    const unidade = ref<Unidade | null>(null);

    function sincronizarEstadoInicialContexto(data: ContextoEdicaoSubprocesso) {
        unidade.value = data.unidade;
        mapasStore.definirMapaCompleto(data.detalhes.codigo, data.mapa);
    }

    async function carregarDadosMapaSomenteLeitura(codigo: number) {
        try {
            mapaSomenteLeitura.value = await obterMapaVisualizacao(codigo);
        } catch (error) {
            logger.error("Erro ao carregar visualização do mapa", error);
        }
    }

    async function carregarContextoInicial(podeEditarMapa: Ref<boolean>) {
        try {
            let contexto: ContextoEdicaoSubprocesso | null = null;

            if (typeof props.codSubprocesso === "number") {
                contexto = await subprocessoStore.garantirContextoEdicao(props.codSubprocesso, false);
            } else {
                const diagnostico = await diagnosticarCarregamentoContextoSubprocessoInicial({
                    codProcesso: Number(props.codProcesso),
                    siglaUnidade: props.sigla,
                    store: subprocessoStore,
                });

                if (diagnostico.tipo === 'sucesso') {
                    contexto = diagnostico.resultado.contexto;
                }
            }

            if (!contexto) {
                logger.error("Falha grave ao resolver subprocesso para o mapa.");
                return false;
            }

            codigoSubprocesso.value = contexto.detalhes.codigo;
            sincronizarEstadoInicialContexto(contexto);

            // Lógica de visualização vs edição
            if (!podeEditarMapa.value) {
                await carregarDadosMapaSomenteLeitura(contexto.detalhes.codigo);
            }

            return true;
        } catch (e) {
            logger.error("Erro ao carregar contexto inicial do mapa", e);
            return false;
        } finally {
            carregandoInicial.value = false;
        }
    }

    return {
        carregandoInicial,
        codigoSubprocesso,
        unidade,
        carregarContextoInicial,
        sincronizarEstadoInicialContexto,
        carregarDadosMapaSomenteLeitura
    };
}
