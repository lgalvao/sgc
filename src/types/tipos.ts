import {SITUACOES_SUBPROCESSO} from '@/constants/situacoes';

export enum Perfil {
    ADMIN = 'ADMIN',
    GESTOR = 'GESTOR',
    CHEFE = 'CHEFE',
    SERVIDOR = 'SERVIDOR',
}

export enum TipoProcesso {
    MAPEAMENTO = 'Mapeamento',
    REVISAO = 'Revisão',
    DIAGNOSTICO = 'Diagnóstico',
}

export enum SituacaoProcesso {
    CRIADO = 'Criado',
    EM_ANDAMENTO = 'Em andamento',
    FINALIZADO = 'Finalizado',
}

export interface UnidadeSnapshot {
    sigla: string;
    tipo: string;
    filhas: UnidadeSnapshot[];
}

export interface Processo {
    id: number;
    descricao: string;
    tipo: TipoProcesso;
    dataLimite: Date;
    situacao: SituacaoProcesso;
    dataFinalizacao: Date | null;
    unidadesSnapshot?: UnidadeSnapshot[]; // Cópia da árvore/unidades participantes no momento da iniciação
}

export interface Subprocesso {
    id: number;
    idProcesso: number;
    unidade: string;
    situacao: typeof SITUACOES_SUBPROCESSO[keyof typeof SITUACOES_SUBPROCESSO];
    unidadeAtual: string;
    unidadeAnterior: string | null;
    dataLimiteEtapa1: Date;
    dataFimEtapa1: Date | null;
    dataLimiteEtapa2: Date | null;
    dataFimEtapa2: Date | null;
    sugestoes?: string;
    observacoes?: string; // Adicionado
    movimentacoes: Movimentacao[]; // Adicionado
    analises: AnaliseValidacao[]; // Adicionado
    idMapaCopiado?: number;
}

export enum TipoResponsabilidade {
    SUBSTITUICAO = 'Substituição',
    ATRIBUICAO = 'Atribuição temporária'
}

export interface Responsavel {
    idServidor: number;
    tipo: TipoResponsabilidade;
    dataInicio: Date | null;
    dataFim: Date | null;
}

export interface Unidade {
    id: number;
    sigla: string;
    tipo: string;
    nome: string;
    idServidorTitular: number;
    responsavel: Responsavel | null;
    filhas: Unidade[];
}

export interface Servidor {
    id: number;
    nome: string;
    unidade: string;
    email: string | null;
    ramal: string | null;
}

export interface Competencia {
    id: number;
    descricao: string;
    atividadesAssociadas: number[];
}

export interface Mapa {
    id: number;
    unidade: string;
    situacao: string;
    idProcesso: number;
    competencias: Competencia[];
    dataCriacao: Date;
    dataDisponibilizacao: Date | null;
    dataFinalizacao: Date | null;
    vigente?: boolean;
    dataInicio?: Date;
    dataFim?: Date | null;
    descricao?: string;
    tipo?: string;
    subprocessos?: Subprocesso[];
}

export interface Conhecimento {
    id: number;
    descricao: string;
}

export interface Atividade {
    id: number;
    descricao: string;
    idSubprocesso: number;
    conhecimentos: Conhecimento[];
}

export interface Alerta {
    id: number;
    unidadeOrigem: string;
    unidadeDestino: string;
    dataHora: Date;
    idProcesso: number;
    descricao: string;
}

export interface AlertaServidor {
    id: number;
    idAlerta: number;
    idServidor: number;
    lido: boolean;
    dataLeitura: Date | null;
}

export interface AtribuicaoTemporaria {
    idServidor: number;
    unidade: string;
    dataInicio: Date;
    dataTermino: Date;
    justificativa: string;
}

export interface Movimentacao {
    id: number;
    idSubprocesso: number;
    dataHora: Date;
    unidadeOrigem: string;
    unidadeDestino: string;
    descricao: string;
}

export enum ResultadoAnalise {
    DEVOLUCAO = 'Devolução',
    ACEITE = 'Aceite',
}

export interface AnaliseValidacao {
    id: number;
    idSubprocesso: number;
    dataHora: Date;
    unidade: string;
    resultado: ResultadoAnalise; // Usar o enum
    observacao?: string;
}
