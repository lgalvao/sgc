import apiClient from "@/axios-setup";

function baixarPdf(blob: Blob, nomeArquivo: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', nomeArquivo);
    document.body.appendChild(link);
    link.click();
    link.remove();
}

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

    async obterRelatorioMapas(codigosUnidades: number[]): Promise<RelatorioMapa[]> {
        const response = await apiClient.get<RelatorioMapa[]>("/relatorios/mapas", {
            params: {
                codUnidade: codigosUnidades
            }
        });
        return response.data;
    },

    async downloadRelatorioAndamentoPdf(codProcesso: number): Promise<void> {
        const response = await apiClient.get(`/relatorios/andamento/${codProcesso}/exportar`, {
            responseType: 'blob'
        });
        baixarPdf(new Blob([response.data]), `relatorio-andamento-${codProcesso}.pdf`);
    },

    async downloadRelatorioMapasPdf(codigosUnidades: number[]): Promise<void> {
        const response = await apiClient.get("/relatorios/mapas/exportar", {
            params: {
                codUnidade: codigosUnidades
            },
            responseType: 'blob'
        });
        baixarPdf(new Blob([response.data]), 'relatorio-mapas-vigentes.pdf');
    }
};
