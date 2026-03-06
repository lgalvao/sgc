import apiClient from '@/axios-setup';

export function useRelatorios() {
  const axios = apiClient;

  const obterRelatorioAndamento = async (processoId: number) => {
    const { data } = await axios.get(`/relatorios/andamento/${processoId}`);
    return data;
  };

  const downloadRelatorioAndamentoPdf = async (processoId: number) => {
    const response = await axios.get(`/relatorios/andamento/${processoId}/exportar`, {
      responseType: 'blob'
    });
    
    // Create a download link for the PDF blob
    const url = globalThis.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `relatorio-andamento-${processoId}.pdf`);
    document.body.appendChild(link);
    link.click();
    link.remove();
  };
  
  const downloadRelatorioMapasPdf = async (processoId: number, unidadeId?: number) => {
    let urlStr = `/relatorios/mapas/${processoId}/exportar`;
    if (unidadeId) {
      urlStr += `?unidadeId=${unidadeId}`;
    }
    
    const response = await axios.get(urlStr, {
      responseType: 'blob'
    });
    
    const url = globalThis.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `relatorio-mapas-${processoId}.pdf`);
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
