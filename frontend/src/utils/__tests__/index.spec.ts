import {describe, expect, it} from "vitest";
import {
    formatarDataBR,
    formatarDataParaInput,
    formatarDataHoraBR,
    ehDataValidaEFutura,
    analisarData,
} from "@/utils";

describe("utilitários", () => {
    describe("analisarData", () => {
        it("deve retornar null para entrada nula ou indefinida", () => {
            expect(analisarData(null)).toBeNull();
            expect(analisarData(undefined)).toBeNull();
            expect(analisarData("")).toBeNull();
        });

        it("deve analisar strings de data ISO", () => {
            const date = analisarData("2024-03-15");
            expect(date).toBeInstanceOf(Date);
            expect(date?.getFullYear()).toBe(2024);
            expect(date?.getMonth()).toBe(2);
            expect([14, 15]).toContain(date?.getDate());
        });

        it("deve analisar o formato de data brasileiro DD/MM/YYYY", () => {
            const date = analisarData("15/03/2024");
            expect(date).toEqual(new Date(2024, 2, 15));
        });

        it("deve retornar null para strings de data inválidas", () => {
            expect(analisarData("invalid")).toBeNull();
            expect(analisarData("99/99/9999")).toBeNull();
            expect(analisarData("00/01/2024")).toBeNull();
        });
    });

    describe("formatarDataBR", () => {
        it('deve retornar "Não informado" para nulo ou indefinido', () => {
            expect(formatarDataBR(null)).toBe("Não informado");
            expect(formatarDataBR(undefined)).toBe("Não informado");
        });

        it("deve formatar o objeto Date para o formato brasileiro", () => {
            const date = new Date(2024, 2, 15);
            expect(formatarDataBR(date)).toBe("15/03/2024");
        });

        it('deve retornar "Data inválida" para datas inválidas', () => {
            expect(formatarDataBR("invalid")).toBe("Data inválida");
            expect(formatarDataBR(new Date("invalid"))).toBe("Data inválida");
        });
    });

    describe("formatarDataParaInput", () => {
        it("deve retornar string vazia para nulo ou indefinido", () => {
            expect(formatarDataParaInput(null)).toBe("");
            expect(formatarDataParaInput(undefined)).toBe("");
        });

        it("deve formatar a data para o formato YYYY-MM-DD", () => {
            const date = new Date(2024, 2, 15);
            expect(formatarDataParaInput(date)).toBe("2024-03-15");
        });

        it("deve preencher meses e dias de um único dígito", () => {
            const date = new Date(2024, 0, 5);
            expect(formatarDataParaInput(date)).toBe("2024-01-05");
        });
    });

    describe("formatarDataHoraBR", () => {
        it("deve formatar data e hora no formato brasileiro", () => {
            const date = new Date(2024, 2, 15, 14, 30, 0);
            const result = formatarDataHoraBR(date);
            expect(result).toContain("15/03/2024");
            expect(result).toContain("14:30");
        });

        it("deve lidar com nulo e indefinido", () => {
            expect(formatarDataHoraBR(null)).toBe("Não informado");
            expect(formatarDataHoraBR(undefined)).toBe("Não informado");
        });
    });

    describe("ehDataValidaEFutura", () => {
        it("deve retornar false para nulo ou indefinido", () => {
            expect(ehDataValidaEFutura(null)).toBe(false);
            expect(ehDataValidaEFutura(undefined)).toBe(false);
        });

        it("deve retornar true para hoje", () => {
            const today = new Date();
            expect(ehDataValidaEFutura(today)).toBe(true);
        });

        it("deve retornar true para datas futuras", () => {
            const tomorrow = new Date();
            tomorrow.setDate(tomorrow.getDate() + 1);
            expect(ehDataValidaEFutura(tomorrow)).toBe(true);
        });

        it("deve retornar false para datas passadas", () => {
            const yesterday = new Date();
            yesterday.setDate(yesterday.getDate() - 1);
            expect(ehDataValidaEFutura(yesterday)).toBe(false);
        });
    });

    describe("tratamento de erro em formatarDataBR", () => {
        it('deve retornar "Data inválida" para strings de data inválidas', () => {
            // Testa com uma string de data inválida que fará com que analisarData falhe
            const result = formatarDataBR("invalid-date-string");
            expect(result).toBe("Data inválida");
        });
    });
});

describe("tratamento de erro em formatarDataParaInput", () => {
    it("deve retornar string vazia quando as operações de data geram um erro", () => {
        const invalidDate = new Date("invalid");

        // Isso deve acionar o bloco catch e retornar ''
        const result = formatarDataParaInput(invalidDate);
        expect(result).toBe("");
    });
});

describe("tratamento de erro em ehDataValidaEFutura", () => {
    it("deve retornar false quando as operações de data geram um erro", () => {
        const invalidDate = new Date("invalid");

        // Isso deve acionar o bloco catch e retornar false
        const result = ehDataValidaEFutura(invalidDate);
        expect(result).toBe(false);
    });
});
