import {describe, expect, it} from "vitest";
import {setupServiceTest, testErrorHandling} from "@/test-utils/serviceTestHelpers";
import * as service from "../painelService";

// Mock do axios no nível do arquivo ainda é necessário se a implementação importa diretamente
// Mas como setupServiceTest mocka o modulo, e aqui mockamos também, pode haver conflito?
// Se test-utils/serviceTestHelpers.ts faz vi.mock("@/axios-setup"), e este arquivo também faz...
// O último a ser executado vence? Ou conflitam?
// A recomendação é centralizar. Se eu uso setupServiceTest, deveria confiar nele.
// Mas ele exporta mockApi.

vi.mock("@/mappers/processos", () => ({
    mapProcessoResumoDtoToFrontend: vi.fn((dto) => ({...dto, mapped: true})),
}));
vi.mock("@/mappers/alertas", () => ({
    mapAlertaDtoToFrontend: vi.fn((dto) => ({...dto, mapped: true})),
}));

describe("painelService", () => {
    // Usando helper centralizado
    const {mockApi} = setupServiceTest();

    describe("listarProcessos", () => {
        it("deve buscar e mapear processos", async () => {
            const dtoList = [{codigo: 1, tipo: "MAPEAMENTO"}];
            const responseData = {
                content: dtoList,
                totalPages: 1,
                totalElements: 1,
                number: 0,
                size: 20,
                first: true,
                last: true,
                empty: false,
            };
            mockApi.get.mockResolvedValueOnce({data: responseData});

            const result = await service.listarProcessos("CHEFE", 1);

            expect(mockApi.get).toHaveBeenCalledWith("/painel/processos", {
                params: {perfil: "CHEFE", unidade: 1, page: 0, size: 20},
            });
            expect(result.content[0]).toEqual(dtoList[0]);
            expect(result.totalPages).toBe(1);
        });

        it("deve lidar com paginação diferente", async () => {
            mockApi.get.mockResolvedValueOnce({data: {content: []}});
            await service.listarProcessos("GESTOR", undefined, 2, 10);
            expect(mockApi.get).toHaveBeenCalledWith("/painel/processos", {
                params: {perfil: "GESTOR", unidade: undefined, page: 2, size: 10},
            });
        });

        it("deve lidar com ordenação", async () => {
            mockApi.get.mockResolvedValueOnce({data: {content: []}});
            await service.listarProcessos("GESTOR", undefined, 0, 10, "descricao", "desc");
            expect(mockApi.get).toHaveBeenCalledWith("/painel/processos", {
                params: {perfil: "GESTOR", unidade: undefined, page: 0, size: 10, sort: "descricao,desc"},
            });
        });

        testErrorHandling(() => service.listarProcessos("CHEFE"));
    });

    describe("listarAlertas", () => {
        it("deve buscar e mapear alertas", async () => {
            const dtoList = [{codigo: 1, mensagem: "Alerta DTO"}];
            const responseData = {
                content: dtoList,
                totalPages: 1,
                totalElements: 1,
                number: 0,
                size: 20,
                first: true,
                last: true,
                empty: false,
            };
            mockApi.get.mockResolvedValueOnce({data: responseData});

            const result = await service.listarAlertas("123", 1);

            expect(mockApi.get).toHaveBeenCalledWith("/painel/alertas", {
                params: {usuarioTitulo: "123", unidade: 1, page: 0, size: 20},
            });
            expect(result.content[0]).toEqual(dtoList[0]);
            expect(result.totalElements).toBe(1);
        });

        it("deve lidar com ordenação", async () => {
            mockApi.get.mockResolvedValueOnce({data: {content: []}});
            await service.listarAlertas("123", 1, 0, 10, "data", "asc");
            expect(mockApi.get).toHaveBeenCalledWith("/painel/alertas", {
                params: {usuarioTitulo: "123", unidade: 1, page: 0, size: 10, sort: "data,asc"},
            });
        });


        testErrorHandling(() => service.listarAlertas("123"));
    });
});
