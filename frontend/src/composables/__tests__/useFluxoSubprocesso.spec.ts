import {describe, it, expect, beforeEach, vi} from "vitest";
import {useFluxoSubprocesso} from "../useFluxoSubprocesso";
import * as cadastroService from "@/services/cadastroService";
import * as subprocessoService from "@/services/subprocessoService";

vi.mock("@/services/cadastroService", () => ({
    disponibilizarCadastro: vi.fn(),
    disponibilizarRevisaoCadastro: vi.fn(),
    iniciarRevisaoCadastro: vi.fn(),
    cancelarInicioRevisaoCadastro: vi.fn(),
}));

vi.mock("@/services/subprocessoService", () => ({
    validarCadastro: vi.fn(),
    alterarDataLimiteSubprocesso: vi.fn(),
}));

vi.mock("@/composables/useErrorHandler", () => ({
    useErrorHandler: () => ({
        lastError: {value: null},
        clearError: vi.fn(),
        withErrorHandling: vi.fn((cb) => cb())
    })
}));

vi.mock("@/stores/subprocesso", () => ({
    useSubprocessoStore: () => ({
        garantirContextoCadastroAtividades: vi.fn()
    })
}));

vi.mock("@/stores/toast", () => ({
    useToastStore: () => ({
        setPending: vi.fn()
    })
}));

vi.mock("vue-router", () => ({
    useRouter: () => ({
        push: vi.fn()
    })
}));

vi.mock("@/composables/useInvalidacaoNavegacao", () => ({
    useInvalidacaoNavegacao: () => ({
        invalidarCachesSubprocesso: vi.fn()
    })
}));

describe("useFluxoSubprocesso", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("deve validar cadastro", async () => {
        const {validarCadastro} = useFluxoSubprocesso();
        vi.mocked(subprocessoService.validarCadastro).mockResolvedValue({valido: true} as any);

        const res = await validarCadastro(123);

        expect(res.valido).toBe(true);
        expect(subprocessoService.validarCadastro).toHaveBeenCalledWith(123);
    });

    it("deve disponibilizar cadastro", async () => {
        const {disponibilizarCadastro} = useFluxoSubprocesso();
        vi.mocked(cadastroService.disponibilizarCadastro).mockResolvedValue({} as any);

        const success = await disponibilizarCadastro(123);

        expect(success).toBe(true);
        expect(cadastroService.disponibilizarCadastro).toHaveBeenCalledWith(123);
    });

    it("deve iniciar revisão de cadastro", async () => {
        const {iniciarRevisaoCadastro} = useFluxoSubprocesso();
        vi.mocked(cadastroService.iniciarRevisaoCadastro).mockResolvedValue({} as any);

        const success = await iniciarRevisaoCadastro(123);

        expect(success).toBe(true);
        expect(cadastroService.iniciarRevisaoCadastro).toHaveBeenCalledWith(123);
    });

    it("deve cancelar início de revisão", async () => {
        const {cancelarInicioRevisaoCadastro} = useFluxoSubprocesso();
        vi.mocked(cadastroService.cancelarInicioRevisaoCadastro).mockResolvedValue({} as any);

        const success = await cancelarInicioRevisaoCadastro(123);

        expect(success).toBe(true);
        expect(cadastroService.cancelarInicioRevisaoCadastro).toHaveBeenCalledWith(123);
    });
});
