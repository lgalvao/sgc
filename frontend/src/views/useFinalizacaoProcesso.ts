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

export function useFinalizacaoProcesso(dependencias: DependenciasProcessoAcoes) {
    const router = useRouter();
    const {registrarPendente} = useToast();
    const {atualizarFluxoProcesso} = useInvalidacaoNavegacao();
    const loadingFinalizacao = ref(false);
    const mostrarModalFinalizacao = ref(false);

    async function confirmarFinalizacao() {
        if (loadingFinalizacao.value) {
            return;
        }

        loadingFinalizacao.value = true;
        try {
            dependencias.limparErro();
            await processoService.finalizarProcesso(dependencias.codProcesso);
            registrarPendente(TEXTOS_SUCESSO_PROCESSO.PROCESSO_FINALIZADO);
            await atualizarFluxoProcesso();
            dependencias.processo.value = null;
            await useQueryCache().invalidateQueries({key: CHAVE_QUERY_HISTORICO, exact: true});
            await router.push("/painel");
        } catch (error) {
            dependencias.notify(dependencias.registrarErro(error) || TEXTOS.processo.ERRO_PADRAO, "danger");
        } finally {
            loadingFinalizacao.value = false;
        }
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
        loadingFinalizacao,
        mostrarModalFinalizacao,
    };
}
