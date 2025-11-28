import {createPinia, setActivePinia} from "pinia";
import {beforeEach, describe, expect, it, vi} from "vitest";
import * as cadastroService from "@/services/cadastroService";
import * as subprocessoService from "@/services/subprocessoService";
import {Perfil} from "@/types/tipos";
import {useNotificacoesStore} from "../notificacoes";
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
    beforeEach(() => {
        setActivePinia(createPinia());
        vi.restoreAllMocks();
    });

    it("buscarSubprocessoDetalhe deve carregar detalhes com sucesso", async () => {
        const store = useSubprocessosStore();
        const perfilStore = usePerfilStore();
        const mockDetalhe = {codigo: 123, situacao: "EM_ANDAMENTO"};

        // Setup perfil store state
        perfilStore.perfilSelecionado = Perfil.GESTOR;
        perfilStore.unidadeSelecionada = 99;
        perfilStore.perfisUnidades = [
            {
                perfil: Perfil.GESTOR,
                unidade: {codigo: 99, sigla: "TEST", nome: "Teste"},
                siglaUnidade: "TEST",
            },
        ];

        vi.mocked(subprocessoService.buscarSubprocessoDetalhe).mockResolvedValue(
            mockDetalhe as any,
        );

        await store.buscarSubprocessoDetalhe(123);

        expect(subprocessoService.buscarSubprocessoDetalhe).toHaveBeenCalledWith(
            123,
            "GESTOR",
            99,
        );
        expect(store.subprocessoDetalhe).toEqual(mockDetalhe);
    });

    it("buscarSubprocessoDetalhe deve notificar erro se perfil não selecionado", async () => {
        const store = useSubprocessosStore();
        const perfilStore = usePerfilStore();
        const notificacoesStore = useNotificacoesStore();
        vi.spyOn(notificacoesStore, "erro");

        // Perfil not selected
        perfilStore.perfilSelecionado = null;

        await store.buscarSubprocessoDetalhe(123);

        expect(notificacoesStore.erro).toHaveBeenCalledWith(
            "Erro ao buscar detalhes do subprocesso",
            "Informações de perfil ou unidade não disponíveis.",
        );
        expect(store.subprocessoDetalhe).toBeNull();
    });

    it("buscarSubprocessoDetalhe deve notificar erro se unidade não encontrada para perfil", async () => {
        const store = useSubprocessosStore();
        const perfilStore = usePerfilStore();
        const notificacoesStore = useNotificacoesStore();
        vi.spyOn(notificacoesStore, "erro");

        perfilStore.perfilSelecionado = Perfil.GESTOR;
        perfilStore.unidadeSelecionada = 99;
        perfilStore.perfisUnidades = []; // No matching unit

        await store.buscarSubprocessoDetalhe(123);

        expect(notificacoesStore.erro).toHaveBeenCalledWith(
            "Erro ao buscar detalhes do subprocesso",
            "Informações de perfil ou unidade não disponíveis.",
        );
    });

    it("buscarSubprocessoDetalhe deve tratar erro do serviço", async () => {
        const store = useSubprocessosStore();
        const perfilStore = usePerfilStore();
        const notificacoesStore = useNotificacoesStore();
        vi.spyOn(notificacoesStore, "erro");

        perfilStore.perfilSelecionado = Perfil.GESTOR;
        perfilStore.unidadeSelecionada = 99;
        perfilStore.perfisUnidades = [
            {
                perfil: Perfil.GESTOR,
                unidade: {codigo: 99, sigla: "TEST", nome: "Teste"},
                siglaUnidade: "TEST",
            },
        ];

        vi.mocked(subprocessoService.buscarSubprocessoDetalhe).mockRejectedValue(
            new Error("Fail"),
        );

        await store.buscarSubprocessoDetalhe(123);

        expect(notificacoesStore.erro).toHaveBeenCalledWith(
            "Erro ao buscar detalhes do subprocesso",
            "Não foi possível carregar as informações.",
        );
    });

    it("buscarSubprocessoPorProcessoEUnidade deve retornar código com sucesso", async () => {
        const store = useSubprocessosStore();
        vi.mocked(
            subprocessoService.buscarSubprocessoPorProcessoEUnidade,
        ).mockResolvedValue({codigo: 555} as any);

        const result = await store.buscarSubprocessoPorProcessoEUnidade(1, "UNID");

        expect(result).toBe(555);
        expect(
            subprocessoService.buscarSubprocessoPorProcessoEUnidade,
        ).toHaveBeenCalledWith(1, "UNID");
    });

    it("buscarSubprocessoPorProcessoEUnidade deve tratar erro", async () => {
        const store = useSubprocessosStore();
        const notificacoesStore = useNotificacoesStore();
        vi.spyOn(notificacoesStore, "erro");
        vi.mocked(
            subprocessoService.buscarSubprocessoPorProcessoEUnidade,
        ).mockRejectedValue(new Error("Fail"));

        const result = await store.buscarSubprocessoPorProcessoEUnidade(1, "UNID");

        expect(result).toBeNull();
        expect(notificacoesStore.erro).toHaveBeenCalledWith(
            "Erro",
            "Não foi possível encontrar o subprocesso para esta unidade.",
        );
    });

    it("alterarDataLimiteSubprocesso deve delegar para processosStore", async () => {
        const store = useSubprocessosStore();
        const processosStore = useProcessosStore();

        vi.spyOn(processosStore, "alterarDataLimiteSubprocesso").mockResolvedValue(
            undefined,
        );

        await store.alterarDataLimiteSubprocesso(1, {novaData: "2023-01-01"});

        expect(processosStore.alterarDataLimiteSubprocesso).toHaveBeenCalledWith(
            1,
            {novaData: "2023-01-01"},
        );
    });

    it("disponibilizarCadastro deve executar ação com sucesso e atualizar processo", async () => {
        const store = useSubprocessosStore();
        const notificacoesStore = useNotificacoesStore();
        const processosStore = useProcessosStore();
        vi.spyOn(notificacoesStore, "sucesso");

        vi.mocked(cadastroService.disponibilizarCadastro).mockResolvedValue(
            {} as any,
        );

        processosStore.processoDetalhe = {codigo: 10} as any;
        vi.spyOn(processosStore, "buscarProcessoDetalhe").mockResolvedValue(
            undefined,
        );

        const success = await store.disponibilizarCadastro(123);

        expect(success).toBe(true);
        expect(cadastroService.disponibilizarCadastro).toHaveBeenCalledWith(123);
        expect(notificacoesStore.sucesso).toHaveBeenCalledWith(
            "Cadastro disponibilizado",
            "Cadastro disponibilizado.",
        );
        expect(processosStore.buscarProcessoDetalhe).toHaveBeenCalledWith(10);
    });

    it("disponibilizarCadastro deve tratar erro", async () => {
        const store = useSubprocessosStore();
        const notificacoesStore = useNotificacoesStore();
        vi.spyOn(notificacoesStore, "erro");

        vi.mocked(cadastroService.disponibilizarCadastro).mockRejectedValue(
            new Error("Fail"),
        );

        const success = await store.disponibilizarCadastro(123);

        expect(success).toBe(false);
        expect(notificacoesStore.erro).toHaveBeenCalledWith(
            "Erro ao disponibilizar",
            "Não foi possível concluir a ação: Erro ao disponibilizar.",
        );
    });

    it("disponibilizarRevisaoCadastro deve chamar serviço correto", async () => {
        const store = useSubprocessosStore();
        vi.mocked(cadastroService.disponibilizarRevisaoCadastro).mockResolvedValue(
            {} as any,
        );
        await store.disponibilizarRevisaoCadastro(123);
        expect(cadastroService.disponibilizarRevisaoCadastro).toHaveBeenCalledWith(
            123,
        );
    });

    it("devolverCadastro deve chamar serviço correto", async () => {
        const store = useSubprocessosStore();
        const req = {motivo: "X"} as any;
        vi.mocked(cadastroService.devolverCadastro).mockResolvedValue({} as any);
        await store.devolverCadastro(123, req);
        expect(cadastroService.devolverCadastro).toHaveBeenCalledWith(123, req);
    });

    it("aceitarCadastro deve chamar serviço correto", async () => {
        const store = useSubprocessosStore();
        const req = {analise: "OK"} as any;
        vi.mocked(cadastroService.aceitarCadastro).mockResolvedValue({} as any);
        await store.aceitarCadastro(123, req);
        expect(cadastroService.aceitarCadastro).toHaveBeenCalledWith(123, req);
    });

    it("homologarCadastro deve chamar serviço correto", async () => {
        const store = useSubprocessosStore();
        const req = {analise: "OK"} as any;
        vi.mocked(cadastroService.homologarCadastro).mockResolvedValue({} as any);
        await store.homologarCadastro(123, req);
        expect(cadastroService.homologarCadastro).toHaveBeenCalledWith(123, req);
    });

    it("devolverRevisaoCadastro deve chamar serviço correto", async () => {
        const store = useSubprocessosStore();
        const req = {motivo: "X"} as any;
        vi.mocked(cadastroService.devolverRevisaoCadastro).mockResolvedValue(
            {} as any,
        );
        await store.devolverRevisaoCadastro(123, req);
        expect(cadastroService.devolverRevisaoCadastro).toHaveBeenCalledWith(
            123,
            req,
        );
    });

    it("aceitarRevisaoCadastro deve chamar serviço correto", async () => {
        const store = useSubprocessosStore();
        const req = {analise: "OK"} as any;
        vi.mocked(cadastroService.aceitarRevisaoCadastro).mockResolvedValue(
            {} as any,
        );
        await store.aceitarRevisaoCadastro(123, req);
        expect(cadastroService.aceitarRevisaoCadastro).toHaveBeenCalledWith(
            123,
            req,
        );
    });

    it("homologarRevisaoCadastro deve chamar serviço correto", async () => {
        const store = useSubprocessosStore();
        const req = {analise: "OK"} as any;
        vi.mocked(cadastroService.homologarRevisaoCadastro).mockResolvedValue(
            {} as any,
        );
        await store.homologarRevisaoCadastro(123, req);
        expect(cadastroService.homologarRevisaoCadastro).toHaveBeenCalledWith(
            123,
            req,
        );
    });
});
