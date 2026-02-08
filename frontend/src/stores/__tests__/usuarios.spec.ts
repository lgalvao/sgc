import {beforeEach, describe, expect, it, vi} from "vitest";
import * as usuarioService from "@/services/usuarioService";
import {setupStoreTest} from "@/test-utils/storeTestHelpers";
import type {Usuario} from "@/types/tipos";
import {useUsuariosStore} from "../usuarios";

const mockUsuarios: Usuario[] = [
    {
        codigo: 1,
        nome: "Ana Paula Souza",
        unidade: { codigo: 1, nome: "Seção de Seleção", sigla: "SESEL" },
        email: "ana.souza@tre-pe.jus.br",
        ramal: "1234",
        tituloEleitoral: "123456789",
    },
    {
        codigo: 2,
        nome: "Carlos Henrique Lima",
        unidade: { codigo: 2, nome: "Seção de Gestão de Pessoas", sigla: "SGP" },
        email: "carlos.lima@tre-pe.jus.br",
        ramal: "2345",
        tituloEleitoral: "987654321",
    },
];

vi.mock("@/services/usuarioService", () => ({
    buscarTodosUsuarios: vi.fn(() => Promise.resolve(mockUsuarios)),
    buscarUsuariosPorUnidade: vi.fn(),
    buscarUsuarioPorTitulo: vi.fn(),
}));

describe("useUsuariosStore", () => {
    const context = setupStoreTest(useUsuariosStore);

    beforeEach(() => {
        // Inicializa com dados simulados para testes de getters
        context.store.usuarios = mockUsuarios;
    });

    it("deve inicializar com usuários simulados", () => {
        expect(context.store.usuarios.length).toBe(2);
        expect(context.store.usuarios[0].tituloEleitoral).toBe("123456789");
    });

    describe("actions", () => {
        it("buscarUsuarios deve buscar e definir usuários", async () => {
            context.store.usuarios = [];
            await context.store.buscarUsuarios();
            expect(usuarioService.buscarTodosUsuarios).toHaveBeenCalledTimes(1);
            expect(context.store.usuarios.length).toBe(2);
        });

        it("buscarUsuarios deve lidar com erros", async () => {
            vi.mocked(usuarioService.buscarTodosUsuarios).mockRejectedValue(
                new Error("Failed"),
            );
            await expect(context.store.buscarUsuarios()).rejects.toThrow("Failed");
            expect(context.store.error).toContain("Failed");
        });

        it("buscarUsuariosPorUnidade deve buscar usuários da unidade", async () => {
            const mockUnidadeUsers = [mockUsuarios[0]];
            vi.mocked(usuarioService.buscarUsuariosPorUnidade).mockResolvedValue(mockUnidadeUsers);

            const result = await context.store.buscarUsuariosPorUnidade(10);

            expect(usuarioService.buscarUsuariosPorUnidade).toHaveBeenCalledWith(10);
            expect(result).toEqual(mockUnidadeUsers);
        });

        it("buscarUsuariosPorUnidade deve lidar com erros", async () => {
            vi.mocked(usuarioService.buscarUsuariosPorUnidade).mockRejectedValue(new Error("Unit fail"));
            await expect(context.store.buscarUsuariosPorUnidade(10)).rejects.toThrow("Unit fail");
            expect(context.store.error).toBe("Unit fail");
        });

        it("buscarUsuarioPorTitulo deve buscar usuário pelo título", async () => {
            vi.mocked(usuarioService.buscarUsuarioPorTitulo).mockResolvedValue(mockUsuarios[0]);

            const result = await context.store.buscarUsuarioPorTitulo("123456789");

            expect(usuarioService.buscarUsuarioPorTitulo).toHaveBeenCalledWith("123456789");
            expect(result).toEqual(mockUsuarios[0]);
        });

        it("buscarUsuarioPorTitulo deve lidar com erros", async () => {
            vi.mocked(usuarioService.buscarUsuarioPorTitulo).mockRejectedValue(new Error("Title fail"));
            await expect(context.store.buscarUsuarioPorTitulo("123")).rejects.toThrow("Title fail");
            expect(context.store.error).toBe("Title fail");
        });

        it("clearError deve limpar erros normalizados e a string de erro", () => {
            context.store.error = "Algum erro";
            context.store.lastError = { message: "Erro normalizado" } as any;

            context.store.clearError();

            expect(context.store.error).toBeNull();
            expect(context.store.lastError).toBeNull();
        });
    });

    describe("getters", () => {
        it("obterUsuarioPorTitulo deve retornar o usuário correto pelo título", () => {
            const usuario = context.store.obterUsuarioPorTitulo("123456789");
            expect(usuario).toBeDefined();
            expect(usuario?.tituloEleitoral).toBe("123456789");
            expect(usuario?.nome).toBe("Ana Paula Souza");
        });

        it("obterUsuarioPorTitulo deve retornar undefined se nenhum usuário correspondente for encontrado", () => {
            const usuario = context.store.obterUsuarioPorTitulo("999999999");
            expect(usuario).toBeUndefined();
        });

        it("obterUsuarioPorCodigo deve retornar o usuário correto pelo codigo", () => {
            const usuario = context.store.obterUsuarioPorCodigo(1);
            expect(usuario).toBeDefined();
            expect(usuario?.codigo).toBe(1);
            expect(usuario?.nome).toBe("Ana Paula Souza");
        });

        it("obterUsuarioPorCodigo deve retornar undefined se nenhum usuário correspondente for encontrado", () => {
            const usuario = context.store.obterUsuarioPorCodigo(999);
            expect(usuario).toBeUndefined();
        });
    });
});
