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
    async buscarDiagnostico(subprocessoId: number): Promise<DiagnosticoDto> {
        const response = await apiClient.get(`/diagnosticos/${subprocessoId}`);
        return response.data;
    },

    async salvarAvaliacao(
        subprocessoId: number,
        competenciaCodigo: number,
        importancia: string,
        dominio: string,
        observacoes?: string
    ): Promise<AvaliacaoServidorDto> {
        const response = await apiClient.post(
            `/diagnosticos/${subprocessoId}/avaliacoes`,
            {competenciaCodigo, importancia, dominio, observacoes}
        );
        return response.data;
    },

    async buscarMinhasAvaliacoes(
        subprocessoId: number,
        servidorTitulo?: string
    ): Promise<AvaliacaoServidorDto[]> {
        const params = servidorTitulo ? {servidorTitulo} : {};
        const response = await apiClient.get(
            `/diagnosticos/${subprocessoId}/avaliacoes/minhas`,
            {params}
        );
        return response.data;
    },

    async concluirAutoavaliacao(
        subprocessoId: number,
        justificativaAtraso?: string
    ): Promise<void> {
        await apiClient.post(
            `/diagnosticos/${subprocessoId}/avaliacoes/concluir`,
            {justificativaAtraso}
        );
    },

    async salvarOcupacao(
        subprocessoId: number,
        servidorTitulo: string,
        competenciaCodigo: number,
        situacao: string
    ): Promise<OcupacaoCriticaDto> {
        const response = await apiClient.post(
            `/diagnosticos/${subprocessoId}/ocupacoes`,
            {servidorTitulo, competenciaCodigo, situacao}
        );
        return response.data;
    },

    async buscarOcupacoes(
        subprocessoId: number
    ): Promise<OcupacaoCriticaDto[]> {
        const response = await apiClient.get(
            `/diagnosticos/${subprocessoId}/ocupacoes`
        );
        return response.data;
    },

    async concluirDiagnostico(
        subprocessoId: number,
        justificativa?: string
    ): Promise<DiagnosticoDto> {
        const response = await apiClient.post(
            `/diagnosticos/${subprocessoId}/concluir`,
            {justificativa}
        );
        return response.data;
    },
};
