import {describe, expect, it} from 'vitest';
import {
    formatDateBR,
    isDateStrictlyFuture,
    isDateValidAndFuture,
    obterAmanhaFormatado,
    obterHojeFormatado,
    parseDate
} from '../dateUtils';

describe('dateUtils', () => {
  it('obterAmanhaFormatado and obterHojeFormatado', () => {
    expect(obterAmanhaFormatado()).toMatch(/^\d{4}-\d{2}-\d{2}$/);
    expect(obterHojeFormatado()).toMatch(/^\d{4}-\d{2}-\d{2}$/);
  });

  it('parseDate covers multiple branches', () => {
    // 22: trimmed empty
    expect(parseDate("  ")).toBeNull();
    
    // 36: timestamp string
    const now = Date.now();
    expect(parseDate(now.toString())?.getTime()).toBe(now);
    expect(parseDate("invalid_timestamp")).toBeNull();

    // 58: number input
    expect(parseDate(now)?.getTime()).toBe(now);
    expect(parseDate(NaN)).toBeNull();

    // 69: array input
    expect(parseDate([2023, 1, 1])).toBeInstanceOf(Date);

    // 48: object with value (BTable context)
    expect(parseDate({ value: "2023-01-01" })).toBeInstanceOf(Date);
    
    // 53: Date instance
    expect(parseDate(new Date("invalid"))).toBeNull();
  });

  it('formatDateBR handles nulls and invalid dates', () => {
    expect(formatDateBR(null)).toBe("Não informado");
    expect(formatDateBR("invalid")).toBe("Data inválida");
  });

  it('isDateValidAndFuture and isDateStrictlyFuture', () => {
    expect(isDateValidAndFuture(null)).toBe(false);
    expect(isDateStrictlyFuture(null)).toBe(false);
    
    const today = new Date();
    expect(isDateValidAndFuture(today)).toBe(true);
    expect(isDateStrictlyFuture(today)).toBe(false);
    
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    expect(isDateStrictlyFuture(tomorrow)).toBe(true);
  });
});
