import {type MaybeRefOrGetter, ref, toValue} from "vue";

export function useImpactoMapaModal(
    codSubprocesso: MaybeRefOrGetter<number | null | undefined>,
    buscarImpactoMapa: (codSubprocesso: number) => Promise<void>,
) {
    const mostrarModalImpacto = ref(false);
    const loadingImpacto = ref(false);

    async function abrirModalImpacto() {
        mostrarModalImpacto.value = true;

        const codigo = toValue(codSubprocesso);
        if (!codigo) {
            return;
        }

        loadingImpacto.value = true;
        try {
            await buscarImpactoMapa(codigo);
        } finally {
            loadingImpacto.value = false;
        }
    }

    function fecharModalImpacto() {
        mostrarModalImpacto.value = false;
    }

    return {
        mostrarModalImpacto,
        loadingImpacto,
        abrirModalImpacto,
        fecharModalImpacto,
    };
}
