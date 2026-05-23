import {computed, ref, type Ref} from "vue";
import {useRouter} from "vue-router";
import type {VarianteAlerta} from "@/composables/useNotification";
import {useHistoricoStore} from "@/stores/historico";
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

interface Dependencias {
    codProcesso: number;
    processo: Ref<Processo | null>;
    carregarContextoCompleto: () => Promise<Processo | null | undefined>;
    limparErro: () => void;
    registrarErro: (error: unknown) => string;
    notify: (mensagem: string, variant?: VarianteAlerta) => void;
}

interface DadosAcaoBloco {
    ids: number[];
    dataLimite?: string;
}

interface DependenciasAcaoBloco {
    processo: Ref<Processo | null>;
    acaoBlocoAtual: Ref<AcaoBlocoProcesso | null>;
    processandoAcaoBloco: Ref<boolean>;
    modalBlocoRef: Ref<ModalAcaoBlocoRef | null>;
    invalidarCachesProcesso: ReturnType<typeof useInvalidacaoNavegacao>["invalidarCachesProcesso"];
    invalidarCachesSubprocesso: ReturnType<typeof useInvalidacaoNavegacao>["invalidarCachesSubprocesso"];
    toastStore: ReturnType<typeof useToastStore>;
    router: ReturnType<typeof useRouter>;
    notify: Dependencias["notify"];
    carregarContextoCompleto: Dependencias["carregarContextoCompleto"];
    registrarErro: Dependencias["registrarErro"];
}

function finalizarEstadoAcaoBloco(deps: DependenciasAcaoBloco) {
    deps.processandoAcaoBloco.value = false;
    deps.modalBlocoRef.value?.setProcessando(false);
}

function validarEstadoAcaoBloco(deps: DependenciasAcaoBloco): Processo | null {
    if (!deps.processo.value) {
        deps.modalBlocoRef.value?.setErro("Detalhes do processo não carregados.");
        finalizarEstadoAcaoBloco(deps);
        return null;
    }

    if (!deps.acaoBlocoAtual.value) {
        deps.modalBlocoRef.value?.setErro(TEXTOS.processo.ERRO_ACAO_BLOCO);
        finalizarEstadoAcaoBloco(deps);
        return null;
    }

    return deps.processo.value;
}

async function concluirAcaoBlocoSemRedirecionamento(deps: DependenciasAcaoBloco, mensagemSucesso: string) {
    deps.notify(mensagemSucesso, "success");
    deps.invalidarCachesSubprocesso({incluirPainel: false, incluirProcesso: true});
    await deps.carregarContextoCompleto();
}

async function concluirAcaoBlocoComRedirecionamento(deps: DependenciasAcaoBloco, mensagemSucesso: string) {
    deps.toastStore.setPending(mensagemSucesso);
    deps.invalidarCachesProcesso();
    deps.processo.value = null;
    await deps.router.push("/painel");
}

async function concluirAcaoBloco(deps: DependenciasAcaoBloco) {
    deps.modalBlocoRef.value?.fechar();

    const {mensagemSucesso, redirecionarPainel} = deps.acaoBlocoAtual.value as AcaoBlocoProcesso;
    if (redirecionarPainel) {
        await concluirAcaoBlocoComRedirecionamento(deps, mensagemSucesso);
        return;
    }

    await concluirAcaoBlocoSemRedirecionamento(deps, mensagemSucesso);
}

export function useProcessoAcoes({
                                     codProcesso,
                                     processo,
                                     carregarContextoCompleto,
                                     limparErro,
                                     registrarErro,
                                     notify,
                                 }: Dependencias) {
    const router = useRouter();
    const toastStore = useToastStore();
    const historicoStore = useHistoricoStore();
    const {invalidarCachesProcesso, invalidarCachesSubprocesso} = useInvalidacaoNavegacao();
    const modalBlocoRef = ref<ModalAcaoBlocoRef | null>(null);
    const mostrarModalFinalizacao = ref(false);
    const acaoBlocoAtual = ref<AcaoBlocoProcesso | null>(null);
    const processandoAcaoBloco = ref(false);
    const loadingFinalizacao = ref(false);

    const unidadesElegiveis = computed(() => {
        const elegiveis = acaoBlocoAtual.value?.unidades ?? [];
        return elegiveis.map((unidade: SubprocessoElegivel) => ({
            codigo: unidade.unidadeCodigo,
            sigla: unidade.unidadeSigla,
            nome: unidade.unidadeNome,
            situacao: formatSituacaoSubprocesso(unidade.situacao),
            ultimaDataLimite: unidade.ultimaDataLimite,
        }));
    });

    const idsElegiveis = computed(() => unidadesElegiveis.value.map((unidade) => unidade.codigo));

    function finalizarProcesso() {
        mostrarModalFinalizacao.value = true;
    }

    function abrirModalBloco(acao: AcaoBlocoProcesso) {
        acaoBlocoAtual.value = acao;
        modalBlocoRef.value?.abrir();
    }
    const dependenciasAcaoBloco = {
        processo,
        acaoBlocoAtual,
        processandoAcaoBloco,
        modalBlocoRef,
        invalidarCachesProcesso,
        invalidarCachesSubprocesso,
        toastStore,
        router,
        notify,
        carregarContextoCompleto,
        registrarErro,
    };

    async function confirmarFinalizacao() {
        if (loadingFinalizacao.value) {
            return;
        }

        loadingFinalizacao.value = true;
        try {
            limparErro();
            await processoService.finalizarProcesso(codProcesso);
            toastStore.setPending(TEXTOS_SUCESSO_PROCESSO.PROCESSO_FINALIZADO);
            invalidarCachesProcesso();
            processo.value = null;
            historicoStore.invalidar();
            await router.push("/painel");
        } catch (error) {
            notify(registrarErro(error) || TEXTOS.processo.ERRO_PADRAO, "danger");
        } finally {
            loadingFinalizacao.value = false;
        }
    }

    async function executarAcaoBloco(dados: DadosAcaoBloco) {
        try {
            limparErro();
            processandoAcaoBloco.value = true;
            modalBlocoRef.value?.setProcessando(true);

            const processoAtual = validarEstadoAcaoBloco(dependenciasAcaoBloco);
            if (!processoAtual) {
                return;
            }

            await processoService.executarAcaoEmBloco(processoAtual.codigo, {
                unidadeCodigos: dados.ids,
                acao: (acaoBlocoAtual.value as AcaoBlocoProcesso).acao,
                dataLimite: dados.dataLimite,
            });

            await concluirAcaoBloco(dependenciasAcaoBloco);
        } catch (erro) {
            modalBlocoRef.value?.setErro(registrarErro(erro) || TEXTOS.processo.ERRO_ACAO_BLOCO);
        } finally {
            finalizarEstadoAcaoBloco(dependenciasAcaoBloco);
        }
    }

    return {
        acaoBlocoAtual,
        abrirModalBloco,
        confirmarFinalizacao,
        executarAcaoBloco,
        finalizarProcesso,
        idsElegiveis,
        loadingFinalizacao,
        modalBlocoRef,
        mostrarModalFinalizacao,
        obterIdBotaoAcao: obterIdBotaoAcaoProcesso,
        obterTestIdBotaoAcao: obterTestIdBotaoAcaoProcesso,
        processandoAcaoBloco,
        unidadesElegiveis,
    };
}
