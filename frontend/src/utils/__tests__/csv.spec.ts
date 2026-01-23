import { describe, it, expect } from "vitest";
import { gerarCSV } from "../csv";

describe("gerarCSV", () => {
  it("should generate valid CSV from data", () => {
    const data = [
      { name: "Alice", role: "Admin" },
      { name: "Bob", role: "User" },
    ];
    const csv = gerarCSV(data);
    const expected = 'name,role\n"Alice","Admin"\n"Bob","User"';
    expect(csv).toBe(expected);
  });

  it("should sanitize fields starting with =", () => {
    const data = [{ name: "=cmd|/C calc!A0", role: "User" }];
    const csv = gerarCSV(data);
    // Expecting the single quote prefix for sanitization
    const expected = 'name,role\n"\'=cmd|/C calc!A0","User"';
    expect(csv).toBe(expected);
  });

  it("should sanitize fields starting with +", () => {
    const data = [{ name: "+100", role: "User" }];
    const csv = gerarCSV(data);
    const expected = 'name,role\n"\'+100","User"';
    expect(csv).toBe(expected);
  });

  it("should sanitize fields starting with -", () => {
    const data = [{ name: "-100", role: "User" }];
    const csv = gerarCSV(data);
    const expected = 'name,role\n"\'-100","User"';
    expect(csv).toBe(expected);
  });

  it("should sanitize fields starting with @", () => {
    const data = [{ name: "@sum(1+1)", role: "User" }];
    const csv = gerarCSV(data);
    const expected = 'name,role\n"\'@sum(1+1)","User"';
    expect(csv).toBe(expected);
  });

  it("should handle non-string values gracefully", () => {
    const data = [{ count: 123, val: null, und: undefined }];
    const csv = gerarCSV(data as any);
    const expectedData = 'count,val,und\n"123","",""';
    expect(csv).toBe(expectedData);

    const dataNum = [{ val: -50 }];
    const csvNum = gerarCSV(dataNum);
    // Should -50 be sanitized? Yes, formulas can start with -
    const expected = 'val\n"\'-50"';
    expect(csvNum).toBe(expected);
  });

  it("should escape double quotes to prevent CSV injection bypass", () => {
    const data = [{ val: 'foo"bar' }];
    const csv = gerarCSV(data);
    const expected = 'val\n"foo""bar"';
    expect(csv).toBe(expected);
  });

  it("should handle complex bypass attempts", () => {
    // Input that tries to close the quote and start a new cell with a formula
    const data = [{ val: 'safe",=cmd|/C calc!A0' }];
    const csv = gerarCSV(data);
    // Should be escaped as: "safe"",=cmd|/C calc!A0"
    // Excel reads this as a single cell containing: safe",=cmd|/C calc!A0
    const expected = 'val\n"safe"",=cmd|/C calc!A0"';
    expect(csv).toBe(expected);
  });
});
