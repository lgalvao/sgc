import {beforeEach, describe, expect, it, vi} from "vitest";
import {normalizeError} from "@/utils/apiError";
import {createTestingPinia} from "@pinia/testing";
import {setActivePinia} from "pinia";

vi.mock("@/services/cadastroService", () => ({
    disponibilizarCadastro: vi.fn(),
    disponibilizarRevisaoCadastro: vi.fn(),
    iniciarRevisaoCadastro: vi.fn(),
    cancelarInicioRevisaoCadastro: vi.fn(),
    devolverCadastro: vi.fn(),
    devolverRevisaoCadastro: vi.fn(),
    aceitarCadastro: vi.fn(),
    aceitarRevisaoCadastro: vi.fn(),
    homologarCadastro: vi.fn(),
    homologarRevisaoCadastro: vi.fn(),
}));

vi.mock("@/services/subprocessoService", () => ({
    validarCadastro: vi.fn(),
}));

vi.mock("@/services/processoService", () => ({
    alterarDataLimiteSubprocesso: vi.fn(),
    reabrirCadastro: vi.fn(),
    reabrirRevisaoCadastro: vi.fn(),
}));

vi.mock("vue-router", () => ({
    useRouter: () => ({
        push: vi.fn(),
    }),
}));

vi.mock("@/composables/useInvalidacaoNavegacao", () => ({
    useInvalidacaoNavegacao: () => ({
        invalidarCachesSubprocesso: vi.fn(),
    }),
}));

const subprocessoStoreMock = {
    garantirContextoEdicao: vi.fn(),
    garantirContextoCadastroAtividades: vi.fn(),
};

vi.mock("@/stores/subprocesso", () => ({
    useSubprocessoStore: () => subprocessoStoreMock,
}));

describe("useFluxoSubprocesso", () => {
    beforeEach(() => {
        setActivePinia(createTestingPinia({ stubActions: true }));
        vi.clearAllMocks();
    });

    it("deve validar cadastro", async () => {
        const {useFluxoSubprocesso} = await import("../useFluxoSubprocesso");
        const {validarCadastro} = useFluxoSubprocesso();
        const {validarCadastro: serviceValidarCadastro} = await import("@/services/subprocessoService");
        (serviceValidarCadastro as any).mockResolvedValue({valido: true});

        const resultado = await validarCadastro(10);

        expect(serviceValidarCadastro).toHaveBeenCalledWith(10);
        expect(resultado).toEqual({valido: true});
    });

    it("deve disponibilizar cadastro e redirecionar para painel", async () => {
        const {useFluxoSubprocesso} = await import("../useFluxoSubprocesso");
        const {disponibilizarCadastro} = useFluxoSubprocesso();
        const {disponibilizarCadastro: service} = await import("@/services/cadastroService");
        (service as any).mockResolvedValue(undefined);

        const resultado = await disponibilizarCadastro(10);

        expect(resultado).toBe(true);
        expect(service).toHaveBeenCalledWith(10);
    });

    it("deve disponibilizar revisao cadastro", async () => {
        const {useFluxoSubprocesso} = await import("../useFluxoSubprocesso");
        const {disponibilizarCadastro} = useFluxoSubprocesso();
        const {disponibilizarRevisaoCadastro: service} = await import("@/services/cadastroService");
        (service as any).mockResolvedValue(undefined);

        await disponibilizarCadastro(10, true);
        expect(service).toHaveBeenCalledWith(10);
    });

    it("deve homologar cadastro e permitir redirecionamento customizado", async () => {
        const {useFluxoSubprocesso} = await import("../useFluxoSubprocesso");
        const {homologarCadastro} = useFluxoSubprocesso();
        const {homologarCadastro: service} = await import("@/services/cadastroService");
        (service as any).mockResolvedValue(undefined);

        const resultado = await homologarCadastro(10, {observacoes: "ok"}, false, {
            redirecionarPara: { name: 'Subprocesso', params: { codProcesso: 1, siglaUnidade: 'TEST' } }
        });

        expect(resultado).toBe(true);
        expect(service).toHaveBeenCalledWith(10, {observacoes: "ok"});
    });

    it("deve retornar false e registrar erro ao reabrir cadastro com falha", async () => {
        const {useFluxoSubprocesso} = await import("../useFluxoSubprocesso");
        const {reabrirCadastro, lastError} = useFluxoSubprocesso();
        const {reabrirCadastro: serviceReabrirCadastro} = await import("@/services/processoService");
        const erro = new Error("Falha");
        (serviceReabrirCadastro as any).mockRejectedValue(erro);

        const resultado = await reabrirCadastro(10, "Justificativa");

        expect(resultado).toBe(false);
        expect(lastError.value).toEqual(normalizeError(erro));
    });

    it("deve iniciar revisao e recarregar contexto de atividades", async () => {
        const {useFluxoSubprocesso} = await import("../useFluxoSubprocesso");
        const {iniciarRevisaoCadastro} = useFluxoSubprocesso();
        const {iniciarRevisaoCadastro: service} = await import("@/services/cadastroService");
        (service as any).mockResolvedValue(undefined);

        await iniciarRevisaoCadastro(10);

        expect(service).toHaveBeenCalledWith(10);
        expect(subprocessoStoreMock.garantirContextoCadastroAtividades).toHaveBeenCalledWith(10, true);
    });

    it("deve devolver cadastro", async () => {
        const {useFluxoSubprocesso} = await import("../useFluxoSubprocesso");
        const {devolverCadastro} = useFluxoSubprocesso();
        const {devolverCadastro: service} = await import("@/services/cadastroService");
        (service as any).mockResolvedValue(undefined);

        await devolverCadastro(10, {observacoes: "Erro"});
        expect(service).toHaveBeenCalledWith(10, {observacoes: "Erro"});
    });

    it("deve aceitar revisao cadastro", async () => {
        const {useFluxoSubprocesso} = await import("../useFluxoSubprocesso");
        const {aceitarCadastro} = useFluxoSubprocesso();
        const {aceitarRevisaoCadastro: service} = await import("@/services/cadastroService");
        (service as any).mockResolvedValue(undefined);

        await aceitarCadastro(10, {observacoes: "OK"}, true);
        expect(service).toHaveBeenCalledWith(10, {observacoes: "OK"});
    });

    it("deve homologar revisao cadastro", async () => {
        const {useFluxoSubprocesso} = await import("../useFluxoSubprocesso");
        const {homologarCadastro} = useFluxoSubprocesso();
        const {homologarRevisaoCadastro: service} = await import("@/services/cadastroService");
        (service as any).mockResolvedValue(undefined);

        await homologarCadastro(10, {observacoes: "OK"}, true);
        expect(service).toHaveBeenCalledWith(10, {observacoes: "OK"});
    });

    it("deve reabrir revisao cadastro", async () => {
        const {useFluxoSubprocesso} = await import("../useFluxoSubprocesso");
        const {reabrirCadastro} = useFluxoSubprocesso();
        const {reabrirRevisaoCadastro: service} = await import("@/services/processoService");
        (service as any).mockResolvedValue(undefined);

        await reabrirCadastro(10, "Justificativa", true);
        expect(service).toHaveBeenCalledWith(10, "Justificativa");
    });
});
