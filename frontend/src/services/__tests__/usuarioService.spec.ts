import { describe, expect, it, vi } from "vitest";
import { setupServiceTest, testGetEndpoint, testPostEndpoint, testErrorHandling } from "../../test-utils/serviceTestHelpers";
import * as mappers from "@/mappers/sgrh";
import * as service from "../usuarioService";

vi.mock("@/mappers/sgrh", () => ({
    mapPerfilUnidadeToFrontend: vi.fn((dto) => ({ ...dto, mapped: true })),
}));

describe("usuarioService", () => {
    const { mockApi } = setupServiceTest();
    const mockMappers = vi.mocked(mappers);

    describe("autenticar", () => {
        it("deve fazer POST e retornar booleano", async () => {
            const request = { tituloEleitoral: 123, senha: "123" };
            mockApi.post.mockResolvedValueOnce({ data: true });

            const result = await service.autenticar(request);

            expect(mockApi.post).toHaveBeenCalledWith("/usuarios/autenticar", request);
            expect(result).toBe(true);
        });

        testErrorHandling(() => service.autenticar({ tituloEleitoral: 123, senha: "123" }), 'post');
    });

    describe("autorizar", () => {
        it("deve fazer POST, mapear e retornar resposta", async () => {
            const tituloEleitoral = 123;
            const responseDto = [{ perfil: "CHEFE", unidade: "UNIT" }];
            mockApi.post.mockResolvedValue({ data: responseDto });

            const result = await service.autorizar(tituloEleitoral);

            expect(mockApi.post).toHaveBeenCalledWith(
                "/usuarios/autorizar",
                tituloEleitoral,
                {
                    headers: { "Content-Type": "application/json" },
                },
            );
            expect(mockMappers.mapPerfilUnidadeToFrontend).toHaveBeenCalled();
            expect(mockMappers.mapPerfilUnidadeToFrontend.mock.calls[0][0]).toEqual(
                responseDto[0],
            );
            expect(result[0]).toHaveProperty("mapped", true);
        });

        testErrorHandling(() => service.autorizar(123), 'post');
    });

    describe("entrar", () => {
        const request = {
            tituloEleitoral: 123,
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
});
