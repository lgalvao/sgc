import {describe, expect, it} from "vitest";
import {
    mapAtividadeDtoToModel,
    mapAtividadeVisualizacaoToModel,
    mapConhecimentoDtoToModel,
    mapConhecimentoVisualizacaoToModel,
    mapCriarAtividadeRequestToDto,
    mapCriarConhecimentoRequestToDto
} from "@/mappers/atividades";

describe("mappers/atividades.ts", () => {
    describe("mapAtividadeVisualizacaoToModel", () => {
        it("deve mapear corretamente DTO completo", () => {
            const dto = {
                codigo: 1,
                descricao: "Ativ 1",
                conhecimentos: [{codigo: 10, descricao: "Conh 1"}]
            };
            const result = mapAtividadeVisualizacaoToModel(dto);
            expect(result.codigo).toBe(1);
            expect(result.descricao).toBe("Ativ 1");
            expect(result.conhecimentos).toHaveLength(1);
            expect(result.conhecimentos[0].codigo).toBe(10);
        });

        it("deve lidar com conhecimentos vazios", () => {
            const dto = {codigo: 1, descricao: "Ativ 1"};
            const result = mapAtividadeVisualizacaoToModel(dto);
            expect(result.conhecimentos).toEqual([]);
        });

        it("deve retornar null se entrada for null", () => {
            const result = mapAtividadeVisualizacaoToModel(null);
            expect(result).toBeNull();
        });
    });

    describe("mapConhecimentoVisualizacaoToModel", () => {
        it("deve mapear corretamente", () => {
            const dto = {codigo: 10, descricao: "Conh 1"};
            const result = mapConhecimentoVisualizacaoToModel(dto);
            expect(result.codigo).toBe(10);
            expect(result.descricao).toBe("Conh 1");
        });

        it("deve retornar null se input null", () => {
            expect(mapConhecimentoVisualizacaoToModel(null)).toBeNull();
        });
    });

    describe("mapAtividadeDtoToModel", () => {
        it("deve mapear DTO de resposta", () => {
            const dto = {
                codigo: 1,
                descricao: "A",
                conhecimentos: [{codigo: 2, descricao: "C"}]
            };
            const result = mapAtividadeDtoToModel(dto);
            expect(result.codigo).toBe(1);
            expect(result.conhecimentos[0].codigo).toBe(2);
        });

        it("deve lidar com conhecimentos null", () => {
            const dto = {codigo: 1, descricao: "A", conhecimentos: null};
            const result = mapAtividadeDtoToModel(dto);
            expect(result.conhecimentos).toEqual([]);
        });

        it("deve retornar null se input null", () => {
            expect(mapAtividadeDtoToModel(null)).toBeNull();
        });
    });

    describe("mapConhecimentoDtoToModel", () => {
        it("deve mapear usando id", () => {
            const dto = {codigo: 5, descricao: "D"};
            expect(mapConhecimentoDtoToModel(dto).codigo).toBe(5);
        });

        it("deve mapear usando codigo (fallback)", () => {
            const dto = {codigo: 6, descricao: "E"};
            expect(mapConhecimentoDtoToModel(dto).codigo).toBe(6);
        });

        it("deve retornar null se input null", () => {
            expect(mapConhecimentoDtoToModel(null)).toBeNull();
        });
    });

    describe("mapCriarAtividadeRequestToDto", () => {
        it("deve adicionar mapaCodigo", () => {
            const req = {descricao: "Nova"};
            const result = mapCriarAtividadeRequestToDto(req, 99);
            expect(result.descricao).toBe("Nova");
            expect(result.mapaCodigo).toBe(99);
        });
    });

    describe("mapCriarConhecimentoRequestToDto", () => {
        it("deve formatar request", () => {
            const req = {descricao: "Novo C"};
            const result = mapCriarConhecimentoRequestToDto(req, 88);
            expect(result.descricao).toBe("Novo C");
            expect(result.atividadeCodigo).toBe(88);
        });
    });
});
