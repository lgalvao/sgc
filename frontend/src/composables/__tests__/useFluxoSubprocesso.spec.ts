import {beforeEach, describe, expect, it, vi} from "vitest";
import {useFluxoSubprocesso} from "../useFluxoSubprocesso";
import * as cadastroService from "@/services/cadastroService";
import * as subprocessoService from "@/services/subprocessoService";

vi.mock("@/services/cadastroService", () => ({
    aceitarCadastro: vi.fn(),
    aceitarRevisaoCadastro: vi.fn(),
    cancelarInicioRevisaoCadastro: vi.fn(),
    devolverCadastro: vi.fn(),
    devolverRevisaoCadastro: vi.fn(),
    disponibilizarCadastro: vi.fn(),
    disponibilizarRevisaoCadastro: vi.fn(),
    homologarCadastro: vi.fn(),
    homologarRevisaoCadastro: vi.fn(),
    iniciarRevisaoCadastro: vi.fn(),
}));

vi.mock("@/services/subprocessoService", () => ({
    alterarDataLimiteSubprocesso: vi.fn(),
    reabrirCadastro: vi.fn(),
    reabrirRevisaoCadastro: vi.fn(),
    validarCadastro: vi.fn(),
}));

vi.mock("@/composables/useErrorHandler", () => ({
    useErrorHandler: () => ({
        ultimoErro: {value: null},
        limparErro: vi.fn(),
        executarComTratamentoDeErros: vi.fn((cb) => cb())
    })
}));

const subprocessoStoreMock = {
    garantirContextoCadastroAtividades: vi.fn(),
    garantirContextoEdicao: vi.fn(),
    limparContextoAtual: vi.fn()
};

const atualizarFluxoMapaMock = vi.fn();

vi.mock("@/stores/subprocesso", () => ({
    useSubprocessoStore: () => subprocessoStoreMock
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
        atualizarFluxoMapa: atualizarFluxoMapaMock
    })
}));

describe("useFluxoSubprocesso", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        subprocessoStoreMock.garantirContextoCadastroAtividades.mockReset();
        subprocessoStoreMock.garantirContextoEdicao.mockReset();
        subprocessoStoreMock.limparContextoAtual.mockReset();
        atualizarFluxoMapaMock.mockReset();
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
        expect(atualizarFluxoMapaMock).toHaveBeenCalledWith(123);
        expect(subprocessoStoreMock.limparContextoAtual).toHaveBeenCalled();
    });

    it("deve iniciar revisão de cadastro", async () => {
        const {iniciarRevisaoCadastro} = useFluxoSubprocesso();
        vi.mocked(cadastroService.iniciarRevisaoCadastro).mockResolvedValue({} as any);

        const success = await iniciarRevisaoCadastro(123);

        expect(success).toBe(true);
        expect(cadastroService.iniciarRevisaoCadastro).toHaveBeenCalledWith(123);
        expect(subprocessoStoreMock.garantirContextoCadastroAtividades).toHaveBeenCalledWith(123, true);
    });

    it("deve cancelar início de revisão", async () => {
        const {cancelarInicioRevisaoCadastro} = useFluxoSubprocesso();
        vi.mocked(cadastroService.cancelarInicioRevisaoCadastro).mockResolvedValue({} as any);

        const success = await cancelarInicioRevisaoCadastro(123);

        expect(success).toBe(true);
        expect(cadastroService.cancelarInicioRevisaoCadastro).toHaveBeenCalledWith(123);
        expect(subprocessoStoreMock.garantirContextoCadastroAtividades).toHaveBeenCalledWith(123, true);
    });

    it("deve devolver cadastro", async () => {
        const {devolverCadastro} = useFluxoSubprocesso();
        const req = {justificativa: "Erro"} as any;
        vi.mocked(cadastroService.devolverCadastro).mockResolvedValue({} as any);

        const success = await devolverCadastro(123, req);

        expect(success).toBe(true);
        expect(cadastroService.devolverCadastro).toHaveBeenCalledWith(123, req);
        expect(atualizarFluxoMapaMock).toHaveBeenCalledWith(123);
    });

    it("deve aceitar cadastro", async () => {
        const {aceitarCadastro} = useFluxoSubprocesso();
        const req = {justificativa: "OK"} as any;
        vi.mocked(cadastroService.aceitarCadastro).mockResolvedValue({} as any);

        const success = await aceitarCadastro(123, req);

        expect(success).toBe(true);
        expect(cadastroService.aceitarCadastro).toHaveBeenCalledWith(123, req);
        expect(atualizarFluxoMapaMock).toHaveBeenCalledWith(123);
    });

    it("deve homologar cadastro", async () => {
        const {homologarCadastro} = useFluxoSubprocesso();
        const req = {} as any;
        vi.mocked(cadastroService.homologarCadastro).mockResolvedValue({} as any);

        const success = await homologarCadastro(123, req);

        expect(success).toBe(true);
        expect(cadastroService.homologarCadastro).toHaveBeenCalledWith(123, req);
        expect(atualizarFluxoMapaMock).toHaveBeenCalledWith(123);
    });

    it("não deve invalidar caches ao homologar cadastro sem redirecionar para o painel", async () => {
        const {homologarCadastro} = useFluxoSubprocesso();
        vi.mocked(cadastroService.homologarCadastro).mockResolvedValue({} as any);

        const success = await homologarCadastro(123, {} as any, {
            redirecionarParaPainel: false,
            redirecionarPara: {name: "Subprocesso", params: {codProcesso: 1, siglaUnidade: "TEST"}}
        });

        expect(success).toBe(true);
        expect(atualizarFluxoMapaMock).not.toHaveBeenCalled();
    });

    it("deve alterar data limite do subprocesso", async () => {
        const {alterarDataLimiteSubprocesso} = useFluxoSubprocesso();
        const dados = {novaData: "2024-12-31"};
        vi.mocked(subprocessoService.alterarDataLimiteSubprocesso).mockResolvedValue({} as any);

        await alterarDataLimiteSubprocesso(123, dados);

        expect(subprocessoService.alterarDataLimiteSubprocesso).toHaveBeenCalledWith(123, dados);
    });

    it("deve reabrir cadastro", async () => {
        const {reabrirCadastro} = useFluxoSubprocesso();
        vi.mocked(subprocessoService.reabrirCadastro).mockResolvedValue({} as any);

        const success = await reabrirCadastro(123, "justificativa");

        expect(success).toBe(true);
        expect(subprocessoService.reabrirCadastro).toHaveBeenCalledWith(123, "justificativa");
        expect(subprocessoStoreMock.garantirContextoEdicao).toHaveBeenCalledWith(123, true);
    });

    it("deve lidar com erro na ação de workflow", async () => {
        const {aceitarCadastro} = useFluxoSubprocesso();
        vi.mocked(cadastroService.aceitarCadastro).mockRejectedValue(new Error("Erro"));

        const success = await aceitarCadastro(123, {} as any);

        expect(success).toBe(false);
    });
});
