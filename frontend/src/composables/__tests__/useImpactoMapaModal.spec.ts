import {describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import {useImpactoMapaModal} from "../useImpactoMapaModal";

describe("useImpactoMapaModal", () => {
    it("abre modal sem carregar impacto quando nao houver subprocesso", async () => {
        const buscarImpactoMapa = vi.fn().mockResolvedValue(undefined);
        const codSubprocesso = ref<number | null>(null);
        const composable = useImpactoMapaModal(codSubprocesso, buscarImpactoMapa);

        await composable.abrirModalImpacto();

        expect(composable.mostrarModalImpacto.value).toBe(true);
        expect(composable.loadingImpacto.value).toBe(false);
        expect(buscarImpactoMapa).not.toHaveBeenCalled();
    });

    it("carrega impacto e encerra loading ao abrir modal", async () => {
        const buscarImpactoMapa = vi.fn().mockResolvedValue(undefined);
        const codSubprocesso = ref<number | null>(123);
        const composable = useImpactoMapaModal(codSubprocesso, buscarImpactoMapa);

        await composable.abrirModalImpacto();

        expect(buscarImpactoMapa).toHaveBeenCalledWith(123);
        expect(composable.mostrarModalImpacto.value).toBe(true);
        expect(composable.loadingImpacto.value).toBe(false);
    });

    it("fecha modal sem alterar estado de carga", () => {
        const composable = useImpactoMapaModal(ref<number | null>(123), vi.fn().mockResolvedValue(undefined));
        composable.mostrarModalImpacto.value = true;
        composable.loadingImpacto.value = true;

        composable.fecharModalImpacto();

        expect(composable.mostrarModalImpacto.value).toBe(false);
        expect(composable.loadingImpacto.value).toBe(true);
    });
});
