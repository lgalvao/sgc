import apiClient from "@/axios-setup";
import {obterHojeFormatado} from "@/utils/date";

function baixarArquivo(blob: Blob, nomeArquivo: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', nomeArquivo);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
}

function nomearArquivoRelatorio(prefixo: string, extensao: "pdf" | "csv"): string {
    return `${prefixo}-${obterHojeFormatado()}.${extensao}`;
}

function escaparValorCsv(valor: string | number): string {
    const texto = String(valor).replaceAll('"', '""');
    return `"${texto}"`;
}

function criarLinhaCsv(colunas: Array<string | number>): string {
    return colunas.map(escaparValorCsv).join(";");
}

function montarLinhasRelatorioMapaCsv(relatorio: RelatorioMapa): string[] {
    const linhas: string[] = [];

    for (const competencia of relatorio.competencias) {
        if (competencia.atividades.length === 0) {
            linhas.push(criarLinhaCsv([
                competencia.descricao,
                "",
                "",
            ]));
            continue;
        }

        for (const atividade of competencia.atividades) {
            if (atividade.conhecimentos.length === 0) {
                linhas.push(criarLinhaCsv([
                    competencia.descricao,
                    atividade.descricao,
                    "",
                ]));
                continue;
            }

            for (const conhecimento of atividade.conhecimentos) {
                linhas.push(criarLinhaCsv([
                    competencia.descricao,
                    atividade.descricao,
                    conhecimento.descricao,
                ]));
            }
        }
    }

    return linhas;
}

function baixarCsv(relatorio: RelatorioMapa, nomeArquivo: string): void {
    const cabecalho = criarLinhaCsv([
        "Competência",
        "Atividade",
        "Conhecimento",
    ]);
    const linhas = montarLinhasRelatorioMapaCsv(relatorio);
    const conteudo = ['\uFEFF', cabecalho, ...linhas].join("\n");
    const blob = new Blob([conteudo], {type: "text/csv;charset=utf-8;"});
    baixarArquivo(blob, nomeArquivo);
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

    async obterRelatorioMapaAtual(codSubprocesso: number): Promise<RelatorioMapa> {
        const response = await apiClient.get<RelatorioMapa>(`/relatorios/mapas/subprocessos/${codSubprocesso}`);
        return response.data;
    },

    async obterRelatorioMapaVigenteUnidade(codUnidade: number): Promise<RelatorioMapa> {
        const response = await apiClient.get<RelatorioMapa>(`/relatorios/mapas-vigentes/unidades/${codUnidade}`);
        return response.data;
    },

    async downloadRelatorioAndamentoPdf(codProcesso: number): Promise<void> {
        const response = await apiClient.get(`/relatorios/andamento/${codProcesso}/exportar`, {
            responseType: 'blob'
        });
        baixarArquivo(new Blob([response.data]), nomearArquivoRelatorio("sgc-rel-andamento", "pdf"));
    },

    async downloadRelatorioMapasPdf(codigosUnidades: number[]): Promise<void> {
        const response = await apiClient.get("/relatorios/mapas/exportar", {
            params: {
                codUnidade: codigosUnidades
            },
            responseType: 'blob'
        });
        baixarArquivo(new Blob([response.data]), nomearArquivoRelatorio("sgc-rel-mapas", "pdf"));
    },

    async downloadRelatorioMapaAtualPdf(codSubprocesso: number): Promise<void> {
        const response = await apiClient.get(`/relatorios/mapas/subprocessos/${codSubprocesso}/exportar`, {
            responseType: "blob"
        });
        baixarArquivo(new Blob([response.data]), nomearArquivoRelatorio("sgc-rel-mapa-atual", "pdf"));
    },

    async downloadRelatorioMapaAtualCsv(codSubprocesso: number): Promise<void> {
        const relatorio = await this.obterRelatorioMapaAtual(codSubprocesso);
        baixarCsv(relatorio, nomearArquivoRelatorio("sgc-rel-mapa-atual", "csv"));
    },

    async downloadRelatorioMapaVigenteUnidadePdf(codUnidade: number): Promise<void> {
        const response = await apiClient.get(`/relatorios/mapas-vigentes/unidades/${codUnidade}/exportar`, {
            responseType: "blob"
        });
        baixarArquivo(new Blob([response.data]), nomearArquivoRelatorio("sgc-rel-mapa-vigente", "pdf"));
    },

    async downloadRelatorioMapaVigenteUnidadeCsv(codUnidade: number): Promise<void> {
        const relatorio = await this.obterRelatorioMapaVigenteUnidade(codUnidade);
        baixarCsv(relatorio, nomearArquivoRelatorio("sgc-rel-mapa-vigente", "csv"));
    },

    async downloadRelatorioUnidadesSemMapasVigentesPdf(): Promise<void> {
        const response = await apiClient.get("/relatorios/unidades-sem-mapas-vigentes/exportar", {
            responseType: "blob"
        });
        baixarArquivo(new Blob([response.data]), nomearArquivoRelatorio("sgc-rel-unidades-sem-mapas-vigentes", "pdf"));
    }
};
