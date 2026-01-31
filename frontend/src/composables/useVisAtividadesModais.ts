import { ref } from "vue";
import { storeToRefs } from "pinia";
import { useMapasStore } from "@/stores/mapas";
import { useAnalisesStore } from "@/stores/analises";

export function useVisAtividadesModais() {
    const mapasStore = useMapasStore();
    const analisesStore = useAnalisesStore();

    const { impactoMapa } = storeToRefs(mapasStore);
    const loadingImpacto = ref(false);

    const mostrarModalImpacto = ref(false);
    const mostrarModalValidar = ref(false);
    const mostrarModalDevolver = ref(false);
    const mostrarModalHistoricoAnalise = ref(false);
    const observacaoValidacao = ref("");
    const observacaoDevolucao = ref("");

    async function abrirModalImpacto(codSubprocesso: number | undefined) {
        mostrarModalImpacto.value = true;
        if (codSubprocesso) {
            loadingImpacto.value = true;
            try {
                await mapasStore.buscarImpactoMapa(codSubprocesso);
            } finally {
                loadingImpacto.value = false;
            }
        }
    }

    function fecharModalImpacto() {
        mostrarModalImpacto.value = false;
    }

    async function abrirModalHistoricoAnalise(codSubprocesso: number | undefined) {
        if (codSubprocesso) {
            await analisesStore.buscarAnalisesCadastro(codSubprocesso);
        }
        mostrarModalHistoricoAnalise.value = true;
    }

    function fecharModalHistoricoAnalise() {
        mostrarModalHistoricoAnalise.value = false;
    }

    function validarCadastro() {
        mostrarModalValidar.value = true;
    }

    function devolverCadastro() {
        mostrarModalDevolver.value = true;
    }

    function fecharModalValidar() {
        mostrarModalValidar.value = false;
        observacaoValidacao.value = "";
    }

    function fecharModalDevolver() {
        mostrarModalDevolver.value = false;
        observacaoDevolucao.value = "";
    }

    return {
        impactoMapa,
        loadingImpacto,
        mostrarModalImpacto,
        mostrarModalValidar,
        mostrarModalDevolver,
        mostrarModalHistoricoAnalise,
        observacaoValidacao,
        observacaoDevolucao,
        abrirModalImpacto,
        fecharModalImpacto,
        abrirModalHistoricoAnalise,
        fecharModalHistoricoAnalise,
        validarCadastro,
        devolverCadastro,
        fecharModalValidar,
        fecharModalDevolver,
    };
}
