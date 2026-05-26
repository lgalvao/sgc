import {computed, ref} from "vue";
import {useRouter} from "vue-router";
import * as processoService from "@/services/processo";
import {TEXTOS} from "@/constants/textos";
import {formatSituacaoSubprocesso} from "@/utils/formatters";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import type {AcaoBlocoProcesso, SubprocessoElegivel} from "@/types/tipos";
import {obterIdBotaoAcaoProcesso, obterTestIdBotaoAcaoProcesso} from "@/components/processo/processoAcoes";
import type {DadosAcaoBloco, DependenciasProcessoAcoes, ModalAcaoBlocoRef} from "@/views/processoDetalheTipos";
import {useToastStore} from "@/stores/toast";

export function useAcoesBlocoProcesso(dependencias: DependenciasProcessoAcoes) {
    const router = useRouter();
    const toastStore = useToastStore();
    const {atualizarFluxoProcesso, atualizarFluxoSubprocessoEProcesso} = useInvalidacaoNavegacao();
    const modalBlocoRef = ref<ModalAcaoBlocoRef | null>(null);
    const acaoBlocoAtual = ref<AcaoBlocoProcesso | null>(null);
    const processandoAcaoBloco = ref(false);

    async function concluirAcaoBloco(acao: AcaoBlocoProcesso) {
        modalBlocoRef.value?.fechar();

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
            modalBlocoRef.value?.setErro("Detalhes do processo não carregados.");
            return;
        }
        const acao = acaoBlocoAtual.value;
        if (!acao) {
            modalBlocoRef.value?.setErro(TEXTOS.processo.ERRO_ACAO_BLOCO);
            return;
        }

        dependencias.limparErro();
        processandoAcaoBloco.value = true;
        modalBlocoRef.value?.setProcessando(true);
        try {
            await processoService.executarAcaoEmBloco(processo.codigo, {
                unidadeCodigos: dados.ids,
                acao: acao.acao,
                dataLimite: dados.dataLimite,
            });
            await concluirAcaoBloco(acao);
        } catch (error) {
            modalBlocoRef.value?.setErro(dependencias.registrarErro(error) || TEXTOS.processo.ERRO_ACAO_BLOCO);
        } finally {
            processandoAcaoBloco.value = false;
            modalBlocoRef.value?.setProcessando(false);
        }
    }

    return {
        acaoBlocoAtual,
        abrirModalBloco: (acao: AcaoBlocoProcesso) => {
            acaoBlocoAtual.value = acao;
            modalBlocoRef.value?.abrir();
        },
        executarAcaoBloco,
        idsElegiveis: computed(() => {
            const unidades = acaoBlocoAtual.value?.unidades;
            return unidades ? unidades.map((unidade) => unidade.unidadeCodigo) : [];
        }),
        modalBlocoRef,
        obterIdBotaoAcao: obterIdBotaoAcaoProcesso,
        obterTestIdBotaoAcao: obterTestIdBotaoAcaoProcesso,
        processandoAcaoBloco,
        unidadesElegiveis: computed(() => {
            const elegiveis = acaoBlocoAtual.value?.unidades;
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
