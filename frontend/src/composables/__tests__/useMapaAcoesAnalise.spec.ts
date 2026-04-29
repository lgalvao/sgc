import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import {useMapaAcoesAnalise} from "../useMapaAcoesAnalise";
import * as processoService from "@/services/processoService";
import {TEXTOS} from "@/constants/textos";

vi.mock("@/services/processoService", () => ({
    aceitarValidacao: vi.fn(),
    devolverValidacao: vi.fn(),
    homologarValidacao: vi.fn(),
    validarMapa: vi.fn(),
}));

describe("useMapaAcoesAnalise", () => {
    const codSubprocesso = ref<number | null>(null);
    const acaoPrincipalMapa = ref<any>(null);
    const concluirAcaoPainel = vi.fn();
    const notify = vi.fn();

    beforeEach(() => {
        vi.clearAllMocks();
        codSubprocesso.value = 10;
        acaoPrincipalMapa.value = { codigo: "ACEITAR", mensagemSucesso: "Aceito!" };
    });

    it("deve inicializar com estados padrão", () => {
        const hooks = useMapaAcoesAnalise({ codSubprocesso, acaoPrincipalMapa, concluirAcaoPainel, notify });
        expect(hooks.mostrarModalAceitar.value).toBe(false);
        expect(hooks.mostrarModalValidar.value).toBe(false);
        expect(hooks.mostrarModalDevolucao.value).toBe(false);
        expect(hooks.observacaoDevolucao.value).toBe("");
        expect(hooks.isLoading.value).toBe(false);
    });

    describe("modais", () => {
        it("deve abrir e fechar modal aceitar", () => {
            const hooks = useMapaAcoesAnalise({ codSubprocesso, acaoPrincipalMapa, concluirAcaoPainel, notify });
            hooks.abrirModalAceitar();
            expect(hooks.mostrarModalAceitar.value).toBe(true);
            hooks.fecharModalAceitar();
            expect(hooks.mostrarModalAceitar.value).toBe(false);
        });

        it("deve abrir e fechar modal validar", () => {
            const hooks = useMapaAcoesAnalise({ codSubprocesso, acaoPrincipalMapa, concluirAcaoPainel, notify });
            hooks.abrirModalValidar();
            expect(hooks.mostrarModalValidar.value).toBe(true);
            hooks.fecharModalValidar();
            expect(hooks.mostrarModalValidar.value).toBe(false);
        });

        it("deve abrir e fechar modal devolucao", () => {
            const hooks = useMapaAcoesAnalise({ codSubprocesso, acaoPrincipalMapa, concluirAcaoPainel, notify });
            hooks.observacaoDevolucao.value = "teste";
            hooks.abrirModalDevolucao();
            expect(hooks.mostrarModalDevolucao.value).toBe(true);
            hooks.fecharModalDevolucao();
            expect(hooks.mostrarModalDevolucao.value).toBe(false);
            expect(hooks.observacaoDevolucao.value).toBe("");
        });
    });

    describe("confirmarValidacao", () => {
        it("deve validar mapa com sucesso", async () => {
            const hooks = useMapaAcoesAnalise({ codSubprocesso, acaoPrincipalMapa, concluirAcaoPainel, notify });
            await hooks.confirmarValidacao();
            expect(processoService.validarMapa).toHaveBeenCalledWith(10);
            expect(concluirAcaoPainel).toHaveBeenCalledWith(TEXTOS.sucesso.MAPA_VALIDADO_SUBMETIDO, expect.any(Function));
        });

        it("deve notificar erro ao falhar validacao", async () => {
            const hooks = useMapaAcoesAnalise({ codSubprocesso, acaoPrincipalMapa, concluirAcaoPainel, notify });
            vi.mocked(processoService.validarMapa).mockRejectedValue(new Error("Erro"));
            await hooks.confirmarValidacao();
            expect(notify).toHaveBeenCalledWith(TEXTOS.mapa.ERRO_VALIDAR, "danger");
        });

        it("não deve fazer nada se codSubprocesso for nulo", async () => {
            codSubprocesso.value = null;
            const hooks = useMapaAcoesAnalise({ codSubprocesso, acaoPrincipalMapa, concluirAcaoPainel, notify });
            await hooks.confirmarValidacao();
            expect(processoService.validarMapa).not.toHaveBeenCalled();
        });
    });

    describe("confirmarAceitacao", () => {
        it("deve aceitar validacao com sucesso", async () => {
            const hooks = useMapaAcoesAnalise({ codSubprocesso, acaoPrincipalMapa, concluirAcaoPainel, notify });
            await hooks.confirmarAceitacao("obs");
            expect(processoService.aceitarValidacao).toHaveBeenCalledWith(10, { texto: "obs" });
            expect(concluirAcaoPainel).toHaveBeenCalledWith("Aceito!", expect.any(Function));
        });

        it("deve homologar validacao com sucesso", async () => {
            acaoPrincipalMapa.value = { codigo: "HOMOLOGAR", mensagemSucesso: "Homologado!" };
            const hooks = useMapaAcoesAnalise({ codSubprocesso, acaoPrincipalMapa, concluirAcaoPainel, notify });
            await hooks.confirmarAceitacao("obs");
            expect(processoService.homologarValidacao).toHaveBeenCalledWith(10, { texto: "obs" });
            expect(concluirAcaoPainel).toHaveBeenCalledWith("Homologado!", expect.any(Function));
        });

        it("não deve fazer nada se acaoPrincipalMapa for nulo", async () => {
            acaoPrincipalMapa.value = null;
            const hooks = useMapaAcoesAnalise({ codSubprocesso, acaoPrincipalMapa, concluirAcaoPainel, notify });
            await hooks.confirmarAceitacao();
            expect(processoService.aceitarValidacao).not.toHaveBeenCalled();
        });

        it("deve notificar erro ao falhar aceitacao", async () => {
            const hooks = useMapaAcoesAnalise({ codSubprocesso, acaoPrincipalMapa, concluirAcaoPainel, notify });
            vi.mocked(processoService.aceitarValidacao).mockRejectedValue(new Error("Erro"));
            await hooks.confirmarAceitacao();
            expect(notify).toHaveBeenCalledWith(TEXTOS.comum.ERRO_OPERACAO, "danger");
        });
    });

    describe("confirmarDevolucao", () => {
        it("deve devolver validacao com sucesso", async () => {
            const hooks = useMapaAcoesAnalise({ codSubprocesso, acaoPrincipalMapa, concluirAcaoPainel, notify });
            hooks.observacaoDevolucao.value = "justificativa";
            await hooks.confirmarDevolucao();
            expect(processoService.devolverValidacao).toHaveBeenCalledWith(10, { justificativa: "justificativa" });
            expect(concluirAcaoPainel).toHaveBeenCalledWith(TEXTOS.sucesso.DEVOLUCAO_REALIZADA, expect.any(Function));
        });

        it("deve notificar erro ao falhar devolucao", async () => {
            const hooks = useMapaAcoesAnalise({ codSubprocesso, acaoPrincipalMapa, concluirAcaoPainel, notify });
            vi.mocked(processoService.devolverValidacao).mockRejectedValue(new Error("Erro"));
            await hooks.confirmarDevolucao();
            expect(notify).toHaveBeenCalledWith(TEXTOS.mapa.ERRO_DEVOLVER, "danger");
        });
    });
});
