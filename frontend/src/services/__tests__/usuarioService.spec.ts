import {describe, expect, it, vi} from "vitest";
import {setupServiceTest, testErrorHandling, testGetEndpoint, testPostEndpoint} from "@/test-utils/serviceTestHelpers";
import * as service from "../usuarioService";

describe("usuarioService", () => {
    const { mockApi } = setupServiceTest();

    describe("autenticar", () => {
        it("deve fazer POST e retornar booleano", async () => {
            const request = { tituloEleitoral: "123", senha: "123" };
            mockApi.post.mockResolvedValueOnce({ data: true });

            const result = await service.autenticar(request);

            expect(mockApi.post).toHaveBeenCalledWith("/usuarios/autenticar", request);
            expect(result).toBe(true);
        });

        testErrorHandling(() => service.autenticar({ tituloEleitoral: "123", senha: "123" }), 'post');
    });

    describe("autorizar", () => {
        it("deve fazer POST e retornar resposta", async () => {
            const tituloEleitoral = "123";
            const responseDto = [{ perfil: "CHEFE", unidade: { codigo: 1, nome: 'U', sigla: 'U' } }];
            mockApi.post.mockResolvedValue({ data: responseDto });

            const result = await service.autorizar(tituloEleitoral);

            expect(mockApi.post).toHaveBeenCalledWith(
                "/usuarios/autorizar",
                { tituloEleitoral },
            );
            expect(result[0].perfil).toBe("CHEFE");
        });

        testErrorHandling(() => service.autorizar("123"), 'post');
    });

    describe("entrar", () => {
        const request = {
            tituloEleitoral: "123",
            perfil: "GESTOR",
            unidadeCodigo: 1,
        };
        testPostEndpoint(
            () => service.entrar(request),
            "/usuarios/entrar",
            request
        );

        testErrorHandling(() => service.entrar(request), 'post');
    });

    describe("buscarTodosUsuarios", () => {
        const mockUsuarios = [{ codigo: 1, name: "Test User" }];
        testGetEndpoint(
            () => service.buscarTodosUsuarios(),
            "/usuarios",
            mockUsuarios
        );

        testErrorHandling(() => service.buscarTodosUsuarios(), 'get');
    });

    describe("buscarUsuariosPorUnidade", () => {
        const mockUsuarios = [{ codigo: 1, name: "Test User" }];
        testGetEndpoint(
            () => service.buscarUsuariosPorUnidade(1),
            "/unidades/1/usuarios",
            mockUsuarios
        );

        testErrorHandling(() => service.buscarUsuariosPorUnidade(1), 'get');
    });

    describe("buscarUsuarioPorTitulo", () => {
        const mockUsuario = { codigo: 1, name: "Test User", tituloEleitoral: "123" };
        testGetEndpoint(
            () => service.buscarUsuarioPorTitulo("123"),
            "/usuarios/123",
            mockUsuario
        );

        testErrorHandling(() => service.buscarUsuarioPorTitulo("123"), 'get');
    });
});
