import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import {normalizeError} from "@/utils/apiError";

vi.mock("@/services/cadastroService", () => ({
    disponibilizarCadastro: vi.fn(),
    disponibilizarRevisaoCadastro: vi.fn(),
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

const processosMock = {
    processoDetalhe: ref<any>({codigo: 999}),
    buscarProcessoDetalhe: vi.fn(),
};

const subprocessosStoreMock = {
    buscarSubprocessoDetalhe: vi.fn(),
};

vi.mock("@/composables/useProcessos", () => ({
    useProcessos: () => processosMock,
}));

vi.mock("@/composables/useSubprocessos", () => ({
    useSubprocessos: () => subprocessosStoreMock,
}));

describe("useFluxoSubprocesso", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        processosMock.processoDetalhe.value = {codigo: 999};
    });

    it("deve validar cadastro", async () => {
        const {validarCadastro} = await import("../useFluxoSubprocesso").then((m) => m.useFluxoSubprocesso());
        const {validarCadastro: serviceValidarCadastro} = await import("@/services/subprocessoService");
        (serviceValidarCadastro as any).mockResolvedValue({valido: true});

        const resultado = await validarCadastro(10);

        expect(serviceValidarCadastro).toHaveBeenCalledWith(10);
        expect(resultado).toEqual({valido: true});
    });

    it("deve disponibilizar cadastro e recarregar processo", async () => {
        const {disponibilizarCadastro} = await import("../useFluxoSubprocesso").then((m) => m.useFluxoSubprocesso());
        const {disponibilizarCadastro: serviceDisponibilizarCadastro} = await import("@/services/cadastroService");
        (serviceDisponibilizarCadastro as any).mockResolvedValue(undefined);

        const resultado = await disponibilizarCadastro(10);

        expect(resultado).toBe(true);
        expect(serviceDisponibilizarCadastro).toHaveBeenCalledWith(10);
        expect(processosMock.buscarProcessoDetalhe).toHaveBeenCalledWith(999);
    });

    it("deve homologar cadastro e recarregar subprocesso", async () => {
        const {homologarCadastro} = await import("../useFluxoSubprocesso").then((m) => m.useFluxoSubprocesso());
        const {homologarCadastro: serviceHomologarCadastro} = await import("@/services/cadastroService");
        (serviceHomologarCadastro as any).mockResolvedValue(undefined);

        const resultado = await homologarCadastro(10, {observacoes: "ok"});

        expect(resultado).toBe(true);
        expect(serviceHomologarCadastro).toHaveBeenCalledWith(10, {observacoes: "ok"});
        expect(subprocessosStoreMock.buscarSubprocessoDetalhe).toHaveBeenCalledWith(10);
    });

    it("deve alterar data limite e recarregar subprocesso", async () => {
        const {alterarDataLimiteSubprocesso} = await import("../useFluxoSubprocesso").then((m) => m.useFluxoSubprocesso());
        const {alterarDataLimiteSubprocesso: serviceAlterarDataLimite} = await import("@/services/processoService");
        (serviceAlterarDataLimite as any).mockResolvedValue(undefined);

        await alterarDataLimiteSubprocesso(10, {novaData: "2026-04-01"});

        expect(serviceAlterarDataLimite).toHaveBeenCalledWith(10, {novaData: "2026-04-01"});
        expect(processosMock.buscarProcessoDetalhe).toHaveBeenCalledWith(999);
        expect(subprocessosStoreMock.buscarSubprocessoDetalhe).toHaveBeenCalledWith(10);
    });

    it("deve retornar false e registrar erro ao reabrir cadastro com falha", async () => {
        const {reabrirCadastro, lastError} = await import("../useFluxoSubprocesso").then((m) => m.useFluxoSubprocesso());
        const {reabrirCadastro: serviceReabrirCadastro} = await import("@/services/processoService");
        const erro = new Error("Falha");
        (serviceReabrirCadastro as any).mockRejectedValue(erro);

        const resultado = await reabrirCadastro(10, "Justificativa");

        expect(resultado).toBe(false);
        expect(lastError.value).toEqual(normalizeError(erro));
    });
});
