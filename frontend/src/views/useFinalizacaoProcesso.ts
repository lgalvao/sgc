import {ref} from "vue";
import {useQueryCache} from "@pinia/colada";
import {useRouter} from "vue-router";
import * as processoService from "@/services/processo";
import {TEXTOS} from "@/constants/textos";
import {TEXTOS_SUCESSO_PROCESSO} from "@/constants/textos-processo";
import {useToast} from "@/composables/useToast";
import {CHAVE_QUERY_HISTORICO} from "@/composables/useHistoricoQuery";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import type {DependenciasProcessoAcoes} from "@/views/processoDetalheTipos";
import {useAsyncAction} from "@/composables/useAsyncAction";

export function useFinalizacaoProcesso(dependencias: DependenciasProcessoAcoes) {
    const router = useRouter();
    const {registrarPendente} = useToast();
    const {atualizarFluxoProcesso} = useInvalidacaoNavegacao();
    const acaoFinalizacao = useAsyncAction();
    const mostrarModalFinalizacao = ref(false);

    async function confirmarFinalizacao() {
        if (acaoFinalizacao.carregando.value) {
            return;
        }

        dependencias.limparErro();
        await acaoFinalizacao.executar(
            () => processoService.finalizarProcesso(dependencias.codProcesso),
            TEXTOS.processo.ERRO_PADRAO,
            {
                relancarErro: false,
                aoOcorrerErro: (_erro, causa) => {
                    dependencias.notify(dependencias.registrarErro(causa) || TEXTOS.processo.ERRO_PADRAO, "danger");
                },
                aoSucesso: async () => {
                    registrarPendente(TEXTOS_SUCESSO_PROCESSO.PROCESSO_FINALIZADO);
                    await atualizarFluxoProcesso();
                    dependencias.processo.value = null;
                    await useQueryCache().invalidateQueries({key: CHAVE_QUERY_HISTORICO, exact: true});
                    await router.push("/painel");
                },
            },
        );
    }

    function finalizarProcesso() {
        const processo = dependencias.processo.value;
        if (processo && processo.podeFinalizar === false) {
            dependencias.notify(processo.mensagemFinalizacao || TEXTOS.processo.ERRO_PADRAO, "danger");
            return;
        }
        mostrarModalFinalizacao.value = true;
    }

    return {
        confirmarFinalizacao,
        finalizarProcesso,
        loadingFinalizacao: acaoFinalizacao.carregando,
        mostrarModalFinalizacao,
    };
}
