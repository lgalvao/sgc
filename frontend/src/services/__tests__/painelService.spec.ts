import {describe, expect, it, vi} from "vitest";
import {setupServiceTest, testErrorHandling} from "@/test-utils/serviceTestHelpers";
import * as service from "../painelService";

vi.mock("@/mappers/processos", () => ({
    mapProcessoResumoDtoToFrontend: vi.fn((dto) => ({...dto, mapped: true})),
}));
vi.mock("@/mappers/alertas", () => ({
    mapAlertaDtoToFrontend: vi.fn((dto) => ({...dto, mapped: true})),
}));

describe("painelService", () => {
    const {mockApi} = setupServiceTest();

    describe("listarProcessos", () => {
        it("deve buscar e mapear processos sem enviar o perfil", async () => {
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

            const result = await service.listarProcessos(1);

            expect(mockApi.get).toHaveBeenCalledWith("/painel/processos", {
                params: {unidade: 1, page: 0, size: 20},
            });
            expect(result.content[0]).toEqual(dtoList[0]);
        });

        it("deve lidar com paginação", async () => {
            mockApi.get.mockResolvedValueOnce({data: {content: []}});
            await service.listarProcessos(undefined, 2, 10);
            expect(mockApi.get).toHaveBeenCalledWith("/painel/processos", {
                params: {page: 2, size: 10},
            });
        });

        it("deve lidar com ordenação", async () => {
            mockApi.get.mockResolvedValueOnce({data: {content: []}});
            await service.listarProcessos(undefined, 0, 10, "descricao", "desc");
            expect(mockApi.get).toHaveBeenCalledWith("/painel/processos", {
                params: {page: 0, size: 10, sort: "descricao,desc"},
            });
        });

        testErrorHandling(() => service.listarProcessos(1));
    });

    describe("listarAlertas", () => {
        it("deve buscar e mapear alertas sem enviar título do usuário", async () => {
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

            const result = await service.listarAlertas(1);

            expect(mockApi.get).toHaveBeenCalledWith("/painel/alertas", {
                params: {unidade: 1, page: 0, size: 20},
            });
            expect(result.content[0]).toEqual(dtoList[0]);
        });

        it("deve lidar com ordenação", async () => {
            mockApi.get.mockResolvedValueOnce({data: {content: []}});
            await service.listarAlertas(1, 0, 10, "dataHora", "asc");
            expect(mockApi.get).toHaveBeenCalledWith("/painel/alertas", {
                params: {unidade: 1, page: 0, size: 10, sort: "dataHora,asc"},
            });
        });

        testErrorHandling(() => service.listarAlertas(1));
    });
});
