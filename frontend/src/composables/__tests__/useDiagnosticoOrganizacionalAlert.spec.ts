import {beforeEach, describe, expect, it, vi} from "vitest";
import {effectScope, nextTick, ref} from "vue";
import {useDiagnosticoOrganizacionalAlert} from "../useDiagnosticoOrganizacionalAlert";
import {useOrganizacaoStore} from "@/stores/organizacao";
import {criarPiniaDeTeste} from "@/test-utils/storeTestHelpers";

describe("useDiagnosticoOrganizacionalAlert", () => {
    beforeEach(() => {
        criarPiniaDeTeste();
        vi.clearAllMocks();
    });

    it("deve carregar o diagnóstico ao montar uma tela consumidora", async () => {
        const organizacaoStore = useOrganizacaoStore();
        const spyGarantirDiagnostico = vi.spyOn(organizacaoStore, "garantirDiagnostico").mockResolvedValue();
        const scope = effectScope();

        scope.run(() => {
            useDiagnosticoOrganizacionalAlert(ref([]), ref(true));
        });
        await nextTick();

        expect(spyGarantirDiagnostico).toHaveBeenCalledTimes(1);
        expect(spyGarantirDiagnostico).toHaveBeenCalledWith(true);

        scope.stop();
    });

    it("não deve carregar o diagnóstico quando a tela não puder exibi-lo", async () => {
        const organizacaoStore = useOrganizacaoStore();
        const spyGarantirDiagnostico = vi.spyOn(organizacaoStore, "garantirDiagnostico").mockResolvedValue();
        const scope = effectScope();

        scope.run(() => {
            useDiagnosticoOrganizacionalAlert(ref([]), ref(false));
        });
        await nextTick();

        expect(spyGarantirDiagnostico).toHaveBeenCalledTimes(1);
        expect(spyGarantirDiagnostico).toHaveBeenCalledWith(false);

        scope.stop();
    });
});
