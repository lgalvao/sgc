import {type MaybeRefOrGetter, ref, toValue} from "vue";

export function useImpactoMapaModal(
    codSubprocesso: MaybeRefOrGetter<number | null | undefined>,
    carregarImpacto: (codSubprocesso: number) => Promise<void>,
) {
    const mostrarModalImpacto = ref(false);
    const loadingImpacto = ref(false);

    async function abrirModalImpacto() {
        mostrarModalImpacto.value = true;

        if (loadingImpacto.value) {
            return;
        }

        const codigo = toValue(codSubprocesso);
        if (!codigo) {
            return;
        }

        loadingImpacto.value = true;
        try {
            await carregarImpacto(codigo);
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
