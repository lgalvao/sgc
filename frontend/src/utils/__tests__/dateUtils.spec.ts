import {describe, expect, it} from 'vitest';
import {
    analisarData,
    ehDataEstritamenteFutura,
    formatarDataBR,
    obterAmanhaFormatado,
    obterHojeFormatado
} from '../date';

describe('dateUtils', () => {
    it('obterAmanhaFormatado and obterHojeFormatado', () => {
        expect(obterAmanhaFormatado()).toMatch(/^\d{4}-\d{2}-\d{2}$/);
        expect(obterHojeFormatado()).toMatch(/^\d{4}-\d{2}-\d{2}$/);
    });

    it('analisarData covers multiple branches', () => {
        // 22: trimmed empty
        expect(analisarData("  ")).toBeNull();

        // 36: timestamp string
        const now = Date.now();
        expect(analisarData(now.toString())?.getTime()).toBe(now);
        expect(analisarData("invalid_timestamp")).toBeNull();

        // 58: number input
        expect(analisarData(now)?.getTime()).toBe(now);
        expect(analisarData(NaN)).toBeNull();

        // 69: array input
        expect(analisarData([2023, 1, 1])).toBeInstanceOf(Date);

        // 48: object with value (BTable context)
        expect(analisarData({value: "2023-01-01"})).toBeInstanceOf(Date);

        // 53: Date instance
        expect(analisarData(new Date("invalid"))).toBeNull();
    });

    it('formatarDataBR handles nulls and invalid dates', () => {
        expect(formatarDataBR(null)).toBe("Não informado");
        expect(formatarDataBR("invalid")).toBe("Data inválida");
    });

    it('ehDataEstritamenteFutura', () => {
        expect(ehDataEstritamenteFutura(null)).toBe(false);

        const today = new Date();
        expect(ehDataEstritamenteFutura(today)).toBe(false);

        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        expect(ehDataEstritamenteFutura(tomorrow)).toBe(true);
    });
});
