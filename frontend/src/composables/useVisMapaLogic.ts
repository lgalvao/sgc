import { onMounted } from "vue";
import { useVisMapaState } from "@/composables/useVisMapaState";
import { useVisMapaModais } from "@/composables/useVisMapaModais";
import { useVisMapaCrud } from "@/composables/useVisMapaCrud";

export function useVisMapaLogic() {
    const state = useVisMapaState();
    const modais = useVisMapaModais();
    const crud = useVisMapaCrud();

    onMounted(async () => {
        await state.unidadesStore.buscarUnidade(state.sigla.value);
        await state.processosStore.buscarProcessoDetalhe(state.codProcesso.value);
        if (state.codSubprocesso.value) {
            await state.subprocessosStore.buscarSubprocessoDetalhe(state.codSubprocesso.value);
            await state.mapaStore.buscarMapaVisualizacao(state.codSubprocesso.value);
        }
    });

    return {
        perfilSelecionado: state.perfilSelecionado,
        permissoes: state.permissoes,
        mapa: state.mapa,
        unidade: state.unidade,
        subprocesso: state.subprocesso,
        podeValidar: state.podeValidar,
        podeAnalisar: state.podeAnalisar,
        podeVerSugestoes: state.podeVerSugestoes,
        temHistoricoAnalise: state.temHistoricoAnalise,
        historicoAnalise: state.historicoAnalise,
        mostrarModalAceitar: modais.mostrarModalAceitar,
        mostrarModalSugestoes: modais.mostrarModalSugestoes,
        mostrarModalVerSugestoes: modais.mostrarModalVerSugestoes,
        mostrarModalValidar: modais.mostrarModalValidar,
        mostrarModalDevolucao: modais.mostrarModalDevolucao,
        mostrarModalHistorico: modais.mostrarModalHistorico,
        sugestoes: modais.sugestoes,
        sugestoesVisualizacao: modais.sugestoesVisualizacao,
        observacaoDevolucao: modais.observacaoDevolucao,
        isLoading: crud.isLoading,
        confirmarSugestoes: () => crud.confirmarSugestoes(state.codSubprocesso.value, modais.sugestoes.value, modais.fecharModalSugestoes),
        confirmarValidacao: () => crud.confirmarValidacao(state.codSubprocesso.value, modais.fecharModalValidar),
        confirmarAceitacao: (observacoes?: string) =>
            crud.confirmarAceitacao(
                state.codSubprocesso.value,
                state.permissoes.value?.podeHomologarMapa,
                state.perfilSelecionado.value,
                state.processo.value?.tipo,
                observacoes,
                modais.fecharModalAceitar
            ),
        confirmarDevolucao: () => crud.confirmarDevolucao(state.codSubprocesso.value, modais.observacaoDevolucao.value, modais.fecharModalDevolucao),
        abrirModalAceitar: modais.abrirModalAceitar,
        fecharModalAceitar: modais.fecharModalAceitar,
        abrirModalSugestoes: modais.abrirModalSugestoes,
        fecharModalSugestoes: modais.fecharModalSugestoes,
        verSugestoes: () => modais.verSugestoes(state.mapa.value),
        fecharModalVerSugestoes: modais.fecharModalVerSugestoes,
        abrirModalValidar: modais.abrirModalValidar,
        fecharModalValidar: modais.fecharModalValidar,
        abrirModalDevolucao: modais.abrirModalDevolucao,
        fecharModalDevolucao: modais.fecharModalDevolucao,
        abrirModalHistorico: () => modais.abrirModalHistorico(state.codSubprocesso.value),
        fecharModalHistorico: modais.fecharModalHistorico,
    };
}
