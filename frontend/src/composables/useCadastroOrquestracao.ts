import {getCurrentInstance, onActivated, onMounted, ref, type Ref} from "vue";
import type {Atividade, ContextoCadastroAtividadesSubprocesso, RespostaLocalCadastro, Unidade} from "@/types/tipos";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import {calcularAssinaturaCadastro} from "@/utils/formatters";
import logger from "@/utils/logger";
import {useCacheMapa} from "@/composables/useMapaQuery";

interface CadastroOrquestracaoProps {
    codProcesso: number | string;
    sigla: string;
    codSubprocesso?: number;
}

function erroIntegracaoFoiCancelado(subprocessoStore: ReturnType<typeof useSubprocessoStore>): boolean {
    return subprocessoStore.erroIntegracaoContexto?.codigo === "REQUEST_CANCELADA";
}

async function buscarContextoInicial(
    props: CadastroOrquestracaoProps,
    subprocessoStore: ReturnType<typeof useSubprocessoStore>,
) {
    return typeof props.codSubprocesso === "number"
        ? await subprocessoStore.obterContextoCadastroAtividades(props.codSubprocesso)
        : await subprocessoStore.obterContextoCadastroAtividadesPorProcessoEUnidade(
            Number(props.codProcesso),
            props.sigla,
        );
}

async function recarregarContextoInicial(
    props: CadastroOrquestracaoProps,
    subprocessoStore: ReturnType<typeof useSubprocessoStore>,
) {
    return typeof props.codSubprocesso === "number"
        ? await subprocessoStore.recarregarContextoCadastroAtividades(props.codSubprocesso)
        : await subprocessoStore.recarregarContextoCadastroAtividadesPorProcessoEUnidade(
            Number(props.codProcesso),
            props.sigla,
        );
}

export function useCadastroOrquestracao(props: CadastroOrquestracaoProps, atividades: Ref<Atividade[]>) {
    const subprocessoStore = useSubprocessoStore();
    const cacheMapa = useCacheMapa();
    const {atualizarFluxoSubprocessoEPainel} = useInvalidacaoNavegacao();

    const carregandoInicial = ref(true);
    const codigoSubprocesso = ref<number | null>(null);
    const atividadesSnapshotInicial = ref<string | null>(null);
    const unidade = ref<Unidade | null>(null);
    const codMapa = ref<number | null>(null);
    const carregamentoInicialConcluido = ref(false);

    function aplicarRespostaLocal(response: RespostaLocalCadastro) {
        atividades.value = response.atividadesAtualizadas;
        subprocessoStore.atualizarStatusLocal({
            ...response.subprocesso,
            permissoes: response.permissoes
        });
        cacheMapa.invalidarMapa(response.subprocesso.codigo);
        subprocessoStore.marcarContextoEdicaoParaAtualizacao(response.subprocesso.codigo);
    }

    function sincronizarEstadoInicial(data: ContextoCadastroAtividadesSubprocesso) {
        aplicarRespostaLocal({
            subprocesso: {
                codigo: data.detalhes.codigo,
                situacao: data.detalhes.situacao,
            },
            permissoes: data.detalhes.permissoes,
            atividadesAtualizadas: data.atividadesDisponiveis,
        });

        atividadesSnapshotInicial.value =
            data.assinaturaCadastroReferencia ?? calcularAssinaturaCadastro(data.atividadesDisponiveis);
        unidade.value = data.unidade;
        codMapa.value = data.mapa.codigo;
    }

    async function carregarContextoInicial() {
        try {
            let data = await buscarContextoInicial(props, subprocessoStore);

            if (!data && erroIntegracaoFoiCancelado(subprocessoStore)) {
                data = await recarregarContextoInicial(props, subprocessoStore);
            }

            if (!data) {
                if (erroIntegracaoFoiCancelado(subprocessoStore)) {
                    return false;
                }
                logger.error("ERRO: Subprocesso não encontrado!");
                return false;
            }

            codigoSubprocesso.value = data.detalhes.codigo;
            sincronizarEstadoInicial(data);
            return true;
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
            if (typeof codigo === "number" && subprocessoStore.dadosCadastroValidos(codigo)) {
                return;
            }

            await carregarContextoInicial();
        });
    }

    return {
        carregandoInicial,
        codigoSubprocesso,
        atividadesSnapshotInicial,
        unidade,
        codMapa,
        carregarContextoInicial,
        processarRespostaLocal: (response: RespostaLocalCadastro) => {
            aplicarRespostaLocal(response);
            atualizarFluxoSubprocessoEPainel();
        },
        sincronizarEstadoInicialContexto: sincronizarEstadoInicial,
    };
}
