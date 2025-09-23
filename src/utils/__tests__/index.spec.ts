import {describe, expect, it} from 'vitest';
import {
    badgeClass,
    diffInDays,
    ensureValidDate,
    formatDateBR,
    formatDateForInput,
    formatDateTimeBR,
    generateUniqueId,
    iconeTipo,
    isDateValidAndFuture,
    parseDate
} from '@/utils';

describe('utils', () => {
    describe('generateUniqueId', () => {
        it('should generate unique IDs', () => {
            const id1 = generateUniqueId();
            const id2 = generateUniqueId();
            expect(id1).not.toBe(id2);
            expect(typeof id1).toBe('number');
            expect(typeof id2).toBe('number');
        });
    });

    describe('badgeClass', () => {
        it('should return correct badge class for known situations', () => {
            expect(badgeClass('Finalizado')).toBe('bg-success');
            expect(badgeClass('Em andamento')).toBe('bg-warning text-dark');
        });

        it('should return default class for unknown situations', () => {
            expect(badgeClass('unknown')).toBe('bg-secondary');
        });
    });

    describe('iconeTipo', () => {
        it('should return correct icons for notification types', () => {
            expect(iconeTipo('success')).toBe('bi bi-check-circle-fill text-success');
            expect(iconeTipo('error')).toBe('bi bi-exclamation-triangle-fill text-danger');
            expect(iconeTipo('warning')).toBe('bi bi-exclamation-triangle-fill text-warning');
            expect(iconeTipo('info')).toBe('bi bi-info-circle-fill text-info');
            expect(iconeTipo('email')).toBe('bi bi-envelope-fill text-primary');
        });

        it('should return default icon for unknown types', () => {
            expect(iconeTipo('unknown' as 'success')).toBe('bi bi-bell-fill');
        });
    });

    describe('parseDate', () => {
        it('should return null for null or undefined input', () => {
            expect(parseDate(null)).toBeNull();
            expect(parseDate(undefined)).toBeNull();
            expect(parseDate('')).toBeNull();
        });

        it('should parse ISO date strings', () => {
            const date = parseDate('2024-03-15');
            expect(date).toBeInstanceOf(Date);
            expect(date?.getFullYear()).toBe(2024);
            expect(date?.getMonth()).toBe(2);
            expect([14, 15]).toContain(date?.getDate());
        });

        it('should parse Brazilian date format DD/MM/YYYY', () => {
            const date = parseDate('15/03/2024');
            expect(date).toEqual(new Date(2024, 2, 15));
        });

        it('should return null for invalid date strings', () => {
            expect(parseDate('invalid')).toBeNull();
            expect(parseDate('99/99/9999')).toBeNull();
            expect(parseDate('00/01/2024')).toBeNull();
        });
    });

    describe('formatDateBR', () => {
        it('should return "Não informado" for null or undefined', () => {
            expect(formatDateBR(null)).toBe('Não informado');
            expect(formatDateBR(undefined)).toBe('Não informado');
        });

        it('should format Date object to Brazilian format', () => {
            const date = new Date(2024, 2, 15);
            expect(formatDateBR(date)).toBe('15/03/2024');
        });

        it('should return "Data inválida" for invalid dates', () => {
            expect(formatDateBR('invalid')).toBe('Data inválida');
            expect(formatDateBR(new Date('invalid'))).toBe('Data inválida');
        });
    });

    describe('formatDateForInput', () => {
        it('should return empty string for null or undefined', () => {
            expect(formatDateForInput(null)).toBe('');
            expect(formatDateForInput(undefined)).toBe('');
        });

        it('should format Date to YYYY-MM-DD format', () => {
            const date = new Date(2024, 2, 15);
            expect(formatDateForInput(date)).toBe('2024-03-15');
        });

        it('should pad single digit months and days', () => {
            const date = new Date(2024, 0, 5);
            expect(formatDateForInput(date)).toBe('2024-01-05');
        });
    });

    describe('formatDateTimeBR', () => {
        it('should format date and time in Brazilian format', () => {
            const date = new Date(2024, 2, 15, 14, 30, 0);
            const result = formatDateTimeBR(date);
            expect(result).toContain('15/03/2024');
            expect(result).toContain('14:30');
        });

        it('should handle null and undefined', () => {
            expect(formatDateTimeBR(null)).toBe('Não informado');
            expect(formatDateTimeBR(undefined)).toBe('Não informado');
        });
    });

    describe('isDateValidAndFuture', () => {
        it('should return false for null or undefined', () => {
            expect(isDateValidAndFuture(null)).toBe(false);
            expect(isDateValidAndFuture(undefined)).toBe(false);
        });

        it('should return true for today', () => {
            const today = new Date();
            expect(isDateValidAndFuture(today)).toBe(true);
        });

        it('should return true for future dates', () => {
            const tomorrow = new Date();
            tomorrow.setDate(tomorrow.getDate() + 1);
            expect(isDateValidAndFuture(tomorrow)).toBe(true);
        });

        it('should return false for past dates', () => {
            const yesterday = new Date();
            yesterday.setDate(yesterday.getDate() - 1);
            expect(isDateValidAndFuture(yesterday)).toBe(false);
        });
    });

    describe('diffInDays', () => {
        it('should calculate positive difference between dates', () => {
            const date1 = new Date(2024, 0, 1);
            const date2 = new Date(2024, 0, 5);
            expect(diffInDays(date1, date2)).toBe(4);
        });

        it('should calculate difference regardless of order', () => {
            const date1 = new Date(2024, 0, 5);
            const date2 = new Date(2024, 0, 1);
            expect(diffInDays(date1, date2)).toBe(4);
        });

        it('should return 0 for same date', () => {
            const date1 = new Date(2024, 0, 1);
            const date2 = new Date(2024, 0, 1);
            expect(diffInDays(date1, date2)).toBe(0);
        });
    });

    describe('ensureValidDate', () => {
        it('should return null for null or undefined', () => {
            expect(ensureValidDate(null)).toBeNull();
            expect(ensureValidDate(undefined)).toBeNull();
        });

        it('should return valid Date object for valid Date', () => {
            const validDate = new Date(2024, 2, 15);
            const result = ensureValidDate(validDate);
            expect(result).toBe(validDate);
        });

        it('should return null for invalid Date object', () => {
            const invalidDate = new Date('invalid');
            expect(ensureValidDate(invalidDate)).toBeNull();
        });

        it('should return null for Date with NaN time', () => {
            const nanDate = new Date(NaN);
            expect(ensureValidDate(nanDate)).toBeNull();
        });

        it('should handle Date objects with valid time but invalid components', () => {
            // Create a date that would be invalid (like February 30th)
            // JavaScript automatically corrects this to March 1st, so this test validates the corrected behavior
            const edgeCaseDate = new Date(2024, 1, 30); // February 30th becomes March 1st
            expect(ensureValidDate(edgeCaseDate)).toBeInstanceOf(Date);
            expect(ensureValidDate(edgeCaseDate)?.getMonth()).toBe(2); // March
            expect(ensureValidDate(edgeCaseDate)?.getDate()).toBe(1); // 1st
        });
    });
     describe('error handling in formatDateBR', () => {
         it('should return "Data inválida" for invalid date strings', () => {
             // Test with an invalid date string that will cause parseDate to fail
             const result = formatDateBR('invalid-date-string');
             expect(result).toBe('Data inválida');
         });
     });
});
     

     describe('error handling in formatDateForInput', () => {
         it('should return empty string when date operations throw an error', () => {
             // Create a date that will cause getFullYear() to throw
             const invalidDate = new Date('invalid');

             // This should trigger the catch block and return ''
             const result = formatDateForInput(invalidDate);
             expect(result).toBe('');
         });
     });

     describe('error handling in isDateValidAndFuture', () => {
         it('should return false when date operations throw an error', () => {
             // Create a date that will cause setHours() to throw
             const invalidDate = new Date('invalid');

             // This should trigger the catch block and return false
             const result = isDateValidAndFuture(invalidDate);
             expect(result).toBe(false);
         });
     });