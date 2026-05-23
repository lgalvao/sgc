import {getCurrentInstance, onActivated, onMounted, ref, type Ref} from "vue";
import type {Atividade, ContextoCadastroAtividadesSubprocesso, RespostaLocalCadastro, Unidade} from "@/types/tipos";
import {useMapasStore} from "@/stores/mapas";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import {calcularAssinaturaCadastro} from "@/utils/formatters";
import logger from "@/utils/logger";

interface CadastroOrquestracaoProps {
    codProcesso: number | string;
    sigla: string;
    codSubprocesso?: number;
}

interface EstadoCadastroOrquestracao {
    atividades: Ref<Atividade[]>;
    carregandoInicial: Ref<boolean>;
    codigoSubprocesso: Ref<number | null>;
    atividadesSnapshotInicial: Ref<string | null>;
    unidade: Ref<Unidade | null>;
    codMapa: Ref<number | null>;
    carregamentoInicialConcluido: Ref<boolean>;
}

interface DependenciasCadastroOrquestracao {
    subprocessoStore: ReturnType<typeof useSubprocessoStore>;
    mapasStore: ReturnType<typeof useMapasStore>;
    invalidarCachesSubprocesso: ReturnType<typeof useInvalidacaoNavegacao>["invalidarCachesSubprocesso"];
}

function aplicarEstadoContexto(
    estado: EstadoCadastroOrquestracao,
    deps: DependenciasCadastroOrquestracao,
    response: RespostaLocalCadastro,
) {
    estado.atividades.value = response.atividadesAtualizadas;
    deps.subprocessoStore.atualizarStatusLocal({
        ...response.subprocesso,
        permissoes: response.permissoes
    });
    deps.mapasStore.invalidar(response.subprocesso.codigo);
    deps.subprocessoStore.invalidarContextoEdicao(response.subprocesso.codigo);
}

function processarRespostaLocal(
    estado: EstadoCadastroOrquestracao,
    deps: DependenciasCadastroOrquestracao,
    response: RespostaLocalCadastro,
) {
    aplicarEstadoContexto(estado, deps, response);
    deps.invalidarCachesSubprocesso({incluirPainel: true});
}

function sincronizarEstadoInicialContexto(
    estado: EstadoCadastroOrquestracao,
    deps: DependenciasCadastroOrquestracao,
    data: ContextoCadastroAtividadesSubprocesso,
) {
    aplicarEstadoContexto(estado, deps, {
        subprocesso: {
            codigo: data.detalhes.codigo,
            situacao: data.detalhes.situacao,
        },
        permissoes: data.detalhes.permissoes,
        atividadesAtualizadas: data.atividadesDisponiveis,
    });

    estado.atividadesSnapshotInicial.value = data.assinaturaCadastroReferencia ?? calcularAssinaturaCadastro(data.atividadesDisponiveis);
    estado.unidade.value = data.unidade;
    estado.codMapa.value = data.mapa.codigo;
}

function erroIntegracaoFoiCancelado(subprocessoStore: ReturnType<typeof useSubprocessoStore>): boolean {
    return subprocessoStore.erroIntegracaoContexto?.codigo === "REQUEST_CANCELADA";
}

async function buscarContextoInicial(
    props: CadastroOrquestracaoProps,
    subprocessoStore: ReturnType<typeof useSubprocessoStore>,
    limparAntes = false,
) {
    return typeof props.codSubprocesso === "number"
        ? await subprocessoStore.garantirContextoCadastroAtividades(props.codSubprocesso, limparAntes)
        : await subprocessoStore.garantirContextoCadastroAtividadesPorProcessoEUnidade(
            Number(props.codProcesso),
            props.sigla,
            limparAntes,
        );
}

export function useCadastroOrquestracao(props: CadastroOrquestracaoProps, atividades: Ref<Atividade[]>) {
    const subprocessoStore = useSubprocessoStore();
    const mapasStore = useMapasStore();
    const {invalidarCachesSubprocesso} = useInvalidacaoNavegacao();

    const carregandoInicial = ref(true);
    const codigoSubprocesso = ref<number | null>(null);
    const atividadesSnapshotInicial = ref<string | null>(null);
    const unidade = ref<Unidade | null>(null);
    const codMapa = ref<number | null>(null);
    const carregamentoInicialConcluido = ref(false);
    const estado = {
        atividades,
        carregandoInicial,
        codigoSubprocesso,
        atividadesSnapshotInicial,
        unidade,
        codMapa,
        carregamentoInicialConcluido,
    };
    const dependencias = {
        subprocessoStore,
        mapasStore,
        invalidarCachesSubprocesso,
    };

    async function carregarContextoInicial() {
        try {
            let data = await buscarContextoInicial(props, subprocessoStore, false);

            if (!data && erroIntegracaoFoiCancelado(subprocessoStore)) {
                data = await buscarContextoInicial(props, subprocessoStore, true);
            }

            if (!data) {
                if (erroIntegracaoFoiCancelado(subprocessoStore)) {
                    return false;
                }
                logger.error("ERRO: Subprocesso não encontrado!");
                return false;
            }

            codigoSubprocesso.value = data.detalhes.codigo;
            sincronizarEstadoInicialContexto(estado, dependencias, data);
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
        processarRespostaLocal: (response: RespostaLocalCadastro) => processarRespostaLocal(estado, dependencias, response),
        sincronizarEstadoInicialContexto: (data: ContextoCadastroAtividadesSubprocesso) =>
            sincronizarEstadoInicialContexto(estado, dependencias, data),
    };
}
