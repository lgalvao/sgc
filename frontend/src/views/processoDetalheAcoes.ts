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

function criarEstado() {
    return {
        modalBlocoRef: ref<ModalAcaoBlocoRef | null>(null),
        mostrarModalFinalizacao: ref(false),
        acaoBlocoAtual: ref<AcaoBlocoProcesso | null>(null),
        processandoAcaoBloco: ref(false),
        loadingFinalizacao: ref(false),
    };
}

export function useProcessoAcoes(dependencias: Dependencias) {
    const router = useRouter();
    const queryCache = useQueryCache();
    const toastStore = useToastStore();
    const {atualizarFluxoProcesso, atualizarFluxoSubprocessoEProcesso} = useInvalidacaoNavegacao();
    const estado = criarEstado();

    async function confirmarFinalizacao() {
        if (estado.loadingFinalizacao.value) {
            return;
        }

        estado.loadingFinalizacao.value = true;
        try {
            dependencias.limparErro();
            await processoService.finalizarProcesso(dependencias.codProcesso);
            toastStore.setPending(TEXTOS_SUCESSO_PROCESSO.PROCESSO_FINALIZADO);
            atualizarFluxoProcesso();
            dependencias.processo.value = null;
            await queryCache.invalidateQueries({key: CHAVE_QUERY_HISTORICO, exact: true});
            await router.push("/painel");
        } catch (error) {
            dependencias.notify(dependencias.registrarErro(error) || TEXTOS.processo.ERRO_PADRAO, "danger");
        } finally {
            estado.loadingFinalizacao.value = false;
        }
    }

    async function concluirAcaoBloco(acao: AcaoBlocoProcesso) {
        estado.modalBlocoRef.value?.fechar();

        if (acao.redirecionarPainel) {
            toastStore.setPending(acao.mensagemSucesso);
            atualizarFluxoProcesso();
            dependencias.processo.value = null;
            await router.push("/painel");
            return;
        }

        dependencias.notify(acao.mensagemSucesso, "success");
        atualizarFluxoSubprocessoEProcesso();
        await dependencias.carregarContextoCompleto();
    }

    async function executarAcaoBloco(dados: DadosAcaoBloco) {
        const processo = dependencias.processo.value;
        if (!processo) {
            estado.modalBlocoRef.value?.setErro("Detalhes do processo não carregados.");
            return;
        }
        const acao = estado.acaoBlocoAtual.value;
        if (!acao) {
            estado.modalBlocoRef.value?.setErro(TEXTOS.processo.ERRO_ACAO_BLOCO);
            return;
        }

        dependencias.limparErro();
        estado.processandoAcaoBloco.value = true;
        estado.modalBlocoRef.value?.setProcessando(true);
        try {
            await processoService.executarAcaoEmBloco(processo.codigo, {
                unidadeCodigos: dados.ids,
                acao: acao.acao,
                dataLimite: dados.dataLimite,
            });
            await concluirAcaoBloco(acao);
        } catch (error) {
            estado.modalBlocoRef.value?.setErro(dependencias.registrarErro(error) || TEXTOS.processo.ERRO_ACAO_BLOCO);
        } finally {
            estado.processandoAcaoBloco.value = false;
            estado.modalBlocoRef.value?.setProcessando(false);
        }
    }

    return {
        acaoBlocoAtual: estado.acaoBlocoAtual,
        abrirModalBloco: (acao: AcaoBlocoProcesso) => {
            estado.acaoBlocoAtual.value = acao;
            estado.modalBlocoRef.value?.abrir();
        },
        confirmarFinalizacao,
        executarAcaoBloco,
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
