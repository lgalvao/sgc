import {describe, expect, it} from "vitest";
import {analisarData} from "../parsing";

describe("analisarData", () => {
    describe("entradas nulas ou vazias", () => {
        it("retorna null para null", () => {
            expect(analisarData(null)).toBeNull();
        });

        it("retorna null para undefined", () => {
            expect(analisarData(undefined)).toBeNull();
        });

        it("retorna null para string vazia", () => {
            expect(analisarData("")).toBeNull();
        });
    });

    describe("objeto de contexto do BTable (com campo value)", () => {
        it("extrai e converte o campo value quando e string ISO", () => {
            const resultado = analisarData({value: "2024-03-15"});
            expect(resultado).toBeInstanceOf(Date);
            expect(resultado!.getFullYear()).toBe(2024);
            expect(resultado!.getMonth()).toBe(2); // março = 2 (0-indexed)
            expect(resultado!.getDate()).toBe(15);
        });

        it("retorna null quando value e string vazia", () => {
            expect(analisarData({value: ""})).toBeNull();
        });

        it("retorna null quando value e null", () => {
            expect(analisarData({value: null})).toBeNull();
        });
    });

    describe("entrada Date", () => {
        it("retorna a mesma data quando valida", () => {
            const data = new Date(2024, 0, 10);
            const resultado = analisarData(data);
            expect(resultado).toEqual(data);
        });

        it("retorna null para Date invalida", () => {
            expect(analisarData(new Date("invalido"))).toBeNull();
        });
    });

    describe("entrada number (timestamp)", () => {
        it("converte timestamp numerico para Date", () => {
            const ts = new Date(2024, 5, 1).getTime();
            const resultado = analisarData(ts);
            expect(resultado).toBeInstanceOf(Date);
            expect(resultado!.getFullYear()).toBe(2024);
        });
    });

    describe("entrada string", () => {
        it("converte string ISO (YYYY-MM-DD)", () => {
            const resultado = analisarData("2024-01-20");
            expect(resultado).toBeInstanceOf(Date);
            expect(resultado!.getFullYear()).toBe(2024);
            expect(resultado!.getMonth()).toBe(0);
            expect(resultado!.getDate()).toBe(20);
        });

        it("converte string ISO com hora", () => {
            const resultado = analisarData("2024-06-15T10:30:00");
            expect(resultado).toBeInstanceOf(Date);
            expect(resultado!.getFullYear()).toBe(2024);
        });

        it("converte string no formato dd/MM/yyyy", () => {
            const resultado = analisarData("25/12/2023");
            expect(resultado).toBeInstanceOf(Date);
            expect(resultado!.getFullYear()).toBe(2023);
            expect(resultado!.getMonth()).toBe(11); // dezembro = 11
            expect(resultado!.getDate()).toBe(25);
        });

        it("converte string de timestamp numerico (>= 10 digitos)", () => {
            const ts = new Date(2024, 3, 1).getTime();
            const resultado = analisarData(String(ts));
            expect(resultado).toBeInstanceOf(Date);
            expect(resultado!.getFullYear()).toBe(2024);
        });

        it("retorna null para string invalida", () => {
            expect(analisarData("nao-e-data")).toBeNull();
        });

        it("retorna null para string com apenas espacos", () => {
            expect(analisarData("   ")).toBeNull();
        });
    });

    describe("entrada array [ano, mes, dia, hora, minuto, segundo]", () => {
        it("converte array completo para Date", () => {
            const resultado = analisarData([2024, 8, 20, 14, 30, 0]);
            expect(resultado).toBeInstanceOf(Date);
            expect(resultado!.getFullYear()).toBe(2024);
            expect(resultado!.getMonth()).toBe(7); // agosto = 7 (mes 8, 0-indexed)
            expect(resultado!.getDate()).toBe(20);
            expect(resultado!.getHours()).toBe(14);
        });

        it("converte array sem hora/minuto/segundo (usa defaults 0)", () => {
            const resultado = analisarData([2023, 1, 5]);
            expect(resultado).toBeInstanceOf(Date);
            expect(resultado!.getFullYear()).toBe(2023);
            expect(resultado!.getMonth()).toBe(0); // janeiro = 0
            expect(resultado!.getDate()).toBe(5);
        });
    });

    describe("tipos nao suportados", () => {
        it("retorna null para boolean", () => {
            expect(analisarData(true)).toBeNull();
        });

        it("retorna null para objeto sem campo value", () => {
            expect(analisarData({outro: "campo"})).toBeNull();
        });
    });
});
