import {useOrganizacaoStore} from "@/stores/organizacao";
import {useUnidadeStore} from "@/stores/unidade";

export function useEstadoOrganizacaoNavegacao() {
    const unidadeStore = useUnidadeStore();
    const organizacaoStore = useOrganizacaoStore();

    function atualizarEstadoOrganizacional(): void {
        unidadeStore.invalidar();
        organizacaoStore.invalidar();
    }

    function atualizarEstadoUnidade(): void {
        unidadeStore.invalidar();
    }

    function resetarEstadoOrganizacional(): void {
        unidadeStore.resetar();
        organizacaoStore.resetar();
    }

    return {
        atualizarEstadoOrganizacional,
        atualizarEstadoUnidade,
        resetarEstadoOrganizacional,
    };
}
