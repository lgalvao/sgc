import {useMapasStore} from "@/stores/mapas";
import {useOrganizacaoStore} from "@/stores/organizacao";
import {usePainelStore} from "@/stores/painel";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useUnidadeStore} from "@/stores/unidade";

export function useInvalidacaoEstadoNavegacao() {
    const {invalidar: atualizarPainelLocal} = usePainelStore();
    const {invalidar: atualizarSubprocessoLocal, limparContextoAtual: limparSubprocessoAtualLocal} = useSubprocessoStore();
    const {invalidar: atualizarMapasLocais, marcarMapaParaAtualizacao: atualizarMapaLocalPorCodigo} = useMapasStore();
    const {invalidar: atualizarUnidadeLocal} = useUnidadeStore();
    const {invalidar: atualizarOrganizacaoLocal} = useOrganizacaoStore();

    function atualizarEstadoPainel(): void {
        atualizarPainelLocal();
    }

    function atualizarEstadoSubprocesso(): void {
        atualizarSubprocessoLocal();
    }

    function atualizarEstadoMapa(codigoSubprocesso?: number): void {
        if (typeof codigoSubprocesso === "number") {
            atualizarMapaLocalPorCodigo(codigoSubprocesso);
            return;
        }

        atualizarMapasLocais();
    }

    function atualizarEstadoOrganizacional(): void {
        atualizarUnidadeLocal();
        atualizarOrganizacaoLocal();
    }

    function atualizarEstadoUnidade(): void {
        atualizarUnidadeLocal();
    }

    function limparSubprocessoAtual(): void {
        limparSubprocessoAtualLocal();
    }

    return {
        atualizarEstadoMapa,
        atualizarEstadoOrganizacional,
        atualizarEstadoPainel,
        atualizarEstadoSubprocesso,
        atualizarEstadoUnidade,
        limparSubprocessoAtual,
    };
}
