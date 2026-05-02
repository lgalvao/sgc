import type {Unidade} from "./organizacao";

export interface Conhecimento {
  codigo: number;
  descricao: string;
}

export interface Atividade {
  codigo: number;
  descricao: string;
  conhecimentos: Conhecimento[];
  mapaCodigo?: number;
}

export interface Competencia {
  codigo: number;
  descricao: string;
  atividades: Atividade[];
}

export interface Mapa {
  codigo: number;
  descricao: string;
  unidade: Unidade;
  codProcesso: number;
  competencias: Competencia[];
  situacao: string;
  dataCriacao: string;
  dataDisponibilizacao?: string;
  dataFinalizacao?: string;
}

export interface MapaResumo {
  codigo: number;
  subprocessoCodigo: number;
  dataHoraDisponibilizado?: string | null;
  observacoesDisponibilizacao?: string | null;
  sugestoes?: string | null;
  dataHoraHomologado?: string | null;
}

export interface MapaVisualizacao {
  codigo: number;
  descricao: string;
  competencias: Competencia[];
  atividadesSemCompetencia?: Atividade[];
  sugestoes?: string;
}

export interface MapaCompleto {
  codigo: number;
  subprocessoCodigo: number;
  observacoes: string;
  competencias: Competencia[];
  atividades: Atividade[];
  situacao: string;
}

export interface MapaAjuste {
  codigo: number;
  descricao: string;
  competencias: Competencia[];
}
