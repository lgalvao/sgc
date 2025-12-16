import {beforeEach, describe, expect, it, vi} from "vitest";
import * as usuarioService from "@/services/usuarioService";
import {initPinia} from "@/test-utils/helpers";
import type {Usuario} from "@/types/tipos";
import {useUsuariosStore} from "../usuarios";

const mockUsuarios: Usuario[] = [
    {
        nome: "Ana Paula Souza",
        unidade: {codigo: 1, nome: "Seção de Seleção", sigla: "SESEL"},
        email: "ana.souza@tre-pe.jus.br",
        ramal: "1234",
        tituloEleitoral: "123456789",
    },
    {
        nome: "Carlos Henrique Lima",
        unidade: {codigo: 2, nome: "Seção de Gestão de Pessoas", sigla: "SGP"},
        email: "carlos.lima@tre-pe.jus.br",
        ramal: "2345",
        tituloEleitoral: "987654321",
    },
];

vi.mock("@/services/usuarioService", () => ({
    buscarTodosUsuarios: vi.fn(() => Promise.resolve(mockUsuarios)),
}));

describe("useUsuariosStore", () => {
    let usuariosStore: ReturnType<typeof useUsuariosStore>;

    beforeEach(() => {
        initPinia();
        usuariosStore = useUsuariosStore();
        usuariosStore.usuarios = mockUsuarios;
        vi.clearAllMocks();
    });

    it("should initialize with mock usuarios", () => {
        expect(usuariosStore.usuarios.length).toBe(2);
        expect(usuariosStore.usuarios[0].tituloEleitoral).toBe("123456789");
    });

    describe("actions", () => {
        it("buscarUsuarios should fetch and set usuarios", async () => {
            usuariosStore.usuarios = [];
            await usuariosStore.buscarUsuarios();
            expect(usuarioService.buscarTodosUsuarios).toHaveBeenCalledTimes(1);
            expect(usuariosStore.usuarios.length).toBe(2);
        });

        it("buscarUsuarios should handle errors", async () => {
            vi.mocked(usuarioService.buscarTodosUsuarios).mockRejectedValue(
                new Error("Failed"),
            );
            await usuariosStore.buscarUsuarios();
            expect(usuariosStore.error).toContain("Failed");
        });
    });

    describe("getters", () => {
        it("obterUsuarioPorTitulo should return the correct usuario by titulo", () => {
            const usuario = usuariosStore.obterUsuarioPorTitulo("123456789");
            expect(usuario).toBeDefined();
            expect(usuario?.tituloEleitoral).toBe("123456789");
            expect(usuario?.nome).toBe("Ana Paula Souza");
        });

        it("obterUsuarioPorTitulo should return undefined if no matching usuario is found", () => {
            const usuario = usuariosStore.obterUsuarioPorTitulo("999999999");
            expect(usuario).toBeUndefined();
        });
    });
});
