import { onMounted } from "vue";
import { useVisAtividadesState } from "@/composables/useVisAtividadesState";
import { useVisAtividadesModais } from "@/composables/useVisAtividadesModais";
import { useVisAtividadesCrud } from "@/composables/useVisAtividadesCrud";

export function useVisAtividadesLogic(props: { codProcesso: number | string; sigla: string }) {
    const state = useVisAtividadesState(props);
    const modais = useVisAtividadesModais();
    const crud = useVisAtividadesCrud();

    onMounted(async () => {
        await state.processosStore.buscarProcessoDetalhe(state.codProcesso.value);
        if (state.codSubprocesso.value) {
            await state.atividadesStore.buscarAtividadesParaSubprocesso(state.codSubprocesso.value);
        }
    });

    return {
        atividades: state.atividades,
        siglaUnidade: state.siglaUnidade,
        nomeUnidade: state.nomeUnidade,
        isRevisao: state.isRevisao,
        isHomologacao: state.isHomologacao,
        podeVerImpacto: state.podeVerImpacto,
        codSubprocesso: state.codSubprocesso,
        impactoMapa: modais.impactoMapa,
        loadingImpacto: modais.loadingImpacto,
        mostrarModalImpacto: modais.mostrarModalImpacto,
        historicoAnalises: state.historicoAnalises,
        mostrarModalHistoricoAnalise: modais.mostrarModalHistoricoAnalise,
        mostrarModalValidar: modais.mostrarModalValidar,
        loadingValidacao: crud.loadingValidacao,
        observacaoValidacao: modais.observacaoValidacao,
        mostrarModalDevolver: modais.mostrarModalDevolver,
        loadingDevolucao: crud.loadingDevolucao,
        observacaoDevolucao: modais.observacaoDevolucao,
        perfilSelecionado: state.perfilSelecionado,
        Perfil: state.Perfil,
        abrirModalImpacto: () => modais.abrirModalImpacto(state.codSubprocesso.value),
        fecharModalImpacto: modais.fecharModalImpacto,
        abrirModalHistoricoAnalise: () => modais.abrirModalHistoricoAnalise(state.codSubprocesso.value),
        fecharModalHistoricoAnalise: modais.fecharModalHistoricoAnalise,
        validarCadastro: modais.validarCadastro,
        devolverCadastro: modais.devolverCadastro,
        confirmarValidacao: () =>
            crud.confirmarValidacao(
                state.codSubprocesso.value,
                state.isHomologacao.value,
                state.isRevisao.value,
                modais.observacaoValidacao.value,
                props,
                modais.fecharModalValidar
            ),
        confirmarDevolucao: () =>
            crud.confirmarDevolucao(
                state.codSubprocesso.value,
                state.isRevisao.value,
                modais.observacaoDevolucao.value,
                modais.fecharModalDevolver
            ),
        fecharModalValidar: modais.fecharModalValidar,
        fecharModalDevolver: modais.fecharModalDevolver,
    };
}
