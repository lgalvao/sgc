import Papa from "papaparse";

export type CSVData = Record<string, string | number | undefined>;

/**
 * Preprocesses value to mitigate CSV Injection (Formula Injection).
 * Prepends a single quote (') if the value starts with =, +, -, or @.
 */
function sanitizeCSVValue(value: unknown): string | number | undefined {
  if (value === undefined || value === null) {
    return "";
  }
  
  const stringValue = String(value);
  
  // CSV Injection prevention
  if (/^[=+\-@]/.test(stringValue)) {
    return "'" + stringValue;
  }
  
  return value as string | number;
}

export function gerarCSV(dados: CSVData[]): string {
  if (dados.length === 0) return "";

  // Sanitize data before passing to PapaParse
  const sanitizedData = dados.map(row => {
    const newRow: Record<string, any> = {};
    Object.keys(row).forEach(key => {
      newRow[key] = sanitizeCSVValue(row[key]);
    });
    return newRow;
  });

  return Papa.unparse(sanitizedData, {
    quotes: true,
    quoteChar: '"',
    escapeChar: '"',
    delimiter: ",",
    header: true,
    newline: "\n",
    skipEmptyLines: true, // Good practice
  });
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
    link.remove();
  }
}
