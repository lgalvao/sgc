import {beforeEach, describe, expect, it, vi} from "vitest";
import * as usuarioService from "@/services/usuarioService";
import {initPinia} from "@/test-utils/helpers";
import type {Usuario} from "@/types/tipos";
import {useUsuariosStore} from "../usuarios";

const mockUsuarios: Usuario[] = [
    {
        codigo: 1,
        nome: "Ana Paula Souza",
        unidade: {codigo: 1, nome: "Seção de Seleção", sigla: "SESEL"},
        email: "ana.souza@tre-pe.jus.br",
        ramal: "1234",
        tituloEleitoral: "123456789",
    },
    {
        codigo: 2,
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
        expect(usuariosStore.usuarios[0].codigo).toBe(1);
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
        it("obterUsuarioPorId should return the correct usuario by ID", () => {
            const usuario = usuariosStore.obterUsuarioPorId(1);
            expect(usuario).toBeDefined();
            expect(usuario?.codigo).toBe(1);
            expect(usuario?.nome).toBe("Ana Paula Souza");
        });

        it("obterUsuarioPorId should return undefined if no matching usuario is found", () => {
            const usuario = usuariosStore.obterUsuarioPorId(999);
            expect(usuario).toBeUndefined();
        });
    });
});
