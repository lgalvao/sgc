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

    it("deve disponibilizar revisao cadastro", async () => {
        const {disponibilizarRevisaoCadastro} = await import("../useFluxoSubprocesso").then((m) => m.useFluxoSubprocesso());
        const {disponibilizarRevisaoCadastro: service} = await import("@/services/cadastroService");
        (service as any).mockResolvedValue(undefined);

        await disponibilizarRevisaoCadastro(10);
        expect(service).toHaveBeenCalledWith(10);
    });

    it("deve devolver cadastro", async () => {
        const {devolverCadastro} = await import("../useFluxoSubprocesso").then((m) => m.useFluxoSubprocesso());
        const {devolverCadastro: service} = await import("@/services/cadastroService");
        (service as any).mockResolvedValue(undefined);

        await devolverCadastro(10, {observacoes: "Erro"});
        expect(service).toHaveBeenCalledWith(10, {observacoes: "Erro"});
    });

    it("deve devolver revisao cadastro", async () => {
        const {devolverRevisaoCadastro} = await import("../useFluxoSubprocesso").then((m) => m.useFluxoSubprocesso());
        const {devolverRevisaoCadastro: service} = await import("@/services/cadastroService");
        (service as any).mockResolvedValue(undefined);

        await devolverRevisaoCadastro(10, {observacoes: "Erro"});
        expect(service).toHaveBeenCalledWith(10, {observacoes: "Erro"});
    });

    it("deve aceitar cadastro", async () => {
        const {aceitarCadastro} = await import("../useFluxoSubprocesso").then((m) => m.useFluxoSubprocesso());
        const {aceitarCadastro: service} = await import("@/services/cadastroService");
        (service as any).mockResolvedValue(undefined);

        await aceitarCadastro(10, {observacoes: "OK"});
        expect(service).toHaveBeenCalledWith(10, {observacoes: "OK"});
    });

    it("deve aceitar revisao cadastro", async () => {
        const {aceitarRevisaoCadastro} = await import("../useFluxoSubprocesso").then((m) => m.useFluxoSubprocesso());
        const {aceitarRevisaoCadastro: service} = await import("@/services/cadastroService");
        (service as any).mockResolvedValue(undefined);

        await aceitarRevisaoCadastro(10, {observacoes: "OK"});
        expect(service).toHaveBeenCalledWith(10, {observacoes: "OK"});
    });

    it("deve homologar revisao cadastro", async () => {
        const {homologarRevisaoCadastro} = await import("../useFluxoSubprocesso").then((m) => m.useFluxoSubprocesso());
        const {homologarRevisaoCadastro: service} = await import("@/services/cadastroService");
        (service as any).mockResolvedValue(undefined);

        await homologarRevisaoCadastro(10, {observacoes: "OK"});
        expect(service).toHaveBeenCalledWith(10, {observacoes: "OK"});
    });

    it("deve reabrir revisao cadastro", async () => {
        const {reabrirRevisaoCadastro} = await import("../useFluxoSubprocesso").then((m) => m.useFluxoSubprocesso());
        const {reabrirRevisaoCadastro: service} = await import("@/services/processoService");
        (service as any).mockResolvedValue(undefined);

        await reabrirRevisaoCadastro(10, "Justificativa");
        expect(service).toHaveBeenCalledWith(10, "Justificativa");
    });

    it("não deve recarregar processo se processoDetalhe for nulo", async () => {
        processosMock.processoDetalhe.value = null;
        const {disponibilizarCadastro} = await import("../useFluxoSubprocesso").then((m) => m.useFluxoSubprocesso());
        const {disponibilizarCadastro: service} = await import("@/services/cadastroService");
        (service as any).mockResolvedValue(undefined);

        await disponibilizarCadastro(10);
        expect(processosMock.buscarProcessoDetalhe).not.toHaveBeenCalled();
    });
});
