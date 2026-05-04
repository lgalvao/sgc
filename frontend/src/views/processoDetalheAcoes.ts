import {computed, ref, type Ref} from "vue";
import {useRouter} from "vue-router";
import type {VarianteAlerta} from "@/composables/useNotification";
import {useHistoricoStore} from "@/stores/historico";
import {useToastStore} from "@/stores/toast";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import type {AcaoBlocoProcesso, Processo, SubprocessoElegivel} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
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
    clearError: () => void;
    registrarErro: (error: unknown) => string;
    notify: (mensagem: string, variant?: VarianteAlerta) => void;
}

export function useProcessoAcoes({
                                     codProcesso,
                                     processo,
                                     carregarContextoCompleto,
                                     clearError,
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

    async function confirmarFinalizacao() {
        if (loadingFinalizacao.value) {
            return;
        }

        loadingFinalizacao.value = true;
        try {
            clearError();
            await processoService.finalizarProcesso(codProcesso);
            toastStore.setPending(TEXTOS.sucesso.PROCESSO_FINALIZADO);
            invalidarCachesProcesso();
            historicoStore.invalidar();
            await router.push("/painel");
        } catch (error) {
            notify(registrarErro(error) || TEXTOS.processo.ERRO_PADRAO, "danger");
        } finally {
            loadingFinalizacao.value = false;
        }
    }

    async function executarAcaoBloco(dados: { ids: number[], dataLimite?: string }) {
        try {
            clearError();
            processandoAcaoBloco.value = true;
            modalBlocoRef.value?.setProcessando(true);

            if (!processo.value) {
                modalBlocoRef.value?.setErro("Detalhes do processo não carregados.");
                processandoAcaoBloco.value = false;
                return;
            }
            if (!acaoBlocoAtual.value) {
                modalBlocoRef.value?.setErro(TEXTOS.processo.ERRO_ACAO_BLOCO);
                processandoAcaoBloco.value = false;
                return;
            }

            await processoService.executarAcaoEmBloco(processo.value.codigo, {
                unidadeCodigos: dados.ids,
                acao: acaoBlocoAtual.value.acao,
                dataLimite: dados.dataLimite,
            });

            modalBlocoRef.value?.fechar();
            const {mensagemSucesso, redirecionarPainel} = acaoBlocoAtual.value;
            if (redirecionarPainel) {
                toastStore.setPending(mensagemSucesso);
                invalidarCachesProcesso();
                await router.push("/painel");
                return;
            }

            notify(mensagemSucesso, "success");
            invalidarCachesSubprocesso({incluirPainel: false, incluirProcesso: true});
            await carregarContextoCompleto();
        } catch (error) {
            modalBlocoRef.value?.setErro(registrarErro(error) || TEXTOS.processo.ERRO_ACAO_BLOCO);
            modalBlocoRef.value?.setProcessando(false);
        } finally {
            processandoAcaoBloco.value = false;
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
