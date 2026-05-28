import {beforeEach, describe, expect, it, vi} from "vitest";
import {effectScope, nextTick, ref} from "vue";
import {useDiagnosticoOrganizacionalAlert} from "../useDiagnosticoOrganizacionalAlert";
import {criarPiniaDeTeste} from "@/test-utils/storeTestHelpers";

const mockQueryData = ref<any>(null);
const mockIsLoading = ref(false);
const mockError = ref<Error | null>(null);

vi.mock("@/composables/useDiagnosticoOrganizacionalQuery", () => ({
    useDiagnosticoOrganizacionalQuery: () => ({
        data: mockQueryData,
        isLoading: mockIsLoading,
        error: mockError,
    }),
    useInvalidacaoDiagnosticoOrganizacional: () => ({
        invalidarDiagnostico: vi.fn(),
    }),
}));

describe("useDiagnosticoOrganizacionalAlert", () => {
    beforeEach(() => {
        criarPiniaDeTeste();
        vi.clearAllMocks();
        mockQueryData.value = null;
        mockIsLoading.value = false;
        mockError.value = null;
    });

    it("deve exibir o alerta quando há violações e o perfil tem permissão", async () => {
        mockQueryData.value = {possuiViolacoes: true, resumo: "Há pendências", grupos: []};
        const scope = effectScope();

        let resultado: ReturnType<typeof useDiagnosticoOrganizacionalAlert> | undefined;
        scope.run(() => {
            resultado = useDiagnosticoOrganizacionalAlert(ref([]), ref(true));
        });
        await nextTick();

        expect(resultado!.exibirAlertaDiagnostico.value).toBe(true);

        scope.stop();
    });

    it("não deve exibir o alerta quando o perfil não tem permissão", async () => {
        mockQueryData.value = {possuiViolacoes: true, resumo: "Há pendências", grupos: []};
        const scope = effectScope();

        let resultado: ReturnType<typeof useDiagnosticoOrganizacionalAlert> | undefined;
        scope.run(() => {
            resultado = useDiagnosticoOrganizacionalAlert(ref([]), ref(false));
        });
        await nextTick();

        expect(resultado!.exibirAlertaDiagnostico.value).toBe(false);

        scope.stop();
    });

    it("deve exibir mensagem de erro quando a query falha", async () => {
        mockError.value = new Error("Erro de rede");
        const scope = effectScope();

        let resultado: ReturnType<typeof useDiagnosticoOrganizacionalAlert> | undefined;
        scope.run(() => {
            resultado = useDiagnosticoOrganizacionalAlert(ref([]), ref(true));
        });
        await nextTick();

        expect(resultado!.erroDiagnosticoOrganizacional.value).toBe(
            "Não foi possível verificar as pendências organizacionais."
        );
        expect(resultado!.exibirAlertaDiagnostico.value).toBe(true);

        scope.stop();
    });

    it("deve permitir dispensar o alerta", async () => {
        mockQueryData.value = {possuiViolacoes: true, resumo: "Há pendências", grupos: []};
        const scope = effectScope();

        let resultado: ReturnType<typeof useDiagnosticoOrganizacionalAlert> | undefined;
        scope.run(() => {
            resultado = useDiagnosticoOrganizacionalAlert(ref([]), ref(true));
        });
        await nextTick();

        expect(resultado!.exibirAlertaDiagnostico.value).toBe(true);
        resultado!.dispensarAlertaDiagnostico();
        expect(resultado!.exibirAlertaDiagnostico.value).toBe(false);

        scope.stop();
    });
});
