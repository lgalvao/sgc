import apiClient from '@/axios-setup';

function baixarArquivoPdf(dadosArquivo: BlobPart, nomeArquivo: string) {
  const urlTemporaria = globalThis.URL.createObjectURL(new Blob([dadosArquivo]));
  const link = document.createElement('a');

  link.href = urlTemporaria;
  link.setAttribute('download', nomeArquivo);
  document.body.appendChild(link);
  link.click();
  link.remove();
  globalThis.URL.revokeObjectURL(urlTemporaria);
}

export function useRelatorios() {
  const axios = apiClient;

  const obterRelatorioAndamento = async (codProcesso: number) => {
    const { data } = await axios.get(`/relatorios/andamento/${codProcesso}`);
    return data;
  };

  const downloadRelatorioAndamentoPdf = async (codProcesso: number) => {
    const response = await axios.get(`/relatorios/andamento/${codProcesso}/exportar`, {
      responseType: 'blob'
    });

    baixarArquivoPdf(response.data, `relatorio-andamento-${codProcesso}.pdf`);
  };

  const downloadRelatorioMapasPdf = async (codProcesso: number, unidadeId?: number) => {
    const sufixoUnidade = unidadeId ? `?unidadeId=${unidadeId}` : '';
    const response = await axios.get(`/relatorios/mapas/${codProcesso}/exportar${sufixoUnidade}`, {
      responseType: 'blob'
    });

    baixarArquivoPdf(response.data, `relatorio-mapas-${codProcesso}.pdf`);
  };

  return {
    obterRelatorioAndamento,
    downloadRelatorioAndamentoPdf,
    downloadRelatorioMapasPdf,
  };
}
