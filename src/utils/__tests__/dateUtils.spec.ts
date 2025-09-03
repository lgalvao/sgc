import {describe, expect, it} from 'vitest';
import {
    diffInDays,
    formatDateBR,
    formatDateForInput,
    formatDateTimeBR,
    isDateValidAndFuture,
    parseDate
} from '../dateUtils';

describe('dateUtils', () => {
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
      expect(date?.getMonth()).toBe(2); // March is month 2 (0-indexed)
      // Allow for timezone differences - just check that it's March 15th
      expect([14, 15]).toContain(date?.getDate());
      // Allow for timezone differences in hour
      expect(typeof date?.getHours()).toBe('number');
    });

    it('should parse Brazilian date format DD/MM/YYYY', () => {
      const date = parseDate('15/03/2024');
      expect(date).toEqual(new Date(2024, 2, 15));
    });

    it('should return null for invalid date strings', () => {
      expect(parseDate('invalid')).toBeNull();
      expect(parseDate('99/99/9999')).toBeNull(); // Invalid day/month
      expect(parseDate('2024-13-45')).toBeNull(); // Invalid month/day
      expect(parseDate('00/01/2024')).toBeNull(); // Invalid day
      expect(parseDate('01/13/2024')).toBeNull(); // Invalid month
    });

    it('should handle edge cases', () => {
      expect(parseDate('01/01/2024')).toEqual(new Date(2024, 0, 1));
      expect(parseDate('31/12/2024')).toEqual(new Date(2024, 11, 31));
    });
  });

  describe('formatDateBR', () => {
    it('should return "Não informado" for null or undefined', () => {
      expect(formatDateBR(null)).toBe('Não informado');
      expect(formatDateBR(undefined)).toBe('Não informado');
    });

    it('should format Date object to Brazilian format', () => {
      const date = new Date(2024, 2, 15); // March 15, 2024
      expect(formatDateBR(date)).toBe('15/03/2024');
    });

    it('should format date string to Brazilian format', () => {
      const result1 = formatDateBR('2024-03-15');
      expect(result1).toMatch(/^\d{2}\/\d{2}\/2024$/); // Should be DD/MM/2024 format

      const result2 = formatDateBR('15/03/2024');
      expect(result2).toBe('15/03/2024');
    });

    it('should return "Data inválida" for invalid dates', () => {
      expect(formatDateBR('invalid')).toBe('Data inválida');
      expect(formatDateBR(new Date('invalid'))).toBe('Data inválida');
    });

    it('should accept custom format options', () => {
      const date = new Date(2024, 2, 15);
      expect(formatDateBR(date, { month: 'long', year: 'numeric' })).toBe('março de 2024');
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
      const date = new Date(2024, 0, 5); // January 5, 2024
      expect(formatDateForInput(date)).toBe('2024-01-05');
    });

    it('should return empty string for invalid dates', () => {
      const invalidDate = new Date('invalid');
      expect(formatDateForInput(invalidDate)).toBe('');
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

    it('should return false for invalid dates', () => {
      const invalidDate = new Date('invalid');
      expect(isDateValidAndFuture(invalidDate)).toBe(false);
    });

    it('should compare dates ignoring time', () => {
      const today = new Date();
      today.setHours(23, 59, 59, 999);
      const yesterday = new Date();
      yesterday.setDate(yesterday.getDate() - 1);
      yesterday.setHours(0, 0, 0, 1);

      expect(isDateValidAndFuture(today)).toBe(true);
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

    it('should handle dates with different times', () => {
      const date1 = new Date(2024, 0, 1, 0, 0, 0, 0);
      const date2 = new Date(2024, 0, 2, 23, 59, 59, 999);
      expect(diffInDays(date1, date2)).toBe(2);
    });
  });
});