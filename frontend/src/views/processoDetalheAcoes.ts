import {computed, ref, type Ref} from "vue";
import {useQueryCache} from "@pinia/colada";
import {useRouter} from "vue-router";
import type {VarianteAlerta} from "@/composables/useNotification";
import {CHAVE_QUERY_HISTORICO} from "@/composables/useHistoricoQuery";
import {useToastStore} from "@/stores/toast";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import type {AcaoBlocoProcesso, Processo, SubprocessoElegivel} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
import {TEXTOS_SUCESSO_PROCESSO} from "@/constants/textos-processo";
import {formatSituacaoSubprocesso} from "@/utils/formatters";
import * as processoService from "@/services/processo";
import {obterIdBotaoAcaoProcesso, obterTestIdBotaoAcaoProcesso} from "@/components/processo/processoAcoes";

type ModalAcaoBlocoRef = {
    abrir: () => void;
    fechar: () => void;
    setProcessando: (valor: boolean) => void;
    setErro: (mensagem: string) => void;
};

type Dependencias = {
    codProcesso: number;
    processo: Ref<Processo | null>;
    carregarContextoCompleto: () => Promise<Processo | null | undefined>;
    limparErro: () => void;
    registrarErro: (error: unknown) => string;
    notify: (mensagem: string, variant?: VarianteAlerta) => void;
};

type DadosAcaoBloco = {
    ids: number[];
    dataLimite?: string;
};

type EstadoProcessoAcoes = ReturnType<typeof criarEstado>;

type ContextoProcessoAcoes = {
    dependencias: Dependencias;
    estado: EstadoProcessoAcoes;
    router: ReturnType<typeof useRouter>;
    queryCache: ReturnType<typeof useQueryCache>;
    toastStore: ReturnType<typeof useToastStore>;
    invalidarCachesProcesso: ReturnType<typeof useInvalidacaoNavegacao>["invalidarCachesProcesso"];
    invalidarCachesSubprocesso: ReturnType<typeof useInvalidacaoNavegacao>["invalidarCachesSubprocesso"];
};

function criarEstado() {
    return {
        modalBlocoRef: ref<ModalAcaoBlocoRef | null>(null),
        mostrarModalFinalizacao: ref(false),
        acaoBlocoAtual: ref<AcaoBlocoProcesso | null>(null),
        processandoAcaoBloco: ref(false),
        loadingFinalizacao: ref(false),
    };
}

async function confirmarFinalizacao(contexto: ContextoProcessoAcoes) {
    if (contexto.estado.loadingFinalizacao.value) {
        return;
    }

    contexto.estado.loadingFinalizacao.value = true;
    try {
        contexto.dependencias.limparErro();
        await processoService.finalizarProcesso(contexto.dependencias.codProcesso);
        contexto.toastStore.setPending(TEXTOS_SUCESSO_PROCESSO.PROCESSO_FINALIZADO);
        contexto.invalidarCachesProcesso();
        contexto.dependencias.processo.value = null;
        await contexto.queryCache.invalidateQueries({key: CHAVE_QUERY_HISTORICO, exact: true});
        await contexto.router.push("/painel");
    } catch (error) {
        contexto.dependencias.notify(contexto.dependencias.registrarErro(error) || TEXTOS.processo.ERRO_PADRAO, "danger");
    } finally {
        contexto.estado.loadingFinalizacao.value = false;
    }
}

async function concluirAcaoBloco(contexto: ContextoProcessoAcoes, acao: AcaoBlocoProcesso) {
    contexto.estado.modalBlocoRef.value?.fechar();

    if (acao.redirecionarPainel) {
        contexto.toastStore.setPending(acao.mensagemSucesso);
        contexto.invalidarCachesProcesso();
        contexto.dependencias.processo.value = null;
        await contexto.router.push("/painel");
        return;
    }

    contexto.dependencias.notify(acao.mensagemSucesso, "success");
    contexto.invalidarCachesSubprocesso({incluirPainel: false, incluirProcesso: true});
    await contexto.dependencias.carregarContextoCompleto();
}

async function executarAcaoBloco(contexto: ContextoProcessoAcoes, dados: DadosAcaoBloco) {
    const processo = contexto.dependencias.processo.value;
    if (!processo) {
        contexto.estado.modalBlocoRef.value?.setErro("Detalhes do processo não carregados.");
        return;
    }
    const acao = contexto.estado.acaoBlocoAtual.value;
    if (!acao) {
        contexto.estado.modalBlocoRef.value?.setErro(TEXTOS.processo.ERRO_ACAO_BLOCO);
        return;
    }

    contexto.dependencias.limparErro();
    contexto.estado.processandoAcaoBloco.value = true;
    contexto.estado.modalBlocoRef.value?.setProcessando(true);
    try {
        await processoService.executarAcaoEmBloco(processo.codigo, {
            unidadeCodigos: dados.ids,
            acao: acao.acao,
            dataLimite: dados.dataLimite,
        });
        await concluirAcaoBloco(contexto, acao);
    } catch (error) {
        contexto.estado.modalBlocoRef.value?.setErro(contexto.dependencias.registrarErro(error) || TEXTOS.processo.ERRO_ACAO_BLOCO);
    } finally {
        contexto.estado.processandoAcaoBloco.value = false;
        contexto.estado.modalBlocoRef.value?.setProcessando(false);
    }
}

export function useProcessoAcoes(dependencias: Dependencias) {
    const router = useRouter();
    const queryCache = useQueryCache();
    const toastStore = useToastStore();
    const {invalidarCachesProcesso, invalidarCachesSubprocesso} = useInvalidacaoNavegacao();
    const estado = criarEstado();
    const contexto = {
        dependencias,
        estado,
        router,
        queryCache,
        toastStore,
        invalidarCachesProcesso,
        invalidarCachesSubprocesso,
    };

    return {
        acaoBlocoAtual: estado.acaoBlocoAtual,
        abrirModalBloco: (acao: AcaoBlocoProcesso) => {
            estado.acaoBlocoAtual.value = acao;
            estado.modalBlocoRef.value?.abrir();
        },
        confirmarFinalizacao: () => confirmarFinalizacao(contexto),
        executarAcaoBloco: (dados: DadosAcaoBloco) => executarAcaoBloco(contexto, dados),
        finalizarProcesso: () => {
            estado.mostrarModalFinalizacao.value = true;
        },
        idsElegiveis: computed(() => {
            const unidades = estado.acaoBlocoAtual.value?.unidades;
            return unidades ? unidades.map((unidade) => unidade.unidadeCodigo) : [];
        }),
        loadingFinalizacao: estado.loadingFinalizacao,
        modalBlocoRef: estado.modalBlocoRef,
        mostrarModalFinalizacao: estado.mostrarModalFinalizacao,
        obterIdBotaoAcao: obterIdBotaoAcaoProcesso,
        obterTestIdBotaoAcao: obterTestIdBotaoAcaoProcesso,
        processandoAcaoBloco: estado.processandoAcaoBloco,
        unidadesElegiveis: computed(() => {
            const elegiveis = estado.acaoBlocoAtual.value?.unidades;
            if (!elegiveis) {
                return [];
            }
            return elegiveis.map((unidade: SubprocessoElegivel) => ({
                codigo: unidade.unidadeCodigo,
                sigla: unidade.unidadeSigla,
                nome: unidade.unidadeNome,
                situacao: formatSituacaoSubprocesso(unidade.situacao),
                ultimaDataLimite: unidade.ultimaDataLimite,
            }));
        }),
    };
}
