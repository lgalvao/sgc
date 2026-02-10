import {describe, expect, it} from 'vitest';
import {
    diffInDays,
    ensureValidDate,
    formatDateBR,
    formatDateForInput,
    formatDateTimeBR,
    isDateValidAndFuture,
    parseDate
} from '@/utils/dateUtils';

describe('dateUtils', () => {
    describe('parseDate', () => {
        it('deve retornar null para entrada vazia', () => {
            expect(parseDate(null)).toBeNull();
            expect(parseDate(undefined)).toBeNull();
            expect(parseDate('')).toBeNull();
        });

        it('deve parsear objeto Date', () => {
            const d = new Date();
            expect(parseDate(d)).toEqual(d);
            expect(parseDate(new Date('invalid'))).toBeNull();
        });

        it('deve parsear número (timestamp)', () => {
            const ts = 1704110400000; // 2024-01-01 12:00:00 UTC
            expect(parseDate(ts)?.getFullYear()).toBe(2024);
        });

        it('deve parsear string ISO', () => {
            expect(parseDate('2024-01-01')?.getFullYear()).toBe(2024);
        });

        it('deve parsear string DD/MM/YYYY', () => {
            const d = parseDate('31/12/2023');
            expect(d?.getDate()).toBe(31);
            expect(d?.getMonth()).toBe(11);
            expect(d?.getFullYear()).toBe(2023);
        });

        it('deve parsear string numérica longa', () => {
            expect(parseDate('1704110400000')?.getFullYear()).toBe(2024);
        });

        it('deve retornar null para string inválida', () => {
            expect(parseDate('abc')).toBeNull();
        });
    });

    describe('formatDateBR', () => {
        it('deve formatar data corretamente', () => {
            const d = new Date(2024, 0, 1);
            expect(formatDateBR(d)).toBe('01/01/2024');
        });

        it('deve retornar "Não informado" se não houver data', () => {
            expect(formatDateBR(null)).toBe('Não informado');
        });

        it('deve retornar "Data inválida" se data for inválida', () => {
            expect(formatDateBR('invalid')).toBe('Data inválida');
        });
    });

    it('formatDateTimeBR deve incluir horas', () => {
        const d = new Date(2024, 0, 1, 15, 30);
        expect(formatDateTimeBR(d)).toContain('15:30');
    });

    it('formatDateForInput deve formatar YYYY-MM-DD', () => {
        const d = new Date(2024, 0, 1);
        expect(formatDateForInput(d)).toBe('2024-01-01');
        expect(formatDateForInput(null)).toBe('');
    });

    describe('isDateValidAndFuture', () => {
        it('deve retornar true para hoje ou futuro', () => {
            const today = new Date();
            const tomorrow = new Date();
            tomorrow.setDate(tomorrow.getDate() + 1);

            expect(isDateValidAndFuture(today)).toBe(true);
            expect(isDateValidAndFuture(tomorrow)).toBe(true);
        });

        it('deve retornar false para passado', () => {
            const yesterday = new Date();
            yesterday.setDate(yesterday.getDate() - 1);
            expect(isDateValidAndFuture(yesterday)).toBe(false);
        });

        it('deve retornar false para data inválida', () => {
            expect(isDateValidAndFuture('invalid')).toBe(false);
        });
    });

    it('diffInDays deve retornar diferença absoluta', () => {
        const d1 = new Date(2024, 0, 1);
        const d2 = new Date(2024, 0, 5);
        expect(diffInDays(d1, d2)).toBe(4);
        expect(diffInDays(d2, d1)).toBe(4);
    });

    it('ensureValidDate deve retornar null se inválido', () => {
        expect(ensureValidDate(null)).toBeNull();
        expect(ensureValidDate(new Date('invalid'))).toBeNull();
        const d = new Date();
        expect(ensureValidDate(d)).toBe(d);
    });
});
