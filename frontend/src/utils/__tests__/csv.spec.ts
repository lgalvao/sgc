import {describe, expect, it} from 'vitest';
import {gerarCSV} from '../csv';

describe('csv utils', () => {
  it('should generate correct CSV for simple data', () => {
    const data = [
      { name: 'John', age: 30, city: 'New York' },
      { name: 'Jane', age: 25, city: 'London' }
    ];
    const csv = gerarCSV(data);
    const expected = '"name","age","city"\n"John","30","New York"\n"Jane","25","London"';
    expect(csv).toBe(expected);
  });

  it('should escape double quotes in data', () => {
    const data = [
      { id: 1, note: 'Hello "World"' }
    ];
    const csv = gerarCSV(data);
    // Expected: quotes inside value should be doubled: "Hello ""World"""
    const expected = '"id","note"\n"1","Hello ""World"""';
    expect(csv).toBe(expected);
  });

  it('should sanitize fields to prevent CSV Injection', () => {
    const data = [
      { malicious: '=cmd|/C calc!A0' },
      { malicious: '+1+1' },
      { malicious: '-1+1' },
      { malicious: '@SUM(1+1)' }
    ];

    const csv = gerarCSV(data);

    const lines = csv.split('\n');
    // First line is header
    // Second line: =cmd... should be prefixed with ' and wrapped in quotes
    expect(lines[1]).toContain(`"'=cmd|/C calc!A0"`);
    expect(lines[2]).toContain(`"'+1+1"`);
    expect(lines[3]).toContain(`"'-1+1"`);
    expect(lines[4]).toContain(`"'@SUM(1+1)"`);
  });

  it('should handle empty data', () => {
    expect(gerarCSV([])).toBe('');
  });
});
