import apiClient from '@/axios-setup';

export interface DiagnosticoDto {
    codigo: number;
    subprocessoCodigo: number;
    situacao: string;
    situacaoLabel: string;
    dataConclusao: string | null;
    dataConclusaoFormatada: string | null;
    justificativaConclusao: string | null;
    servidores: ServidorDiagnosticoDto[];
    podeSerConcluido: boolean;
    motivoNaoPodeConcluir: string | null;
}

export interface ServidorDiagnosticoDto {
    tituloEleitoral: string;
    nome: string;
    situacao: string;
    situacaoLabel: string;
    avaliacoes: AvaliacaoServidorDto[];
    ocupacoes: OcupacaoCriticaDto[];
    totalCompetencias: number;
    competenciasAvaliadas: number;
    ocupacoesPreenchidas: number;
}

export interface AvaliacaoServidorDto {
    codigo: number;
    competenciaCodigo: number;
    competenciaDescricao: string;
    importancia: string;
    importanciaLabel: string;
    dominio: string;
    dominioLabel: string;
    gap: number | null;
    observacoes: string | null;
}

export interface OcupacaoCriticaDto {
    codigo: number;
    competenciaCodigo: number;
    competenciaDescricao: string;
    situacao: string;
    situacaoLabel: string;
}

export const diagnosticoService = {
    async buscarDiagnostico(codSubprocesso: number): Promise<DiagnosticoDto> {
        const response = await apiClient.get(`/diagnosticos/${codSubprocesso}`);
        return response.data;
    },

    async salvarAvaliacao(
        codSubprocesso: number,
        competenciaCodigo: number,
        importancia: string,
        dominio: string,
        observacoes?: string
    ): Promise<AvaliacaoServidorDto> {
        const response = await apiClient.post(
            `/diagnosticos/${codSubprocesso}/avaliacoes`,
            {competenciaCodigo, importancia, dominio, observacoes}
        );
        return response.data;
    },

    async buscarMinhasAvaliacoes(
        codSubprocesso: number,
        servidorTitulo?: string
    ): Promise<AvaliacaoServidorDto[]> {
        const params = servidorTitulo ? {servidorTitulo} : {};
        const response = await apiClient.get(
            `/diagnosticos/${codSubprocesso}/avaliacoes/minhas`,
            {params}
        );
        return response.data;
    },

    async concluirAutoavaliacao(
        codSubprocesso: number,
        justificativaAtraso?: string
    ): Promise<void> {
        await apiClient.post(
            `/diagnosticos/${codSubprocesso}/avaliacoes/concluir`,
            {justificativaAtraso}
        );
    },

    async salvarOcupacao(
        codSubprocesso: number,
        servidorTitulo: string,
        competenciaCodigo: number,
        situacao: string
    ): Promise<OcupacaoCriticaDto> {
        const response = await apiClient.post(
            `/diagnosticos/${codSubprocesso}/ocupacoes`,
            {servidorTitulo, competenciaCodigo, situacao}
        );
        return response.data;
    },

    async buscarOcupacoes(
        codSubprocesso: number
    ): Promise<OcupacaoCriticaDto[]> {
        const response = await apiClient.get(
            `/diagnosticos/${codSubprocesso}/ocupacoes`
        );
        return response.data;
    },

    async concluirDiagnostico(
        codSubprocesso: number,
        justificativa?: string
    ): Promise<DiagnosticoDto> {
        const response = await apiClient.post(
            `/diagnosticos/${codSubprocesso}/concluir`,
            {justificativa}
        );
        return response.data;
    },
};
