import {describe, expect, it} from "vitest";
import {analisarData} from "@/utils";

describe("analisarData - utilitário", () => {
    it("deve parsear ISO date string", () => {
        const d = analisarData("2025-10-01");
        expect(d).toBeInstanceOf(Date);
        expect(d!.getFullYear()).toBe(2025);
        expect(d!.getMonth()).toBe(9); // Outubro = 9
    });

    it("deve parsear ISO datetime string com timezone", () => {
        const d = analisarData("2025-10-01T12:34:56Z");
        expect(d).toBeInstanceOf(Date);
        expect(d!.getUTCFullYear()).toBe(2025);
    });

    it("deve parsear DD/MM/YYYY válido", () => {
        const d = analisarData("01/10/2025");
        expect(d).toBeInstanceOf(Date);
        expect(d!.getDate()).toBe(1);
        expect(d!.getMonth()).toBe(9);
    });

    it("deve retornar null para DD/MM/YYYY inválido", () => {
        const d = analisarData("31/02/2025");
        expect(d).toBeNull();
    });

    it("deve parsear timestamp numérico", () => {
        const ts = Date.UTC(2025, 9, 1);
        const d = analisarData(ts);
        expect(d).toBeInstanceOf(Date);
        expect(d!.getTime()).toBe(ts);
    });

    it("deve aceitar objeto Date válido", () => {
        const now = new Date();
        const d = analisarData(now);
        expect(d).toBeInstanceOf(Date);
        expect(d!.getTime()).toBe(now.getTime());
    });

    it("deve retornar null para entrada vazia", () => {
        expect(analisarData("")).toBeNull();
        expect(analisarData(null)).toBeNull();
        expect(analisarData(undefined)).toBeNull();
    });

    it("deve retornar null para string inválida", () => {
        expect(analisarData("not-a-date")).toBeNull();
    });
});
