import { onMounted } from "vue";
import { useSubprocessosStore } from "@/stores/subprocessos";
import { useAtividadeForm } from "@/composables/useAtividadeForm";
import { useCadAtividadesState } from "@/composables/useCadAtividadesState";
import { useCadAtividadesModais } from "@/composables/useCadAtividadesModais";
import { useCadAtividadesCrud } from "@/composables/useCadAtividadesCrud";
import { useCadAtividadesValidacao } from "@/composables/useCadAtividadesValidacao";
import logger from "@/utils/logger";

export function useCadAtividadesLogic(props: { codProcesso: number | string; sigla: string }) {
    const subprocessosStore = useSubprocessosStore();

    const state = useCadAtividadesState();
    const modais = useCadAtividadesModais();
    const validacao = useCadAtividadesValidacao();

    const { novaAtividade, loadingAdicionar, adicionarAtividade: adicionarAtividadeAction } = useAtividadeForm();

    const crud = useCadAtividadesCrud(adicionarAtividadeAction);

    onMounted(async () => {
        const codProcessoRef = Number(props.codProcesso);
        const id = await subprocessosStore.buscarSubprocessoPorProcessoEUnidade(codProcessoRef, props.sigla);

        if (id) {
            state.codSubprocesso.value = id;
            await subprocessosStore.buscarContextoEdicao(id);
        } else {
            logger.error("[CadAtividades] ERRO: Subprocesso não encontrado!");
        }
    });

    return {
        router: state.router,
        isChefe: state.isChefe,
        codSubprocesso: state.codSubprocesso,
        subprocesso: state.subprocesso,
        nomeUnidade: state.nomeUnidade,
        permissoes: state.permissoes,
        atividades: state.atividades,
        isRevisao: state.isRevisao,
        historicoAnalises: state.historicoAnalises,
        novaAtividade,
        loadingAdicionar,
        loadingValidacao: validacao.loadingValidacao,
        loadingImpacto: modais.loadingImpacto,
        impactoMapa: state.impactoMapa,
        mostrarModalImpacto: modais.mostrarModalImpacto,
        mostrarModalImportar: modais.mostrarModalImportar,
        mostrarModalConfirmacao: modais.mostrarModalConfirmacao,
        mostrarModalHistorico: modais.mostrarModalHistorico,
        mostrarModalConfirmacaoRemocao: modais.mostrarModalConfirmacaoRemocao,
        dadosRemocao: modais.dadosRemocao,
        errosValidacao: validacao.errosValidacao,
        erroGlobal: validacao.erroGlobal,
        podeVerImpacto: state.podeVerImpacto,
        adicionarAtividade: async () => await crud.adicionarAtividade(state.codSubprocesso.value, state.codMapa.value),
        removerAtividade: (idx: number) => crud.removerAtividade(idx, state.codSubprocesso.value, modais.dadosRemocao, modais.mostrarModalConfirmacaoRemocao),
        confirmarRemocao: () => crud.confirmarRemocao(modais.dadosRemocao, state.codSubprocesso.value, state.atividades.value, modais.mostrarModalConfirmacaoRemocao),
        adicionarConhecimento: (idx: number, descricao: string) => crud.adicionarConhecimento(idx, descricao, state.codSubprocesso.value, state.atividades.value),
        removerConhecimento: (idx: number, conhecimentoCodigo: number) => crud.removerConhecimento(idx, conhecimentoCodigo, state.codSubprocesso.value, modais.dadosRemocao, modais.mostrarModalConfirmacaoRemocao),
        salvarEdicaoConhecimento: (atividadeCodigo: number, conhecimentoCodigo: number, descricao: string) => crud.salvarEdicaoConhecimento(atividadeCodigo, conhecimentoCodigo, descricao, state.codSubprocesso.value),
        salvarEdicaoAtividade: (codigo: number, descricao: string) => crud.salvarEdicaoAtividade(codigo, descricao, state.codSubprocesso.value, state.atividades.value),
        handleImportAtividades: () => crud.handleImportAtividades(state.codSubprocesso.value, modais.mostrarModalImportar),
        obterErroParaAtividade: validacao.obterErroParaAtividade,
        setAtividadeRef: validacao.setAtividadeRef,
        abrirModalHistorico: () => modais.abrirModalHistorico(state.codSubprocesso.value),
        abrirModalImpacto: () => modais.abrirModalImpacto(state.codSubprocesso.value),
        disponibilizarCadastro: () => validacao.disponibilizarCadastro(state.codSubprocesso.value, state.subprocesso.value, state.isRevisao.value, modais.mostrarModalConfirmacao),
        confirmarDisponibilizacao: () => validacao.confirmarDisponibilizacao(state.codSubprocesso.value, state.isRevisao.value, modais.mostrarModalConfirmacao, state.router),
        fecharModalImpacto: modais.fecharModalImpacto,
    };
}
