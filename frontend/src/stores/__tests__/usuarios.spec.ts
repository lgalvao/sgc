import { describe, expect, it, vi, beforeEach } from "vitest";
import * as usuarioService from "@/services/usuarioService";
import { setupStoreTest } from "../../test-utils/storeTestHelpers";
import type { Usuario } from "@/types/tipos";
import { useUsuariosStore } from "../usuarios";

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
            await context.store.buscarUsuarios();
            expect(context.store.error).toContain("Failed");
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

        it("obterUsuarioPorId deve retornar o usuário correto pelo id", () => {
            const usuario = context.store.obterUsuarioPorId(1);
            expect(usuario).toBeDefined();
            expect(usuario?.codigo).toBe(1);
            expect(usuario?.nome).toBe("Ana Paula Souza");
        });

        it("obterUsuarioPorId deve retornar undefined se nenhum usuário correspondente for encontrado", () => {
            const usuario = context.store.obterUsuarioPorId(999);
            expect(usuario).toBeUndefined();
        });
    });
});
