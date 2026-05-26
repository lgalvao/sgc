import {useEstadoMapaNavegacao} from "@/composables/useEstadoMapaNavegacao";
import {useEstadoOrganizacaoNavegacao} from "@/composables/useEstadoOrganizacaoNavegacao";
import {useEstadoPainelNavegacao} from "@/composables/useEstadoPainelNavegacao";
import {useEstadoSubprocessoNavegacao} from "@/composables/useEstadoSubprocessoNavegacao";

export function useInvalidacaoEstadoNavegacao() {
    const painel = useEstadoPainelNavegacao();
    const subprocesso = useEstadoSubprocessoNavegacao();
    const mapa = useEstadoMapaNavegacao();
    const organizacao = useEstadoOrganizacaoNavegacao();

    function resetarEstadoSessao(): void {
        painel.resetarEstadoPainel();
        subprocesso.resetarEstadoSubprocesso();
        mapa.resetarEstadoMapa();
        organizacao.resetarEstadoOrganizacional();
    }

    return {
        atualizarEstadoMapa: mapa.atualizarEstadoMapa,
        atualizarEstadoOrganizacional: organizacao.atualizarEstadoOrganizacional,
        atualizarEstadoPainel: painel.atualizarEstadoPainel,
        atualizarEstadoSubprocesso: subprocesso.atualizarEstadoSubprocesso,
        atualizarEstadoUnidade: organizacao.atualizarEstadoUnidade,
        limparSubprocessoAtual: subprocesso.limparSubprocessoAtual,
        resetarEstadoSessao,
    };
}
