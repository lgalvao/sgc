import {describe, expect, it} from 'vitest';
import {parseDate} from '@/utils';

describe('parseDate - utilitário', () => {
  it('deve parsear ISO date string', () => {
    const d = parseDate('2025-10-01');
    expect(d).toBeInstanceOf(Date);
    expect(d!.getFullYear()).toBe(2025);
    expect(d!.getMonth()).toBe(9); // Outubro = 9
  });

  it('deve parsear ISO datetime string com timezone', () => {
    const d = parseDate('2025-10-01T12:34:56Z');
    expect(d).toBeInstanceOf(Date);
    expect(d!.getUTCFullYear()).toBe(2025);
  });

  it('deve parsear DD/MM/YYYY válido', () => {
    const d = parseDate('01/10/2025');
    expect(d).toBeInstanceOf(Date);
    expect(d!.getDate()).toBe(1);
    expect(d!.getMonth()).toBe(9);
  });

  it('deve retornar null para DD/MM/YYYY inválido', () => {
    const d = parseDate('31/02/2025');
    expect(d).toBeNull();
  });

  it('deve parsear timestamp numérico', () => {
    const ts = Date.UTC(2025, 9, 1);
    const d = parseDate(ts);
    expect(d).toBeInstanceOf(Date);
    expect(d!.getTime()).toBe(ts);
  });

  it('deve aceitar objeto Date válido', () => {
    const now = new Date();
    const d = parseDate(now);
    expect(d).toBeInstanceOf(Date);
    expect(d!.getTime()).toBe(now.getTime());
  });

  it('deve retornar null para entrada vazia', () => {
    expect(parseDate('')).toBeNull();
    expect(parseDate(null)).toBeNull();
    expect(parseDate(undefined)).toBeNull();
  });

  it('deve retornar null para string inválida', () => {
    expect(parseDate('not-a-date')).toBeNull();
  });
});