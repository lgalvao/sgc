/**
 * Tipos de DTOs retornados pelo backend.
 * Esses tipos são usados para fornecer type safety aos mappers.
 */

export interface UnidadeParticipanteDto {
    codUnidade: number;
    codUnidadeSuperior?: number;
    sigla?: string;
    nome?: string;
    codSubprocesso?: number;
    situacaoSubprocesso?: string;
    dataLimite?: string;
    mapaCodigo?: number;
    localizacaoAtualCodigo?: number;
    filhos?: UnidadeParticipanteDto[];
}

export interface ProcessoDetalheDto {
    codigo?: number;
    tipo?: string;
    situacao?: string;
    dataCriacao?: string;
    dataInicio?: string;
    dataFinalizacao?: string;
    unidades?: UnidadeParticipanteDto[];
    acoesBloco?: unknown[];

    [key: string]: unknown; // Para campos adicionais do spread
}

// DTOs para SGRH (autenticação e login)
export interface UnidadeDto {
    codigo: number;
    nome: string;
    sigla: string;
}

export interface PerfilUnidadeDto {
    perfil: string;
    unidade: UnidadeDto;
    siglaUnidade: string;
}

export interface SessaoLoginDto {
    tituloEleitoral: string;
    nome: string;
    perfil: string;
    unidadeCodigo: number;
    permissoes: PermissoesSessaoDto;
}

export interface PermissoesSessaoDto {
    mostrarCriarProcesso: boolean;
    mostrarArvoreCompletaUnidades: boolean;
    mostrarCtaPainelVazio: boolean;
    mostrarDiagnosticoOrganizacional: boolean;
    mostrarMenuConfiguracoes: boolean;
    mostrarMenuAdministradores: boolean;
    mostrarCriarAtribuicaoTemporaria: boolean;
}

export interface FluxoLoginResponseDto {
    autenticado: boolean;
    requerSelecaoPerfil: boolean;
    perfisUnidades: PerfilUnidadeDto[];
    sessao: SessaoLoginDto | null;
}
