import {useOrganizacaoStore} from "@/stores/organizacao";
import {useInvalidacaoUnidade} from "@/composables/useUnidadeQuery";

export function useEstadoOrganizacaoNavegacao() {
    const organizacaoStore = useOrganizacaoStore();
    const {invalidarDadosTelaUnidade, invalidarUnidade, invalidarArvoreElegibilidade} = useInvalidacaoUnidade();

    function atualizarEstadoOrganizacional(): void {
        organizacaoStore.invalidar();
        invalidarUnidade();
        invalidarDadosTelaUnidade();
        invalidarArvoreElegibilidade();
    }

    function atualizarEstadoUnidade(): void {
        invalidarUnidade();
        invalidarDadosTelaUnidade();
        invalidarArvoreElegibilidade();
    }

    function resetarEstadoOrganizacional(): void {
        organizacaoStore.resetar();
        invalidarUnidade();
        invalidarDadosTelaUnidade();
        invalidarArvoreElegibilidade();
    }

    return {
        atualizarEstadoOrganizacional,
        atualizarEstadoUnidade,
        resetarEstadoOrganizacional,
    };
}
