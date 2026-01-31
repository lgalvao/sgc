import { ref } from "vue";
import { useAnalisesStore } from "@/stores/analises";

export function useVisMapaModais() {
    const analisesStore = useAnalisesStore();

    const mostrarModalAceitar = ref(false);
    const mostrarModalSugestoes = ref(false);
    const mostrarModalVerSugestoes = ref(false);
    const mostrarModalValidar = ref(false);
    const mostrarModalDevolucao = ref(false);
    const mostrarModalHistorico = ref(false);
    const sugestoes = ref("");
    const sugestoesVisualizacao = ref("");
    const observacaoDevolucao = ref("");

    function abrirModalAceitar() {
        mostrarModalAceitar.value = true;
    }

    function fecharModalAceitar() {
        mostrarModalAceitar.value = false;
    }

    function abrirModalSugestoes() {
        mostrarModalSugestoes.value = true;
    }

    function fecharModalSugestoes() {
        mostrarModalSugestoes.value = false;
        sugestoes.value = "";
    }

    function verSugestoes(mapaVisualizacao: { sugestoes?: string } | null) {
        sugestoesVisualizacao.value = mapaVisualizacao?.sugestoes || "Nenhuma sugestão registrada.";
        mostrarModalVerSugestoes.value = true;
    }

    function fecharModalVerSugestoes() {
        mostrarModalVerSugestoes.value = false;
        sugestoesVisualizacao.value = "";
    }

    function abrirModalValidar() {
        mostrarModalValidar.value = true;
    }

    function fecharModalValidar() {
        mostrarModalValidar.value = false;
    }

    function abrirModalDevolucao() {
        mostrarModalDevolucao.value = true;
    }

    function fecharModalDevolucao() {
        mostrarModalDevolucao.value = false;
        observacaoDevolucao.value = "";
    }

    async function abrirModalHistorico(codSubprocesso: number | undefined) {
        if (codSubprocesso) {
            await analisesStore.buscarAnalisesCadastro(codSubprocesso);
        }
        mostrarModalHistorico.value = true;
    }

    function fecharModalHistorico() {
        mostrarModalHistorico.value = false;
    }

    return {
        mostrarModalAceitar,
        mostrarModalSugestoes,
        mostrarModalVerSugestoes,
        mostrarModalValidar,
        mostrarModalDevolucao,
        mostrarModalHistorico,
        sugestoes,
        sugestoesVisualizacao,
        observacaoDevolucao,
        abrirModalAceitar,
        fecharModalAceitar,
        abrirModalSugestoes,
        fecharModalSugestoes,
        verSugestoes,
        fecharModalVerSugestoes,
        abrirModalValidar,
        fecharModalValidar,
        abrirModalDevolucao,
        fecharModalDevolucao,
        abrirModalHistorico,
        fecharModalHistorico,
    };
}
