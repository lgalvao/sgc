import {ref, type Ref} from "vue";
import type {Atividade, ContextoCadastroAtividadesSubprocesso, RespostaLocalCadastro, Unidade} from "@/types/tipos";
import {useMapasStore} from "@/stores/mapas";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {calcularAssinaturaCadastro} from "@/utils/formatters";
import logger from "@/utils/logger";

interface CadastroOrquestracaoProps {
    codProcesso: number | string;
    sigla: string;
    codSubprocesso?: number;
}

export function useCadastroOrquestracao(props: CadastroOrquestracaoProps, atividades: Ref<Atividade[]>) {
    const subprocessoStore = useSubprocessoStore();
    const mapasStore = useMapasStore();

    const carregandoInicial = ref(true);
    const codigoSubprocesso = ref<number | null>(null);
    const atividadesSnapshotInicial = ref<string | null>(null);
    const unidade = ref<Unidade | null>(null);
    const codMapa = ref<number | null>(null);

    function processarRespostaLocal(response: RespostaLocalCadastro) {
        atividades.value = response.atividadesAtualizadas;
        subprocessoStore.atualizarStatusLocal({
            ...response.subprocesso,
            permissoes: response.permissoes
        });
        mapasStore.invalidar(response.subprocesso.codigo);
        subprocessoStore.invalidarContextoEdicao(response.subprocesso.codigo);
    }

    function sincronizarEstadoInicialContexto(data: ContextoCadastroAtividadesSubprocesso) {
        processarRespostaLocal({
            subprocesso: {
                codigo: data.detalhes.codigo,
                situacao: data.detalhes.situacao,
            },
            permissoes: data.detalhes.permissoes,
            atividadesAtualizadas: data.atividadesDisponiveis,
        });

        atividadesSnapshotInicial.value = data.assinaturaCadastroReferencia ?? calcularAssinaturaCadastro(data.atividadesDisponiveis);
        unidade.value = data.unidade;
        codMapa.value = data.mapa.codigo;
    }

    function erroIntegracaoFoiCancelado(): boolean {
        return subprocessoStore.erroIntegracaoContexto?.codigo === "REQUEST_CANCELADA";
    }

    async function buscarContextoInicial(limparAntes = false) {
        return typeof props.codSubprocesso === "number"
            ? await subprocessoStore.garantirContextoCadastroAtividades(props.codSubprocesso, limparAntes)
            : await subprocessoStore.garantirContextoCadastroAtividadesPorProcessoEUnidade(
                Number(props.codProcesso),
                props.sigla,
                limparAntes,
            );
    }

    async function carregarContextoInicial() {
        try {
            let data = await buscarContextoInicial(false);

            if (!data && erroIntegracaoFoiCancelado()) {
                data = await buscarContextoInicial(true);
            }

            if (!data) {
                if (erroIntegracaoFoiCancelado()) {
                    return false;
                }
                logger.error("ERRO: Subprocesso não encontrado!");
                return false;
            }

            codigoSubprocesso.value = data.detalhes.codigo;
            sincronizarEstadoInicialContexto(data);
            return true;
        } finally {
            carregandoInicial.value = false;
        }
    }

    return {
        carregandoInicial,
        codigoSubprocesso,
        atividadesSnapshotInicial,
        unidade,
        codMapa,
        carregarContextoInicial,
        processarRespostaLocal,
        sincronizarEstadoInicialContexto
    };
}
