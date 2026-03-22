import apiClient from '@/axios-setup';

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
    
    // Create a download link for the PDF blob
    const url = globalThis.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `relatorio-andamento-${codProcesso}.pdf`);
    document.body.appendChild(link);
    link.click();
    link.remove();
  };
  
  const downloadRelatorioMapasPdf = async (codProcesso: number, codigoUnidade?: number) => {
    let urlStr = `/relatorios/mapas/${codProcesso}/exportar`;
    if (codigoUnidade) {
      urlStr += `?codigoUnidade=${codigoUnidade}`;
    }
    
    const response = await axios.get(urlStr, {
      responseType: 'blob'
    });
    
    const url = globalThis.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `relatorio-mapas-${codProcesso}.pdf`);
    document.body.appendChild(link);
    link.click();
    link.remove();
  };

  return {
    obterRelatorioAndamento,
    downloadRelatorioAndamentoPdf,
    downloadRelatorioMapasPdf,
  };
}
