import apiClient from "@/axios-setup";

export interface RelatorioAndamento {
  siglaUnidade: string;
  nomeUnidade: string;
  situacaoAtual: string;
  localizacao?: string;
  dataLimiteEtapa1: string | null;
  dataLimiteEtapa2: string | null;
  dataFimEtapa1: string | null;
  dataFimEtapa2: string | null;
  dataUltimaMovimentacao: string;
  responsavel: string;
  titular: string;
}

export interface RelatorioMapaConhecimento {
  codigo: number;
  descricao: string;
}

export interface RelatorioMapaAtividade {
  codigo: number;
  descricao: string;
  conhecimentos: RelatorioMapaConhecimento[];
}

export interface RelatorioMapaCompetencia {
  codigo: number;
  descricao: string;
  atividades: RelatorioMapaAtividade[];
}

export interface RelatorioMapa {
  codigoUnidade: number;
  siglaUnidade: string;
  nomeUnidade: string;
  totalCompetencias: number;
  competencias: RelatorioMapaCompetencia[];
}

export const relatoriosService = {
  async obterRelatorioAndamento(codProcesso: number): Promise<RelatorioAndamento[]> {
    const response = await apiClient.get<RelatorioAndamento[]>(`/relatorios/andamento/${codProcesso}`);
    return response.data;
  },

  async obterRelatorioMapas(codProcesso: number, unidadeId?: number): Promise<RelatorioMapa[]> {
    const sufixoUnidade = unidadeId ? `?codUnidade=${unidadeId}` : "";
    const response = await apiClient.get<RelatorioMapa[]>(`/relatorios/mapas/${codProcesso}${sufixoUnidade}`);
    return response.data;
  },

  async downloadRelatorioAndamentoPdf(codProcesso: number): Promise<void> {
    const response = await apiClient.get(`/relatorios/andamento/${codProcesso}/exportar`, {
      responseType: 'blob'
    });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `relatorio-andamento-${codProcesso}.pdf`);
    document.body.appendChild(link);
    link.click();
    link.remove();
  },

  async downloadRelatorioMapasPdf(codProcesso: number, unidadeId?: number): Promise<void> {
    const sufixoUnidade = unidadeId ? `?codUnidade=${unidadeId}` : '';
    const response = await apiClient.get(`/relatorios/mapas/${codProcesso}/exportar${sufixoUnidade}`, {
      responseType: 'blob'
    });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `relatorio-mapas-vigentes-${codProcesso}.pdf`);
    document.body.appendChild(link);
    link.click();
    link.remove();
  }
};
