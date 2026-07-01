import {computed, ref} from "vue";
import {useRouter} from "vue-router";
import * as processoService from "@/services/processo";
import {TEXTOS} from "@/constants/textos";
import {formatSituacaoSubprocesso} from "@/utils/formatters";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import {useToast} from "@/composables/useToast";
import type {AcaoBlocoProcesso, SubprocessoElegivel} from "@/types/tipos";
import {obterIdBotaoAcaoProcesso, obterTestIdBotaoAcaoProcesso} from "@/components/processo/processoAcoes";
import type {DadosAcaoBloco, DependenciasProcessoAcoes, ModalAcaoBlocoRef} from "@/views/processoDetalheTipos";
import {useAsyncAction} from "@/composables/useAsyncAction";

export function useAcoesBlocoProcesso(dependencias: DependenciasProcessoAcoes) {
    const router = useRouter();
    const {exibirSucesso, registrarPendente} = useToast();
    const {atualizarFluxoProcesso, atualizarFluxoSubprocessoEProcesso} = useInvalidacaoNavegacao();
    const modalBlocoRef = ref<ModalAcaoBlocoRef | null>(null);
    const acaoBlocoAtual = ref<AcaoBlocoProcesso | null>(null);
    const acaoBloco = useAsyncAction();

    async function concluirAcaoBloco(acao: AcaoBlocoProcesso) {
        modalBlocoRef.value?.fechar();

        if (acao.redirecionarPainel) {
            registrarPendente(acao.mensagemSucesso);
            await atualizarFluxoProcesso();
            dependencias.processo.value = null;
            await router.push("/painel");
            return;
        }

        exibirSucesso(acao.mensagemSucesso);
        await atualizarFluxoSubprocessoEProcesso();
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
        modalBlocoRef.value?.setProcessando(true);
        try {
            await acaoBloco.executar(
                () => processoService.executarAcaoEmBloco(processo.codigo, {
                    unidadeCodigos: dados.ids,
                    acao: acao.acao,
                    dataLimite: dados.dataLimite,
                }),
                TEXTOS.processo.ERRO_ACAO_BLOCO,
                {
                    relancarErro: false,
                    aoSucesso: async () => {
                        await concluirAcaoBloco(acao);
                    },
                    aoOcorrerErro: (_erro, causa) => {
                        modalBlocoRef.value?.setErro(dependencias.registrarErro(causa) || TEXTOS.processo.ERRO_ACAO_BLOCO);
                    },
                },
            );
        } finally {
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
        processandoAcaoBloco: acaoBloco.carregando,
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
