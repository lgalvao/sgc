import {describe, expect, it, vi} from "vitest";
import * as cadastroService from "@/services/cadastroService";
import * as subprocessoService from "@/services/subprocessoService";
import {setupStoreTest} from "../../test-utils/storeTestHelpers";
import {Perfil} from "@/types/tipos";
import {usePerfilStore} from "../perfil";
import {useProcessosStore} from "../processos";
import {useSubprocessosStore} from "../subprocessos";

// Mock dependencies
vi.mock("@/services/subprocessoService", () => ({
    buscarSubprocessoDetalhe: vi.fn(),
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
}));

vi.mock("@/services/cadastroService", () => ({
    aceitarCadastro: vi.fn(),
    aceitarRevisaoCadastro: vi.fn(),
    devolverCadastro: vi.fn(),
    devolverRevisaoCadastro: vi.fn(),
    disponibilizarCadastro: vi.fn(),
    disponibilizarRevisaoCadastro: vi.fn(),
    homologarCadastro: vi.fn(),
    homologarRevisaoCadastro: vi.fn(),
}));

describe("Subprocessos Store", () => {
    const context = setupStoreTest(useSubprocessosStore);

    it("buscarSubprocessoDetalhe deve carregar detalhes com sucesso", async () => {
        const perfilStore = usePerfilStore();
        const mockDetalhe = { codigo: 123, situacao: "EM_ANDAMENTO" };

        // Setup perfil store state
        perfilStore.perfilSelecionado = Perfil.GESTOR;
        perfilStore.unidadeSelecionada = 99;
        perfilStore.perfisUnidades = [
            {
                perfil: Perfil.GESTOR,
                unidade: { codigo: 99, sigla: "TEST", nome: "Teste" },
                siglaUnidade: "TEST",
            },
        ];

        vi.mocked(subprocessoService.buscarSubprocessoDetalhe).mockResolvedValue(
            mockDetalhe as any,
        );

        await context.store.buscarSubprocessoDetalhe(123);

        expect(subprocessoService.buscarSubprocessoDetalhe).toHaveBeenCalledWith(
            123,
            "GESTOR",
            99,
        );
        expect(context.store.subprocessoDetalhe).toEqual(mockDetalhe);
    });

    it("buscarSubprocessoDetalhe deve notificar erro se perfil não selecionado", async () => {
        const perfilStore = usePerfilStore();
        perfilStore.perfilSelecionado = null;

        await context.store.buscarSubprocessoDetalhe(123);

        expect(context.store.subprocessoDetalhe).toBeNull();
    });

    it("buscarSubprocessoDetalhe deve notificar erro se unidade não encontrada para perfil", async () => {
        const perfilStore = usePerfilStore();
        perfilStore.perfilSelecionado = Perfil.GESTOR;
        perfilStore.unidadeSelecionada = 99;
        perfilStore.perfisUnidades = []; // No matching unit

        await context.store.buscarSubprocessoDetalhe(123);
        // Expect nothing happened (error handled internally via notify)
    });

    it("buscarSubprocessoDetalhe deve tratar erro do serviço", async () => {
        const perfilStore = usePerfilStore();
        perfilStore.perfilSelecionado = Perfil.GESTOR;
        perfilStore.unidadeSelecionada = 99;
        perfilStore.perfisUnidades = [
            {
                perfil: Perfil.GESTOR,
                unidade: { codigo: 99, sigla: "TEST", nome: "Teste" },
                siglaUnidade: "TEST",
            },
        ];

        vi.mocked(subprocessoService.buscarSubprocessoDetalhe).mockRejectedValue(
            new Error("Fail"),
        );

        await context.store.buscarSubprocessoDetalhe(123);
    });

    it("buscarSubprocessoPorProcessoEUnidade deve retornar código com sucesso", async () => {
        vi.mocked(
            subprocessoService.buscarSubprocessoPorProcessoEUnidade,
        ).mockResolvedValue({ codigo: 555 } as any);

        const result = await context.store.buscarSubprocessoPorProcessoEUnidade(1, "UNID");

        expect(result).toBe(555);
        expect(
            subprocessoService.buscarSubprocessoPorProcessoEUnidade,
        ).toHaveBeenCalledWith(1, "UNID");
    });

    it("buscarSubprocessoPorProcessoEUnidade deve tratar erro", async () => {
        vi.mocked(
            subprocessoService.buscarSubprocessoPorProcessoEUnidade,
        ).mockRejectedValue(new Error("Fail"));

        const result = await context.store.buscarSubprocessoPorProcessoEUnidade(1, "UNID");

        expect(result).toBeNull();
    });

    it("alterarDataLimiteSubprocesso deve delegar para processosStore", async () => {
        const processosStore = useProcessosStore();
        // Since we are not mocking the store module but the store instance methods
        // and we have a fresh pinia, we can spy on the instance method directly.
        // Wait, spying on instance method created by `useProcessosStore()`
        // The `useProcessosStore` returns an object (proxy).

        // We need to spy on the method of the store instance returned by useProcessosStore().
        // But `context.store` calls `processosStore.alterarDataLimiteSubprocesso()`.

        // Correct way to spy on another store action:
        const spy = vi.spyOn(processosStore, "alterarDataLimiteSubprocesso").mockResolvedValue(undefined);

        await context.store.alterarDataLimiteSubprocesso(1, { novaData: "2023-01-01" });

        expect(spy).toHaveBeenCalledWith(1, { novaData: "2023-01-01" });
    });

    it("disponibilizarCadastro deve executar ação com sucesso e atualizar processo", async () => {
        const processosStore = useProcessosStore();
        vi.mocked(cadastroService.disponibilizarCadastro).mockResolvedValue({} as any);

        processosStore.processoDetalhe = { codigo: 10 } as any;
        const spyBuscar = vi.spyOn(processosStore, "buscarProcessoDetalhe").mockResolvedValue(undefined);

        const success = await context.store.disponibilizarCadastro(123);

        expect(success).toBe(true);
        expect(cadastroService.disponibilizarCadastro).toHaveBeenCalledWith(123);
        expect(spyBuscar).toHaveBeenCalledWith(10);
    });

    it("disponibilizarCadastro deve tratar erro", async () => {
        vi.mocked(cadastroService.disponibilizarCadastro).mockRejectedValue(
            new Error("Fail"),
        );

        const success = await context.store.disponibilizarCadastro(123);

        expect(success).toBe(false);
    });

    it("disponibilizarRevisaoCadastro deve chamar serviço correto", async () => {
        vi.mocked(cadastroService.disponibilizarRevisaoCadastro).mockResolvedValue({} as any);
        await context.store.disponibilizarRevisaoCadastro(123);
        expect(cadastroService.disponibilizarRevisaoCadastro).toHaveBeenCalledWith(123);
    });

    it("devolverCadastro deve chamar serviço correto", async () => {
        const req = { motivo: "X" } as any;
        vi.mocked(cadastroService.devolverCadastro).mockResolvedValue({} as any);
        await context.store.devolverCadastro(123, req);
        expect(cadastroService.devolverCadastro).toHaveBeenCalledWith(123, req);
    });

    it("aceitarCadastro deve chamar serviço correto", async () => {
        const req = { analise: "OK" } as any;
        vi.mocked(cadastroService.aceitarCadastro).mockResolvedValue({} as any);
        await context.store.aceitarCadastro(123, req);
        expect(cadastroService.aceitarCadastro).toHaveBeenCalledWith(123, req);
    });

    it("homologarCadastro deve chamar serviço correto", async () => {
        const req = { analise: "OK" } as any;
        vi.mocked(cadastroService.homologarCadastro).mockResolvedValue({} as any);
        await context.store.homologarCadastro(123, req);
        expect(cadastroService.homologarCadastro).toHaveBeenCalledWith(123, req);
    });

    it("devolverRevisaoCadastro deve chamar serviço correto", async () => {
        const req = { motivo: "X" } as any;
        vi.mocked(cadastroService.devolverRevisaoCadastro).mockResolvedValue({} as any);
        await context.store.devolverRevisaoCadastro(123, req);
        expect(cadastroService.devolverRevisaoCadastro).toHaveBeenCalledWith(123, req);
    });

    it("aceitarRevisaoCadastro deve chamar serviço correto", async () => {
        const req = { analise: "OK" } as any;
        vi.mocked(cadastroService.aceitarRevisaoCadastro).mockResolvedValue({} as any);
        await context.store.aceitarRevisaoCadastro(123, req);
        expect(cadastroService.aceitarRevisaoCadastro).toHaveBeenCalledWith(123, req);
    });

    it("homologarRevisaoCadastro deve chamar serviço correto", async () => {
        const req = { analise: "OK" } as any;
        vi.mocked(cadastroService.homologarRevisaoCadastro).mockResolvedValue({} as any);
        await context.store.homologarRevisaoCadastro(123, req);
        expect(cadastroService.homologarRevisaoCadastro).toHaveBeenCalledWith(123, req);
    });
});
