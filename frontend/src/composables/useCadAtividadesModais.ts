import { ref, type Ref } from "vue";
import { useMapasStore } from "@/stores/mapas";
import { useAnalisesStore } from "@/stores/analises";

export interface CadAtividadesModais {
    mostrarModalImpacto: Ref<boolean>;
    mostrarModalImportar: Ref<boolean>;
    mostrarModalConfirmacao: Ref<boolean>;
    mostrarModalHistorico: Ref<boolean>;
    mostrarModalConfirmacaoRemocao: Ref<boolean>;
    dadosRemocao: Ref<{ tipo: "atividade" | "conhecimento"; index: number; conhecimentoCodigo?: number } | null>;
    loadingImpacto: Ref<boolean>;
    abrirModalHistorico: (codSubprocesso: number | null) => Promise<void>;
    abrirModalImpacto: (codSubprocesso: number | null) => void;
    fecharModalImpacto: () => void;
}

export function useCadAtividadesModais(): CadAtividadesModais {
    const mapasStore = useMapasStore();
    const analisesStore = useAnalisesStore();

    const mostrarModalImpacto = ref(false);
    const mostrarModalImportar = ref(false);
    const mostrarModalConfirmacao = ref(false);
    const mostrarModalHistorico = ref(false);
    const mostrarModalConfirmacaoRemocao = ref(false);
    const dadosRemocao = ref<{ tipo: "atividade" | "conhecimento"; index: number; conhecimentoCodigo?: number } | null>(
        null,
    );

    const loadingImpacto = ref(false);

    async function abrirModalHistorico(codSubprocesso: number | null) {
        if (codSubprocesso) {
            await analisesStore.buscarAnalisesCadastro(codSubprocesso);
        }
        mostrarModalHistorico.value = true;
    }

    function abrirModalImpacto(codSubprocesso: number | null) {
        mostrarModalImpacto.value = true;
        if (codSubprocesso) {
            loadingImpacto.value = true;
            mapasStore.buscarImpactoMapa(codSubprocesso).finally(() => (loadingImpacto.value = false));
        }
    }

    function fecharModalImpacto() {
        mostrarModalImpacto.value = false;
    }

    return {
        mostrarModalImpacto,
        mostrarModalImportar,
        mostrarModalConfirmacao,
        mostrarModalHistorico,
        mostrarModalConfirmacaoRemocao,
        dadosRemocao,
        loadingImpacto,
        abrirModalHistorico,
        abrirModalImpacto,
        fecharModalImpacto,
    };
}
