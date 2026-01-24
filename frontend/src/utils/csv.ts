export type CSVData = Record<string, string | number | undefined>;

/**
 * Escapes a value for CSV format and mitigates CSV Injection (Formula Injection).
 * - Wraps value in double quotes.
 * - Escapes existing double quotes by doubling them (" -> "").
 * - Prepends a single quote (') if the value starts with =, +, -, or @ to prevent formula execution.
 */
function escapeCSVValue(value: string | number | undefined): string {
  if (value === undefined || value === null) {
    // Maintain behavior of converting undefined/null to string, or treat as empty?
    // Original code used template literal `${item[header]}` which converts undefined to "undefined".
    // We will stick to String(value) to minimize behavior change, although "undefined" in CSV is likely not desired.
    // However, for security, let's treat it as string first.
    // Actually, usually one wants empty string for null/undefined.
    // Let's assume we want to match the original output "undefined" if that was the case,
    // OR improve it. Given I am Sentinel, I should focus on Security.
    // But let's check what `String(undefined)` returns. It is "undefined".
    // I will use String(value) to be safe.
  }

  let stringValue = String(value);

  // CSV Injection prevention
  // If value starts with =, +, -, or @, prepend a single quote
  if (/^[=+\-@]/.test(stringValue)) {
    stringValue = "'" + stringValue;
  }

  // Escape double quotes by doubling them
  if (stringValue.includes('"')) {
    stringValue = stringValue.replace(/"/g, '""');
  }

  return `"${stringValue}"`;
}

export function gerarCSV(dados: CSVData[]): string {
  if (dados.length === 0) return "";

  const headers = Object.keys(dados[0]);
  const linhas = dados.map((item) =>
      headers.map((header) => escapeCSVValue(item[header])).join(","),
  );

  return [headers.join(","), ...linhas].join("\n");
}

export function downloadCSV(csv: string, nomeArquivo: string) {
  const blob = new Blob([csv], {type: "text/csv;charset=utf-8;"});
  const link = document.createElement("a");

  if (link.download !== undefined) {
    const url = URL.createObjectURL(blob);
    link.setAttribute("href", url);
    link.setAttribute("download", nomeArquivo);
    link.style.visibility = "hidden";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
}
