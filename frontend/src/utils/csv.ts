export type CSVData = Record<string, string | number | undefined>;

export function gerarCSV(dados: CSVData[]): string {
  if (dados.length === 0) return "";

  const headers = Object.keys(dados[0]);
  const linhas = dados.map((item) =>
      headers.map((header) => `"${item[header]}"`).join(","),
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
