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

    function finalizarEstadoAcaoBloco() {
        processandoAcaoBloco.value = false;
        modalBlocoRef.value?.setProcessando(false);
    }

    function validarEstadoAcaoBloco(): Processo | null {
        if (!processo.value) {
            modalBlocoRef.value?.setErro("Detalhes do processo não carregados.");
            finalizarEstadoAcaoBloco();
            return null;
        }

        if (!acaoBlocoAtual.value) {
            modalBlocoRef.value?.setErro(TEXTOS.processo.ERRO_ACAO_BLOCO);
            finalizarEstadoAcaoBloco();
            return null;
        }

        return processo.value;
    }

    async function concluirAcaoBlocoSemRedirecionamento(mensagemSucesso: string) {
        notify(mensagemSucesso, "success");
        invalidarCachesSubprocesso({incluirPainel: false, incluirProcesso: true});
        await carregarContextoCompleto();
    }

    async function concluirAcaoBlocoComRedirecionamento(mensagemSucesso: string) {
        toastStore.setPending(mensagemSucesso);
        invalidarCachesProcesso();
        processo.value = null;
        await router.push("/painel");
    }

    async function concluirAcaoBloco() {
        modalBlocoRef.value?.fechar();

        const {mensagemSucesso, redirecionarPainel} = acaoBlocoAtual.value as AcaoBlocoProcesso;
        if (redirecionarPainel) {
            await concluirAcaoBlocoComRedirecionamento(mensagemSucesso);
            return;
        }

        await concluirAcaoBlocoSemRedirecionamento(mensagemSucesso);
    }

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

            const processoAtual = validarEstadoAcaoBloco();
            if (!processoAtual) {
                return;
            }

            await processoService.executarAcaoEmBloco(processoAtual.codigo, {
                unidadeCodigos: dados.ids,
                acao: (acaoBlocoAtual.value as AcaoBlocoProcesso).acao,
                dataLimite: dados.dataLimite,
            });

            await concluirAcaoBloco();
        } catch (erro) {
            modalBlocoRef.value?.setErro(registrarErro(erro) || TEXTOS.processo.ERRO_ACAO_BLOCO);
        } finally {
            finalizarEstadoAcaoBloco();
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
